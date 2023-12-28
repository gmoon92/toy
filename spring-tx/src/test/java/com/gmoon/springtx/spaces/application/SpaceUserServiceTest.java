package com.gmoon.springtx.spaces.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.global.Fixtures;
import com.gmoon.springtx.global.event.DeleteFavoriteEvent;

@Transactional
@SpringBootTest
@RecordApplicationEvents
class SpaceUserServiceTest {

	@Autowired
	private SpaceUserService service;

	@Autowired
	private ApplicationEvents events;

	@Test
	void delete() {
		String groupId = Fixtures.SPACE_ID;
		String userId = Fixtures.USER_ID;

		service.delete(groupId, userId);

		assertThat(events.stream(DeleteFavoriteEvent.class).count())
			.isOne();
	}
}
