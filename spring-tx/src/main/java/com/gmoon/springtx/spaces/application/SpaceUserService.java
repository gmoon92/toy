package com.gmoon.springtx.spaces.application;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.favorites.application.FavoriteService;
import com.gmoon.springtx.spaces.domain.SpaceUser;
import com.gmoon.springtx.spaces.domain.SpaceUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserService {

	private final SpaceUserRepository spaceUserRepository;
	private final FavoriteService favoriteService;

	@Lazy
	@Autowired
	private SpaceUserService self;

	@Transactional
	public void delete(String spaceId, String userId) {
		SpaceUser spaceUser = getSpaceUser(spaceId, userId);

		spaceUserRepository.delete(spaceUser);
		self.deleteFavorites(userId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteFavorites(String userId) {
		try {
			favoriteService.delete(userId);
		} catch (RuntimeException e) {
			log.warn("", e);
		}
	}

	private SpaceUser getSpaceUser(String spaceId, String userId) {
		return spaceUserRepository.findBySpaceIdAndUserId(spaceId, userId)
			.orElseThrow(EntityNotFoundException::new);
	}
}
