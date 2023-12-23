package com.gmoon.springtx.spaces.application;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.spaces.domain.SpaceUser;
import com.gmoon.springtx.spaces.domain.SpaceUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserService {

	private final SpaceUserRepository spaceUserRepository;

	@Transactional
	public void delete(String space, String userId) {
		SpaceUser spaceUser = getSpaceUser(space, userId);
		spaceUser.getSpace().getSpaceUsers().size();

		spaceUserRepository.delete(spaceUser);
	}

	private SpaceUser getSpaceUser(String spaceId, String userId) {
		return spaceUserRepository.findBySpaceIdAndUserId(spaceId, userId)
			.orElseThrow(EntityNotFoundException::new);
	}
}
