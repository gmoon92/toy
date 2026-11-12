package com.gmoon.aftercommitinvalidate.user;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.gmoon.aftercommitinvalidate.cache.CacheEvictor;

import lombok.RequiredArgsConstructor;

/**
 * 스케줄러·배치처럼 호출자가 트랜잭션을 열어 주지 않는 경로.
 * <p>
 * 쓰기 코드와 발행 코드는 {@link UserCommandService} 와 같지만
 * 트랜잭션이 없으므로 {@link CacheEvictor} 가 호출되지 않는다.
 * 같은 호출을 트랜잭션으로 감싸면 무효화된다.
 */
@Service
@RequiredArgsConstructor
public class NonTransactionalUserService {

	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void changeEmail(Long id, String email) {
		User user = userRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

		user.changeEmail(email);
		userRepository.save(user);
		eventPublisher.publishEvent(user);
	}
}
