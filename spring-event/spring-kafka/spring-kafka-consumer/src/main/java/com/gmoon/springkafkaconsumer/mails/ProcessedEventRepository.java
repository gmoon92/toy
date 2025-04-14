package com.gmoon.springkafkaconsumer.mails;

import org.springframework.stereotype.Repository;

@Repository
public class ProcessedEventRepository {

	public boolean exists(String id) {
		return true;
	}

	public void save(ProcessedEvent processedEvent) {

	}
}
