package com.gmoon.springtx.spaces.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.global.event.DeleteFavoriteEvent;
import com.gmoon.springtx.spaces.domain.SpaceUser;
import com.gmoon.springtx.spaces.domain.SpaceUserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserService {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final SpaceUserRepository spaceUserRepository;

	@Transactional
	public void delete(String spaceId, String userId) {
		SpaceUser spaceUser = getSpaceUser(spaceId, userId);

		spaceUserRepository.delete(spaceUser);
		deleteFavorites(userId);
	}

	private SpaceUser getSpaceUser(String spaceId, String userId) {
		return spaceUserRepository.findBySpaceIdAndUserId(spaceId, userId)
			 .orElseThrow(EntityNotFoundException::new);
	}

	private void deleteFavorites(String userId) {
		applicationEventPublisher.publishEvent(new DeleteFavoriteEvent(this, userId));
	}
}
