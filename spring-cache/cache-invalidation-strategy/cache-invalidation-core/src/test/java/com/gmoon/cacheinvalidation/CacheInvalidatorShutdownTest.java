package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 종료 순서는 추론이 아니라 실제 컨테이너로 확인한다.
 * <p>
 * 무효화기가 캐시 관리자보다 먼저 정리되지 않으면, 남은 삭제를 비우려는 순간
 * 캐시가 이미 닫혀 있어 드레인이 아무 일도 하지 못한다.
 */
@DisplayName("애플리케이션 종료 시 남은 삭제")
class CacheInvalidatorShutdownTest {

	@Nested
	@DisplayName("비동기 실행기가 삭제를 아직 돌리지 않았으면")
	class WhenExecutorStillHoldsWork {

		@Test
		@DisplayName("캐시 관리자가 닫히기 전에 마저 지운다")
		void drainsBeforeCacheManagerCloses() {
			List<Runnable> queued = new ArrayList<>();
			ClosingCacheManager caches;

			try (AnnotationConfigApplicationContext context = contextWith(queued::add)) {
				caches = context.getBean(ClosingCacheManager.class);
				context.getBean(CacheInvalidator.class)
					 .invalidate(EntityChange.of(new Object(), EntityChange.Type.UPDATE));

				assertThat(caches.evictedKeys)
					 .as("실행기가 들고 있는 동안에는 지워지지 않아야 이 테스트가 의미를 가진다")
					 .isEmpty();
			}

			assertThat(caches.evictedKeys)
				 .as("무효화기가 캐시 관리자보다 먼저 정리되지 않으면 드레인이 빈 손으로 끝난다")
				 .containsExactly("1");
			assertThat(caches.evictedAfterClose)
				 .as("닫힌 캐시에 대고 지우면 운영에서는 커넥션이 끊긴 뒤의 호출이 된다")
				 .isFalse();
		}
	}

	private static AnnotationConfigApplicationContext contextWith(Executor executor) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(Executor.class, () -> executor);
		context.register(ShutdownTestConfig.class);
		context.refresh();
		return context;
	}

	@Configuration(proxyBeanMethods = false)
	static class ShutdownTestConfig {

		@Bean
		ClosingCacheManager cacheManager() {
			return new ClosingCacheManager();
		}

		@Bean
		CacheInvalidator cacheInvalidator(CacheManager cacheManager, Executor executor) {
			return CacheInvalidator.of(
				 List.of(userCacheRule()),
				 cacheManager,
				 executor,
				 null
			);
		}
	}

	private static InvalidationRule userCacheRule() {
		return change -> List.of(CacheKey.of(TestCachePolicy.USER, 1L));
	}

	static class ClosingCacheManager implements CacheManager, DisposableBean {

		private final List<String> evictedKeys = new ArrayList<>();
		private final Cache userCache = new RecordingCache();

		private boolean closed;
		private boolean evictedAfterClose;

		@Override
		public Cache getCache(String name) {
			return userCache;
		}

		@Override
		public Collection<String> getCacheNames() {
			return List.of(TestCachePolicy.Name.USER);
		}

		@Override
		public void destroy() {
			closed = true;
		}

		private final class RecordingCache extends ConcurrentMapCache {

			private RecordingCache() {
				super(TestCachePolicy.Name.USER);
			}

			@Override
			public void evict(Object key) {
				if (closed) {
					evictedAfterClose = true;
				}
				evictedKeys.add(String.valueOf(key));
				super.evict(key);
			}
		}
	}
}
