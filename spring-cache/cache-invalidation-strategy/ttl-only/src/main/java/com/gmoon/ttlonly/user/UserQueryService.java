package com.gmoon.ttlonly.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.ttlonly.config.CachePolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = CachePolicy.Name.USER, key = "#id")
	public UserView findById(Long id) {
		return userRepository.findById(id)
			 .map(UserView::from)
			 .orElse(null);
	}
}
