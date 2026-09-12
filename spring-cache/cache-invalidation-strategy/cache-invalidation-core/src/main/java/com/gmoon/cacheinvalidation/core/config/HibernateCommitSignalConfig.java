package com.gmoon.cacheinvalidation.core.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.core.invalidation.CommitSignalSink;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.signal.HibernateCommitSignalListener;
import com.gmoon.cacheinvalidation.core.signal.HibernateCommitSignalRegistrar;

import jakarta.persistence.EntityManagerFactory;

/**
 * 영속성 컨텍스트를 거친 변경을 무효화 신호로 삼는다.
 * <p>
 * 커밋 이후 실행이 보장되고 쓰기 경로에 캐시 코드가 남지 않는 대신,
 * 벌크 연산({@code @Modifying} JPQL, 네이티브 UPDATE)은 신호를 만들지 않는다.
 */
@Configuration
public class HibernateCommitSignalConfig {

	@Bean
	public HibernateCommitSignalListener hibernateCommitSignalListener(
		 CommitSignalSink sink,
		 InvalidationRecorder recorder
	) {
		return new HibernateCommitSignalListener(sink, recorder);
	}

	@Bean
	public HibernateCommitSignalRegistrar hibernateCommitSignalRegistrar(
		 ObjectProvider<EntityManagerFactory> entityManagerFactories,
		 HibernateCommitSignalListener listener
	) {
		return new HibernateCommitSignalRegistrar(entityManagerFactories, listener);
	}
}
