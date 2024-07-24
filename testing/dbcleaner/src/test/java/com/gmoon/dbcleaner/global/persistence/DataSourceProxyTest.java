package com.gmoon.dbcleaner.global.persistence;

import com.gmoon.dbcleaner.global.test.Fixtures;
import com.gmoon.dbcleaner.movies.domain.Coupon;
import com.gmoon.dbcleaner.movies.domain.Movie;
import com.gmoon.dbcleaner.movies.infra.JpaCouponRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.gmoon.dbcleaner.movies.domain.QCoupon.coupon;
import static com.gmoon.dbcleaner.movies.domain.QMovie.movie;
import static com.gmoon.dbcleaner.movies.domain.QTicketOffice.ticketOffice;

@Slf4j
@SpringBootTest
class DataSourceProxyTest {

	@Autowired
	private JpaCouponRepository repository;

	@Autowired
	private EntityManager em;

	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@SpyBean
	private DataSource dataSource;

	@Test
	@Transactional
	void test() throws SQLException {
		Connection conn = Mockito.mock(Connection.class);
		Mockito.doReturn(conn)
			 .when(dataSource)
			 .getConnection(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

		repository.findAll();

		Mockito.doNothing().when(conn).setAutoCommit(true);
//		Mockito.verify(conn).setAutoCommit(true);
//		Mockito.inOrder(conn).verify(conn, Mockito.calls(1)).setAutoCommit(true);
	}

	@Nested
	class CRUDTest {

		@Nested
		@Transactional
		class InsertStatement {

			@Test
			void nativeQuery() {
				em.createNativeQuery("INSERT INTO tb_coupon (id, movie_id, used) value (99, 1, 0)")
					 .executeUpdate();
			}

			@Test
			void entity() {
				Movie movie = Fixtures.newMovie(1L);
				Coupon coupon = Fixtures.newCoupon(movie);

				repository.save(coupon);
				repository.saveAll(List.of(
					 Fixtures.newCoupon(movie),
					 Fixtures.newCoupon(movie),
					 Fixtures.newCoupon(movie),
					 Fixtures.newCoupon(movie),
					 Fixtures.newCoupon(movie),
					 Fixtures.newCoupon(movie)
				));
				repository.flush();
			}

			/**
			 * QueryDsL
			 * <pre>
			 * 5.6 (INSERT-SELECT만 지원)
			 * <p>
			 * https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#_hql_syntax_for_insert
			 * 6.0(INSERT-SELECT 및 INSERT-VALUES 모두 지원)
			 * <p>
			 * https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#_hql_syntax_for_insert
			 * 이는 hibernate 6.0 미만 버전에서 Querydsl을 사용하는 경우 불가피한 문제입니다.
			 * <p>
			 * hibernate 버전을 업그레이드할 수 없는 경우, 다른 INSERT 방법을 사용하는 것이 좋습니다.
			 * </pre>
			 *
			 * @link {https://github.com/querydsl/querydsl/issues/3027}
			 */
			@Disabled("하이버네이트 버전 문제로 disabled")
			@Test
			@Transactional
			void queryDsl() {
				jpaQueryFactory
					 .insert(coupon)
//					 .columns(coupon.id, coupon.movie.id, coupon.used)
//					 .values(99L, 1L, 0)
//					 .set(coupon.movie.id, 1L)
					 .set(coupon.movie.id,
						  JPAExpressions.select(movie.id)
							   .from(movie)
							   .where(movie.id.eq(1L))
					 )
					 .set(coupon.used, false)
					 .execute();

			}
		}

		@Nested
		class SelectStatement {

			@Test
			void nativeQuery() {
				em.createNativeQuery("SELECT * FROM tb_coupon")
					 .getResultList();
			}

			@Test
			void entity() {
				repository.findAll();
				repository.findById(1L);
				repository.flush();
			}

			@Test
			void queryDsl() {
				jpaQueryFactory.selectFrom(ticketOffice).fetch();
				jpaQueryFactory.selectFrom(ticketOffice).where(ticketOffice.id.eq(1L)).fetch();
			}
		}

		@Nested
		@Transactional
		class UpdateStatement {

			@Test
			void nativeQuery() {
				em.createNativeQuery("UPDATE tb_coupon SET used = 1 WHERE id = 1")
					 .executeUpdate();
			}

			@Test
			void entity() {
				Coupon dirtyChecked = repository.findById(1L).get();
				dirtyChecked.using();
				repository.flush();

				Coupon coupon = repository.findById(2L).get();
				coupon.using();
				repository.saveAndFlush(coupon);
			}

			@Test
			void queryDsl() {
				jpaQueryFactory.update(coupon).set(coupon.used, false).execute();
				jpaQueryFactory.update(coupon).set(coupon.used, true).where(coupon.id.eq(1L)).execute();
			}
		}

		@Nested
		@Transactional
		class DeleteStatement {

			@Test
			void nativeQuery() {
				em.createNativeQuery("DELETE FROM tb_coupon")
					 .executeUpdate();
			}

			@Test
			void entity() {
				repository.deleteAll();
				repository.deleteById(1L);
				repository.flush();
			}

			@Test
			void queryDsl() {
				jpaQueryFactory.delete(ticketOffice).execute();
			}
		}
	}
}
