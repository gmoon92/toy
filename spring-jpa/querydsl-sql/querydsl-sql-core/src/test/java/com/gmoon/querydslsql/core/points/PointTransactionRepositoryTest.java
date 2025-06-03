package com.gmoon.querydslsql.core.points;

import com.gmoon.querydslsql.core.config.QueryDslSqlConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import({
	 QueryDslSqlConfig.class,
	 PointTransactionRepository.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PointTransactionRepositoryTest {

	@Autowired
	private PointTransactionRepository repository;

	@Test
	void bulkInsert() {
		repository.bulkInsert(List.of(
			 new PointTransaction("gmoon01", 1000, "free"),
			 new PointTransaction("gmoon02", 1000, "free")
		));
	}
}
