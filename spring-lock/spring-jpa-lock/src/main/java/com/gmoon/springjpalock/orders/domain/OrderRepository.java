package com.gmoon.springjpalock.orders.domain;

import java.util.Optional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

public interface OrderRepository extends JpaRepository<Order, String> {

	@Transactional(readOnly = true)
	@Lock(LockModeType.PESSIMISTIC_READ)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "300")})
	@Query("SELECT o FROM Order o WHERE o.no = ?1")
	Optional<Order> findByNoWithSharedLock(String no);

	@Transactional(readOnly = true)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "300")})
	@Query("SELECT o FROM Order o WHERE o.no = ?1")
	Optional<Order> findByNoWithExclusiveLock(String no);
}
