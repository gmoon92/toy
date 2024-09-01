package com.gmoon.springjpalock.orders.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

	/**
	 * @implNote @Query 어노테이션을 통해 작성된 변경이 일어나는 쿼리(INSERT, DELETE, UPDATE )를 실행할 때 사용
	 * */
	@Modifying
	@Query("UPDATE Order o SET o.issuedCount = o.issuedCount +1 WHERE o.no = :no")
	void incrementIssuedCount(String no);
}
