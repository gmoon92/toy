package com.gmoon.springkafkaproducer.notices;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, String> {

	@Modifying
	@Query("UPDATE Notice n SET n.likeCount = n.likeCount+1 WHERE n.id = :id")
	void increaseLikeCount(String id);
}
