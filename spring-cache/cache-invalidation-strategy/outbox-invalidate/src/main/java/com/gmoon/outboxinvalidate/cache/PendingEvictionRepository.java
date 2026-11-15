package com.gmoon.outboxinvalidate.cache;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingEvictionRepository extends JpaRepository<PendingEviction, Long> {

	List<PendingEviction> findAllByOrderByIdAsc(Limit limit);
}
