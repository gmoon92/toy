package com.gmoon.writeinvalidate.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = UserCachePolicy.Name.USER, key = "#id")
	public CachedUser findById(Long id) {
		return userRepository.findById(id)
			 .map(CachedUser::from)
			 .orElse(null);
	}
}
