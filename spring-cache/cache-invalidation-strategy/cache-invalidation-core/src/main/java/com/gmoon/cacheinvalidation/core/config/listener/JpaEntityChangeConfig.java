package com.gmoon.cacheinvalidation.core.config.listener;

import java.util.List;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.metrics.InvalidationRecorder;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

/**
 * 영속성 컨텍스트를 거친 변경을 무효화 신호로 삼는다.
 * <p>
 * 커밋 이후 실행이 보장되고 쓰기 경로에 캐시 코드가 남지 않는 대신,
 * 벌크 연산({@code @Modifying} JPQL, 네이티브 UPDATE)은 신호를 만들지 않는다.
 * <p>
 * 리스너는 스프링 빈이 아니다. 하이버네이트가 자기 {@link EventListenerRegistry} 에 담아
 * 수명을 관리하므로, 컨테이너에 중복으로 올릴 이유가 없다.
 */
@Configuration
@RequiredArgsConstructor
public class JpaEntityChangeConfig {

	private final ObjectProvider<EntityManagerFactory> entityManagerFactories;
	private final CacheInvalidator cacheInvalidator;
	private final InvalidationRecorder recorder;

	@PostConstruct
	public void registerListener() {
		List<EntityManagerFactory> targets = entityManagerFactories.stream().toList();
		if (targets.isEmpty()) {
			throw new IllegalStateException("No EntityManagerFactory found to register cache invalidation listener");
		}

		JpaEntityChangeListener listener = new JpaEntityChangeListener(cacheInvalidator, recorder);
		targets.forEach(factory -> appendTo(registryOf(factory), listener));
	}

	private void appendTo(EventListenerRegistry registry, JpaEntityChangeListener listener) {
		registry.appendListeners(EventType.POST_COMMIT_INSERT, listener);
		registry.appendListeners(EventType.POST_COMMIT_UPDATE, listener);
		registry.appendListeners(EventType.POST_COMMIT_DELETE, listener);
	}

	private EventListenerRegistry registryOf(EntityManagerFactory entityManagerFactory) {
		EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImpl.class)
			 .getServiceRegistry()
			 .getService(EventListenerRegistry.class);

		if (registry == null) {
			throw new IllegalStateException("EventListenerRegistry is not available");
		}
		return registry;
	}
}
