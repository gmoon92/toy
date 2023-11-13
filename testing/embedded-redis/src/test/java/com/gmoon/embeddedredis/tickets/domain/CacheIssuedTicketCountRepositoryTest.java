package com.gmoon.embeddedredis.tickets.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("just local test")
@SpringBootTest
class CacheIssuedTicketCountRepositoryTest {

	@Autowired
	private CacheIssuedTicketCountRepository repository;

	private String ticketNo;

	@BeforeEach
	void setUp() {
		ticketNo = "ticket-no1";
		IssuedTicketCount count = IssuedTicketCount.create(ticketNo);

		count.increment();
		repository.save(count);
	}

	@Test
	void getBy() {
		IssuedTicketCount actual = repository.getBy(ticketNo);

		assertThat(actual.getTicketNo()).isNotNull();
		assertThat(actual.getCount()).isEqualTo(1);
	}
}
