package com.gmoon.cacheinvalidation.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.gmoon.cacheinvalidation.CachePolicy;

import lombok.RequiredArgsConstructor;

/**
 * 정책 없이 쓰이는 캐시명을 기동 시점에 찾아 기동을 막는다.
 * <p>
 * 캐시 예외는 서비스를 살리기 위해 삼켜지므로, 선언이 빠진 사실은 런타임에
 * "적중률 0" 으로만 드러난다. 원본 부하만 조용히 올라간다.
 * 그래서 트래픽이 아니라 배포에서 드러나게 한다.
 * <p>
 * 애노테이션에 적힌 이름만 본다. {@code cacheResolver} 를 직접 지정했거나
 * 코드에서 {@code getCache(name)} 으로 꺼내 쓰는 경로는 여기서 잡히지 않는다.
 * 그 경로는 {@code disableCreateOnMissingCache()} 가 호출 시점에 막는다.
 */
@RequiredArgsConstructor
class AnnotatedCacheNameValidator implements SmartInitializingSingleton {

	private final ConfigurableListableBeanFactory beanFactory;
	private final CacheOperationSource cacheOperations;
	private final Supplier<Collection<CachePolicy>> declaredPolicies;

	@Override
	public void afterSingletonsInstantiated() {
		Set<String> declared = declaredPolicies.get()
			 .stream()
			 .map(CachePolicy::cacheName)
			 .collect(Collectors.toUnmodifiableSet());

		List<String> undeclared = annotatedCacheNames().stream()
			 .filter(name -> !declared.contains(name))
			 .toList();

		if (!undeclared.isEmpty()) {
			throw new IllegalStateException("Caches used without a CachePolicy: " + undeclared
				 + ". Declare a CachePolicy for each, so the cache gets a ttl and a value type.");
		}
	}

	private Set<String> annotatedCacheNames() {
		Set<String> names = new TreeSet<>();
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			Class<?> type = typeOf(beanName);
			if (type != null) {
				ReflectionUtils.doWithMethods(type, method -> collectInto(names, method, type));
			}
		}
		return names;
	}

	private void collectInto(Set<String> names, java.lang.reflect.Method method, Class<?> type) {
		Collection<CacheOperation> operations = cacheOperations.getCacheOperations(method, type);
		if (operations != null) {
			operations.forEach(operation -> names.addAll(operation.getCacheNames()));
		}
	}

	/**
	 * 빈을 만들지 않고 타입만 묻는다. 타입을 알 수 없는 빈은 건너뛴다.
	 */
	private Class<?> typeOf(String beanName) {
		try {
			Class<?> type = beanFactory.getType(beanName, false);
			return type == null ? null : ClassUtils.getUserClass(type);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
