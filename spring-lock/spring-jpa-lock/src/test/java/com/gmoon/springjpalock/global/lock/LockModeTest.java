package com.gmoon.springjpalock.global.lock;

import com.gmoon.springjpalock.global.AbstractJpaRepositoryTest;
import com.gmoon.springjpalock.global.Fixtures;
import com.gmoon.springjpalock.orders.domain.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class LockModeTest extends AbstractJpaRepositoryTest {

	@DisplayName("@Version 은 강제로 증가시킬 수 없다.")
	@Test
	void checkVersion() {
		/**
		 * [QUERY] select * from `tb_order` where `no`='order-no-001'
		 * [QUERY] update `tb_order` set `version`=2 where `no`='order-no-001' and `version`=1
		 * [QUERY] commit
		 * */
		executeQuery(em -> em.find(Order.class, Fixtures.ORDER_NO, LockModeType.OPTIMISTIC_FORCE_INCREMENT),
			 RuntimeException::new);

		/**
		 * [QUERY] select * from `tb_order` where `no`='order-no-001'
		 * [QUERY] select `version` as version_ from `tb_order` where `no` ='order-no-001'
		 * [QUERY] commit
		 * */
		executeQuery(em -> em.find(Order.class, Fixtures.ORDER_NO, LockModeType.OPTIMISTIC), RuntimeException::new);
	}

	@DisplayName("낙관적 락 모드 유형")
	@Nested
	class OptimisticLockModeVersionText {

		@DisplayName("OPTIMISTIC 모드는 Update Query 시점에 증가 시킨다."
			 + "[QUERY] SET autocommit=0"
			 + "[QUERY] select * from `tb_order` order0_ where `no`='order-no-001'"
			 + "merge entity"
			 + "increase version 1 -> 2"
			 + "[QUERY] update `tb_order` set `address`='Seoul', `issued_count`=0, `status`='WAITING', `version`=2 where `no`='order-no-001' and `version`=1"
			 + "[QUERY] commit")
		@Test
		void optimistic() {
			long version10 = 10L;
			executeQuery(em -> {
				// default LockModeType.OPTIMISTIC
				Order order = em.find(Order.class, Fixtures.ORDER_NO);
				assertThat(order.getVersion()).isNotNull();
				assertThat(em.getLockMode(order)).isEqualTo(LockModeType.OPTIMISTIC);

				// version 강제 증가 시도
				order.setVersion(version10);

				// update "tb_order" set "address"=?, "status"=?, "version"=? where "no"=? and "version"=?
				return em.merge(order);
			}, RuntimeException::new);

			EntityManager em = factory.createEntityManager();
			assertThat(em.find(Order.class, Fixtures.ORDER_NO).getVersion())
				 .isNotEqualTo(version10);
		}

		@DisplayName("OPTIMISTIC_FORCE_INCREMENT 유형은 commit 시점에 @Version을 증가시킨다."
			 + "일반적으로 어플리케이션의 트랜잭션 범위는 서비스 계층으로 분리되어 있기 때문에 논리적인 버전 관리를 할 수 있다."
			 + "[QUERY] SET autocommit=0"
			 + "[QUERY] select * from `tb_order` where `no`='order-no-001'"
			 + "merge entity"
			 + "increase version 1 -> 2"
			 + "[QUERY] update `tb_order` set `address`='Seoul', `issued_count`=0, `status`='WAITING', `version`=2 where `no`='order-no-001' and `version`=1"
			 + "increase version 2 -> 3"
			 + "[QUERY] update `tb_order` set `version`=3 where `no`='order-no-001' and `version`=2 "
			 + "[QUERY] commit")
		@Test
		void optimisticForceIncrement() {
			long version10 = 10L;
			executeQuery(em -> {
				Order order = em.find(Order.class, Fixtures.ORDER_NO, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
				assertThat(order.getVersion()).isNotNull();
				assertThat(em.getLockMode(order)).isEqualTo(LockModeType.OPTIMISTIC_FORCE_INCREMENT);

				// version 강제 증가 시도
				order.setVersion(version10);
				return em.merge(order);
			}, RuntimeException::new);

			EntityManager em = factory.createEntityManager();
			assertThat(em.find(Order.class, Fixtures.ORDER_NO).getVersion())
				 .isNotEqualTo(version10);
		}
	}
}
