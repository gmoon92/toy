package com.gmoon.springtx.favorites.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.favorites.domain.FavoriteRepository;
import com.gmoon.springtx.global.TransactionalUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;

	@Transactional
	public void delete(String userId) {
		TransactionalUtils.logging();
		favoriteRepository.delete(userId);
		TransactionalUtils.logging();

		throw new RuntimeException("강제 예외 발생");
	}
}
