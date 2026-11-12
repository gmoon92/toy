package com.gmoon.writeinvalidate.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Set;

import org.hibernate.event.spi.PostUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import com.gmoon.writeinvalidate.config.CachePolicy;

@DisplayName("엔티티 변경에 따른 캐시 삭제")
class CacheEvictListenerTest {

	private CacheManager cacheManager;
	private Cache userCache;
	private MeterRegistry registry;
	private CacheEvictListener listener;

	@BeforeEach
	void setUp() {
		cacheManager = mock(CacheManager.class);
		userCache = mock(Cache.class);
		given(cacheManager.getCache(CachePolicy.Name.USER)).willReturn(userCache);
		registry = new SimpleMeterRegistry();
		listener = new CacheEvictListener(cacheManager, registry);
	}

	@Nested
	@DisplayName("하이버네이트가 CacheEvictable 엔티티의 변경을 알리면")
	class WhenHibernateReportsEvictable {

		@Test
		@DisplayName("엔티티가 밝힌 키를 지운다")
		void evictsDeclaredKeys() {
			listener.onPostUpdate(updateOf(new EvictableUser(1L)));

			then(userCache).should(description("엔티티가 자기 키를 안다는 전제가 깨지면 stale 이 남는다")).evict("1");
		}
	}

	@Nested
	@DisplayName("하이버네이트가 CacheEvictable 이 아닌 엔티티의 변경을 알리면")
	class WhenHibernateReportsOther {

		@Test
		@DisplayName("아무 캐시도 건드리지 않는다")
		void evictsNothing() {
			listener.onPostUpdate(updateOf(new Object()));

			then(cacheManager).should(never().description("선언하지 않은 엔티티까지 반응하면 남의 캐시를 지운다"))
				 .getCache(any());
		}
	}

	@Nested
	@DisplayName("CacheEvictable 엔티티를 직접 발행하면")
	class WhenEntityPublished {

		@Test
		@DisplayName("엔티티가 밝힌 키를 지운다")
		void evictsDeclaredKeys() {
			listener.onEntityChanged(new EvictableUser(1L));

			then(userCache).should(description("JPA 를 거치지 않은 쓰기도 같은 경로로 지워져야 한다")).evict("1");
		}
	}

	@Nested
	@DisplayName("엔티티가 키를 내놓다 예외를 던지면")
	class WhenCacheKeysThrow {

		private final CacheEvictable broken = new BrokenEvictable();

		@Test
		@DisplayName("호출자에게 예외를 돌려주지 않는다")
		void doesNotPropagate() {
			assertThatNoException()
				 .as("신호는 트랜잭션 안에서 오므로 예외가 새면 캐시 때문에 트랜잭션이 롤백된다")
				 .isThrownBy(() -> listener.onEntityChanged(broken));
		}

		@Test
		@DisplayName("실패 수를 엔티티 이름과 함께 센다")
		void countsFailure() {
			listener.onEntityChanged(broken);

			assertThat(registry.find("cache.invalidation.failures")
				 .tag("phase", "resolve")
				 .tag("target", BrokenEvictable.class.getSimpleName())
				 .counter()
				 .count())
				 .as("로그만 남기면 집계하지 않는 한 어느 엔티티가 깨졌는지 아무도 모른다")
				 .isEqualTo(1);
		}
	}

	private static PostUpdateEvent updateOf(Object entity) {
		PostUpdateEvent event = mock(PostUpdateEvent.class);
		given(event.getEntity()).willReturn(entity);
		return event;
	}

	private record EvictableUser(Long id) implements CacheEvictable {

		@Override
		public Set<CacheKey> cacheKeys() {
			return Set.of(CacheKey.of(CachePolicy.USER, id));
		}
	}

	private static class BrokenEvictable implements CacheEvictable {

		@Override
		public Set<CacheKey> cacheKeys() {
			throw new IllegalStateException("key is not loaded");
		}
	}
}
