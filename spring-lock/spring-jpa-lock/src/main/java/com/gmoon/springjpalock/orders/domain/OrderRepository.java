package com.gmoon.springjpalock.orders.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Order, String> {

	@Lock(LockModeType.PESSIMISTIC_READ)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "300")})
	@Query("SELECT o FROM Order o WHERE o.no = ?1")
	Optional<Order> findByNoWithSharedLock(String no);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "300")})
	@Query("SELECT o FROM Order o WHERE o.no = ?1")
	Optional<Order> findByNoWithExclusiveLock(String no);

	@Query
	void increment(String no);
}
