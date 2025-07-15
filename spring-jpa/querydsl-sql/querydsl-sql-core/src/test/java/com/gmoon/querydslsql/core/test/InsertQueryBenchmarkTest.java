package com.gmoon.querydslsql.core.test;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.hibernate.query.sqm.InterpretationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.querydslsql.core.points.PointTransaction;
import com.gmoon.querydslsql.core.points.QPointTransaction;
import com.gmoon.querydslsql.querydslsql.QTbPointTransaction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;

import jakarta.persistence.EntityManager;

/**
 * <pre>
 *   Hibernate batch insert 활성화
 * & JDBC rewriteBatchedStatements 활성화
 *
 * Hibernate Batch insert
 * - hibernate.batch_size: JDBC 배치 구문수 (addBatch 수)
 * - hibernate.order_inserts: INSERT 구문 정렬 여부
 *
 * </pre>
 */
@DisplayName("전체 빌드 시간 단축을 위해 5만건 -> 500건으로 검증 데이터 축소")
@Transactional
@SpringBootTest(properties = {
	 "spring.main.lazy-initialization=true",

	 "hibernate.jdbc_batch=50",
	 "hibernate.order_inserts=true"
})
class InsertQueryBenchmarkTest {
	private final int total = 500;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JPAQueryFactory queryFactory;

	@Autowired
	private SQLQueryFactory sqlQueryFactory;

	@AfterEach
	void tearDown() {
		flushAndClear();
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

	@DisplayName("entityManager merge 26s (upsert select-insert)")
	@Test
	void merge() {
		assertThatCode(() ->
			 chunkInsert(transactions -> {
				 for (PointTransaction PointTransaction : transactions) {
					 entityManager.merge(PointTransaction);
				 }
			 })
		).doesNotThrowAnyException();
	}

	@DisplayName("entityManager persist 1s")
	@Test
	void persist() {
		assertThatCode(() ->
			 chunkInsert(transactions -> {
				 for (PointTransaction transaction : transactions) {
					 entityManager.persist(transaction);
				 }
			 })
		).doesNotThrowAnyException();
	}

	@DisplayName("jdbc batchInsert 660ms")
	@Test
	void jdbcBatchInsert() {
		assertThatCode(() ->
			 chunkInsert(transactions -> {
				 String sql =
					  "INSERT INTO tb_point_transaction ("
						   + "id, user_id, amount, type, transaction_at"
						   + ") VALUES ("
						   + "?,?,?,?,?"
						   + ")";

				 jdbcTemplate.batchUpdate(sql, transactions, transactions.size(), (ps, entity) -> {
					 ps.setString(1, entity.getId());
					 ps.setString(2, entity.getUserId());
					 ps.setInt(3, entity.getAmount());
					 ps.setString(4, entity.getType());
					 ps.setTimestamp(5, Timestamp.from(entity.getTransactionAt().toInstant(ZoneOffset.UTC)));
				 });
			 })
		).doesNotThrowAnyException();
	}

	/**
	 * @see <a href="http://querydsl.com/static/querydsl/3.7.2/reference/ko-KR/html/ch02s03.html">DMLClause</a>
	 */
	@DisplayName("QueryDSL SQL - single-row insert 26s")
	@Test
	void querydslSql() {
		assertThatCode(() ->
			 chunkInsert(transactions -> {
				 QTbPointTransaction qPointTransaction = QTbPointTransaction.tbPointTransaction;
				 for (PointTransaction pointTransaction : transactions) {
					 sqlQueryFactory.insert(qPointTransaction)
						  .columns(
							   qPointTransaction.id,
							   qPointTransaction.userId,
							   qPointTransaction.amount,
							   qPointTransaction.type,
							   qPointTransaction.transactionAt
						  )
						  .values(
							   pointTransaction.getId(),
							   pointTransaction.getUserId(),
							   pointTransaction.getAmount(),
							   pointTransaction.getType(),
							   pointTransaction.getTransactionAt()
						  )
						  .execute();
				 }
			 })
		).doesNotThrowAnyException();

	}

	@DisplayName("QueryDSL SQL - multi-row insert 857ms")
	@Test
	void querydslSqlBatchInsert() {
		assertThatCode(() ->
			 chunkInsert(transactions -> {
				 QTbPointTransaction qPointTransaction = QTbPointTransaction.tbPointTransaction;
				 SQLInsertClause insert = sqlQueryFactory.insert(qPointTransaction);
				 for (PointTransaction pointTransaction : transactions) {
					 insert
						  .set(qPointTransaction.id, pointTransaction.getId())
						  .set(qPointTransaction.userId, pointTransaction.getUserId())
						  .set(qPointTransaction.amount, pointTransaction.getAmount())
						  .set(qPointTransaction.type, pointTransaction.getType())
						  .set(qPointTransaction.transactionAt, Timestamp.from(pointTransaction.getTransactionAt().toInstant(ZoneOffset.UTC)))
						  .addBatch();
				 }
				 insert.execute();
			 })
		).doesNotThrowAnyException();

	}

	@DisplayName("QueryDSL JPA - JPQL 에서 insert ... values 미지원")
	@Test
	void querydslJpa() {
		assertThatThrownBy(() ->
			 chunkInsert(transactions -> {
				 for (PointTransaction entity : transactions) {
					 QPointTransaction qDomain = QPointTransaction.pointTransaction;
					 queryFactory
						  .insert(qDomain)
						  .columns(
							   qDomain.id,
							   qDomain.userId,
							   qDomain.amount,
							   qDomain.type,
							   qDomain.transactionAt
						  )
						  .values(
							   1, entity.getId(),
							   2, entity.getUserId(),
							   3, entity.getAmount(),
							   4, entity.getType(),
							   5, entity.getTransactionAt()
						  )
						  .execute();
				 }
			 })
		).isInstanceOf(InterpretationException.class);
	}

	private void chunkInsert(Consumer<List<PointTransaction>> consumer) {
		int chunk = 500;
		for (int i = 0; i < total; i += chunk) {
			int end = Math.min(i + chunk, total);
			List<PointTransaction> PointTransactions = IntStream.range(i, end)
				 .mapToObj(amount -> new PointTransaction(
					  "uuid-user-id01",
					  amount,
					  "free"
				 ))
				 .toList();
			consumer.accept(PointTransactions);
		}
	}
}
