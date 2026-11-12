package com.gmoon.writeinvalidate.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.writeinvalidate.config.CachePolicy;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserRepository userRepository;

	@Cacheable(cacheNames = CachePolicy.Name.USER, key = "#id")
	public UserView findById(Long id) {
		return userRepository.findById(id)
			 .map(UserView::from)
			 .orElse(null);
	}
}
