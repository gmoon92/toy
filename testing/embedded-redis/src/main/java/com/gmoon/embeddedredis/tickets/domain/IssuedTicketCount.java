package com.gmoon.embeddedredis.tickets.domain;

import static com.gmoon.embeddedredis.tickets.domain.IssuedTicketCount.*;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@RedisHash(value = CACHE_KEY_NAME)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class IssuedTicketCount implements Serializable {

	static final String CACHE_KEY_NAME = "issued-ticket-count";

	@Id
	@EqualsAndHashCode.Include
	private String ticketNo;

	private long count;

	@TimeToLive(unit = TimeUnit.MINUTES)
	private long ttl;

	protected IssuedTicketCount() {
		ttl = 5;
		count = 0;
	}

	@Builder
	public static IssuedTicketCount create(String ticketNo) {
		IssuedTicketCount count = new IssuedTicketCount();
		count.ticketNo = ticketNo;
		return count;
	}

	public void increment() {
		count++;
	}

	public void decrement() {
		count--;
	}
}
