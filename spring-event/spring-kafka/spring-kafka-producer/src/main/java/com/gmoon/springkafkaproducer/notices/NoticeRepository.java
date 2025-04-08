package com.gmoon.springkafkaproducer.notices;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, String> {

}
