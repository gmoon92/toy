package com.gmoon.writeupdate.user;

import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.writeupdate.config.CachePolicy;

import lombok.RequiredArgsConstructor;

/**
 * 바뀐 값을 지우지 않고 캐시에 덮어쓴다.
 * <p>
 * {@link CachePut} 은 메서드가 정상 반환한 값을 적재하므로, 쓰기 메서드가 조회 모델 {@link UserView} 를 만들어 돌려준다.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;

	public Long register(String username, String email) {
		return userRepository.save(User.of(username, email)).getId();
	}

	@CachePut(cacheNames = CachePolicy.Name.USER, key = "#id")
	public UserView changeEmail(Long id, String email) {
		User user = findUser(id);
		user.changeEmail(email);
		return UserView.from(user);
	}

	private User findUser(Long id) {
		return userRepository.findById(id)
			 .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
	}
}
