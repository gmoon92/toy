package com.gmoon.aftercommitinvalidate.user;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.aftercommitinvalidate.cache.CacheEvictor;

import lombok.RequiredArgsConstructor;

/**
 * 바뀐 엔티티를 알리는 것까지가 쓰기의 책임이고, 지우는 것은 {@link CacheEvictor} 의 몫이다.
 * <p>
 * 등록도 알린다. 커밋 전에 다른 트랜잭션이 조회하면 '없음' 이 캐시되어, 지울 옛 값이 생긴다.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	public Long register(String username, String email) {
		User user = userRepository.save(User.of(username, email));
		eventPublisher.publishEvent(user);
		return user.getId();
	}

	public void changeEmail(Long id, String email) {
		User user = findUser(id);
		user.changeEmail(email);
		eventPublisher.publishEvent(user);
	}

	public void unregister(Long id) {
		User user = findUser(id);
		userRepository.delete(user);
		eventPublisher.publishEvent(user);
	}

	private User findUser(Long id) {
		return userRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
	}
}
