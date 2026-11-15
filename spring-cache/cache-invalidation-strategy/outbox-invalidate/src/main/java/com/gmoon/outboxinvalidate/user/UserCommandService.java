package com.gmoon.outboxinvalidate.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.outboxinvalidate.cache.EvictionOutbox;

import lombok.RequiredArgsConstructor;

/**
 * 변경과 무효화 지시를 같은 트랜잭션에 적는다. 지시를 브로커로 옮기고 캐시를 지우는 일은 이 서비스가 모른다.
 * <p>
 * 등록도 적는다. 커밋 전에 다른 트랜잭션이 조회하면 '없음' 이 캐시되어, 지울 옛 값이 생긴다.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;
	private final EvictionOutbox evictionOutbox;

	public Long register(String username, String email) {
		User user = userRepository.save(User.of(username, email));
		evictionOutbox.record(user);
		return user.getId();
	}

	public void changeEmail(Long id, String email) {
		User user = findUser(id);
		user.changeEmail(email);
		evictionOutbox.record(user);
	}

	public void unregister(Long id) {
		User user = findUser(id);
		userRepository.delete(user);
		evictionOutbox.record(user);
	}

	private User findUser(Long id) {
		return userRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
	}
}
