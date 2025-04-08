package com.gmoon.springkafkaproducer.messages;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxRepository extends JpaRepository<OutboxMessage, String> {

}
