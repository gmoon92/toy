package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.TestCachePolicy;

@DisplayName("정책 없이 쓰이는 캐시명 검사")
class AnnotatedCacheNameValidatorTest {

	@Nested
	@DisplayName("정책 없는 캐시명으로 @Cacheable 을 붙이면")
	class WhenCacheNameNotDeclared {

		@Test
		@DisplayName("기동을 막는다")
		void failsToStart() {
			assertThatThrownBy(() -> start(UndeclaredCacheConfig.class))
				 .as("호출 시점에 막으면 트래픽 중에 터진다. 선언 누락은 배포에서 드러나야 한다")
				 .isInstanceOf(IllegalStateException.class)
				 .hasMessageContaining("UNDECLARED")
				 .hasMessageContaining("CachePolicy");
		}

		@Test
		@DisplayName("검사기가 없으면 기동이 그대로 성공한다")
		void startsWithoutValidator() {
			assertThatNoException()
				 .as("이 명제가 깨지면 위 테스트가 검사기 덕분인지 알 수 없다")
				 .isThrownBy(() -> start(UndeclaredCacheConfig.WithoutValidator.class).close());
		}
	}

	@Nested
	@DisplayName("모든 캐시명에 정책이 있으면")
	class WhenEveryCacheNameDeclared {

		@Test
		@DisplayName("그대로 기동한다")
		void startsNormally() {
			assertThatNoException()
				 .as("선언을 강제하면서 정상 선언까지 막으면 쓸 수 없다")
				 .isThrownBy(() -> start(DeclaredCacheConfig.class).close());
		}
	}

	private static AnnotationConfigApplicationContext start(Class<?>... configs) {
		return new AnnotationConfigApplicationContext(configs);
	}

	static class UndeclaredReader {

		@Cacheable("UNDECLARED")
		String read(Long id) {
			return "value";
		}
	}

	static class DeclaredReader {

		@Cacheable(TestCachePolicy.Name.USER)
		String read(Long id) {
			return "value";
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableCaching
	static class UndeclaredCacheConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}

		@Bean
		UndeclaredReader undeclaredReader() {
			return new UndeclaredReader();
		}

		@Bean
		SmartInitializingSingleton validator(
			 ConfigurableListableBeanFactory beanFactory,
			 CacheOperationSource cacheOperationSource
		) {
			return new AnnotatedCacheNameValidator(
				 beanFactory,
				 cacheOperationSource,
				 () -> List.of(TestCachePolicy.USER)
			);
		}

		@Configuration(proxyBeanMethods = false)
		@EnableCaching
		static class WithoutValidator {

			@Bean
			CacheManager cacheManager() {
				return new ConcurrentMapCacheManager();
			}

			@Bean
			UndeclaredReader undeclaredReader() {
				return new UndeclaredReader();
			}
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableCaching
	static class DeclaredCacheConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}

		@Bean
		DeclaredReader declaredReader() {
			return new DeclaredReader();
		}

		@Bean
		SmartInitializingSingleton validator(
			 ConfigurableListableBeanFactory beanFactory,
			 CacheOperationSource cacheOperationSource
		) {
			return new AnnotatedCacheNameValidator(
				 beanFactory,
				 cacheOperationSource,
				 () -> List.of(TestCachePolicy.USER)
			);
		}
	}
}
