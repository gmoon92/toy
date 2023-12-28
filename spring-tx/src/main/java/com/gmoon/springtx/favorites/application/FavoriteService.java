package com.gmoon.springtx.favorites.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.favorites.domain.FavoriteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;

	@Transactional
	public void delete(String userId) {
		favoriteRepository.delete(userId);
		throw new RuntimeException("강제 예외 발생");
	}
}
