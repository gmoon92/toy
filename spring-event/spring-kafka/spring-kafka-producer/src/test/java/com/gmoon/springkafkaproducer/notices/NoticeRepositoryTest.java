package com.gmoon.springkafkaproducer.notices;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoticeRepositoryTest {

	@Autowired
	private NoticeRepository repository;

	@Test
	void increaseLikeCount() {
		repository.increaseLikeCount("tsid");
	}
}
