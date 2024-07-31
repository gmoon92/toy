package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetadata;
import com.gmoon.dbrecovery.global.test.Fixtures;
import com.gmoon.dbrecovery.movies.domain.Coupon;
import com.gmoon.dbrecovery.movies.domain.Movie;
import com.gmoon.dbrecovery.movies.infra.JpaCouponRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gmoon.dbrecovery.movies.domain.QCoupon.coupon;
import static com.gmoon.dbrecovery.movies.domain.QMovie.movie;
import static com.gmoon.dbrecovery.movies.domain.QTicketOffice.ticketOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@Slf4j
class DataRecoveryTest {

	/**
	 * 일부 DDL 미지원
	 * https://jsqlparser.github.io/JSqlParser/unsupported.html
	 */
	@Test
	void ddl() {
		String sql = "alter table tb_coupon drop foreign key FKe5km00l69rfrxvx5um3y1klw1";

		assertThrowsExactly(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).Statement());
	}

	@Nested
	@DataRecovery
	@SpringBootTest
	class InitializeTest {

		@Autowired
		private RecoveryDatabaseProperties properties;

		@Test
		void properties() {
			log.info("properties: {}", properties);
			assertThat(properties.getSchema()).isNotBlank();
			assertThat(properties.getRecoverySchema()).isNotBlank();
		}

		/**
		 * <pre>
		 * +----------------+--------------------+----------------+------------------------+---------+
		 * |table_name      |table_key_column_name|ref_table_name  |ref_column_name		   |on_delete|
		 * +----------------+--------------------+----------------+------------------------+---------+
		 * |tb_ticket_office|id                  |null            |null                    |0        |
		 * |tb_ticket       |ticket_office_id    |tb_ticket_office|id                      |1        |
		 * |tb_ticket       |id                  |null            |null                    |0        |
		 * |tb_movie_ticket |ticket_id           |null            |null                    |0        |
		 * |tb_movie_ticket |movie_id            |tb_movie        |id                      |1        |
		 * |tb_movie_ticket |ticket_id           |tb_ticket       |id                      |1        |
		 * |tb_movie_ticket |movie_id            |null            |null                    |0        |
		 * |tb_movie        |ticket_office_id    |tb_ticket_office|id                      |1        |
		 * |tb_movie        |id                  |null            |null                    |0        |
		 * |tb_coupon       |movie_id            |tb_movie        |id                      |1        |
		 * |tb_coupon       |id                  |null            |null                    |0        |
		 * +----------------+--------------------+----------------+------------------------+---------+
		 * </pre>
		 */
		@Test
		void recoveryTable() {
			List<TableMetadata> metadata = List.of(
				 TableMetadata.builder().tableName("tb_ticket_office").tableKeyName("id").onDelete(0).build(),
				 TableMetadata.builder().tableName("tb_ticket").tableKeyName("ticket_office_id").referenceTableName("tb_ticket_office").referenceColumnName("id").onDelete(1).build(),
				 TableMetadata.builder().tableName("tb_ticket").tableKeyName("id").onDelete(0).build(),
				 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("ticket_id").onDelete(0).build(),
				 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("movie_id").referenceTableName("tb_movie").referenceColumnName("id").onDelete(1).build(),
				 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("ticket_id").referenceTableName("tb_ticket").referenceColumnName("id").onDelete(1).build(),
				 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("movie_id").onDelete(0).build(),
				 TableMetadata.builder().tableName("tb_movie").tableKeyName("ticket_office_id").referenceTableName("tb_ticket_office").referenceColumnName("id").onDelete(1).build(),
				 TableMetadata.builder().tableName("tb_movie").tableKeyName("id").onDelete(0).build(),
				 TableMetadata.builder().tableName("tb_coupon").tableKeyName("movie_id").referenceTableName("tb_movie").referenceColumnName("id").onDelete(1).build(),
				 TableMetadata.builder().tableName("tb_coupon").tableKeyName("id").onDelete(0).build()
			);

			RecoveryTable recoveryTable = new RecoveryTable(null, null);
			recoveryTable.initialize(metadata);

			assertThat(recoveryTable.getTableAll().size()).isEqualTo(5);
			assertThat(recoveryTable.getDeleteTables("tb_ticket_office")).containsOnly("tb_ticket", "tb_movie", "tb_movie_ticket", "tb_coupon");
			assertThat(recoveryTable.getDeleteTables("tb_ticket")).containsOnly("tb_movie_ticket");
			assertThat(recoveryTable.getDeleteTables("tb_movie")).containsOnly("tb_movie_ticket", "tb_coupon");
			assertThat(recoveryTable.getDeleteTables("tb_movie_ticket")).isEmpty();
			assertThat(recoveryTable.getDeleteTables("tb_coupon")).isEmpty();
		}
	}


	@Nested
	@SpringBootTest
	class CRUDTest {

		@Autowired
		private JpaCouponRepository repository;

		@Autowired
		private EntityManager em;

		@Autowired
		private JPAQueryFactory jpaQueryFactory;

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
