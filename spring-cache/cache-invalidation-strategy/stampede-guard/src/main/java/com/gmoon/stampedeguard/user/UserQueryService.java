package com.gmoon.stampedeguard.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.stampedeguard.config.CachePolicy;

import lombok.RequiredArgsConstructor;

/**
 * 같은 키에 MISS 가 몰려도 DB 는 한 요청만 읽는다.
 * <p>
 * {@code sync = true} 는 힌트일 뿐이다. 스프링은 {@code Cache#get(key, loader)} 를 부르기만 하고,
 * 실제로 막는 것은 Redis 캐시 writer 의 락이다({@link com.gmoon.stampedeguard.config.CacheConfig}).
 * 락 없는 writer 에서는 이 속성이 아무것도 막지 않는다.
 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserRepository userRepository;

	@Cacheable(cacheNames = CachePolicy.Name.USER, key = "#id", sync = true)
	public UserView findById(Long id) {
		return userRepository.findById(id)
			 .map(UserView::from)
			 .orElse(null);
	}
}
