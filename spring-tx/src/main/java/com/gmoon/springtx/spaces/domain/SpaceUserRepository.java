package com.gmoon.springtx.spaces.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SpaceUserRepository extends JpaRepository<SpaceUser, String> {

	Optional<SpaceUser> findBySpaceIdAndUserId(String spaceId, String userId);
}
