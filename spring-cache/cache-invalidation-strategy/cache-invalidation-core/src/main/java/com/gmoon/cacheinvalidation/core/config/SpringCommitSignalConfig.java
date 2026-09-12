package com.gmoon.cacheinvalidation.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.core.invalidation.CommitSignalSink;
import com.gmoon.cacheinvalidation.core.resilience.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.signal.SpringCommitSignalListener;

/**
 * 애플리케이션이 발행한 변경 이벤트를 무효화 신호로 삼는다.
 * <p>
 * JPA 를 거치지 않는 쓰기(JDBC, jOOQ, 외부 연동)까지 덮을 수 있는 대신,
 * 이벤트를 발행하는 코드가 누락되면 무효화도 함께 누락된다.
 */
@Configuration
public class SpringCommitSignalConfig {

	@Bean
	public SpringCommitSignalListener springCommitSignalListener(
		 CommitSignalSink sink,
		 InvalidationRecorder recorder
	) {
		return new SpringCommitSignalListener(sink, recorder);
	}
}
