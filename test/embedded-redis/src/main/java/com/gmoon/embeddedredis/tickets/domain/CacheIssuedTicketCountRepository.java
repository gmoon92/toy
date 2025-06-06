package com.gmoon.embeddedredis.tickets.domain;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface CacheIssuedTicketCountRepository extends CrudRepository<IssuedTicketCount, String> {

	@Cacheable(value = IssuedTicketCount.CACHE_KEY_NAME, unless = "#result==null")
	IssuedTicketCount getBy(String tickNo);
}
