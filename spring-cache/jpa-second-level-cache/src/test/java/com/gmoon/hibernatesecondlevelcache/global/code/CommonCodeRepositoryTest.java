package com.gmoon.hibernatesecondlevelcache.global.code;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectDelete;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ExtendWith(QuickPerfTestExtension.class)
@Import(QuickPerfSqlConfig.class)
@Transactional
class CommonCodeRepositoryTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private CommonCodeRepository repository;

	@DisplayName("@Immutable CRUD 검증")
	@Nested
	class ImmutableTest {

		@DisplayName("저장 가능")
		@Test
		@ExpectInsert
		void save() {
			CommonCode newCode = CommonCode.builder()
				.code("G1")
				.value("new value")
				.build();

			repository.saveAndFlush(newCode);
		}

		@DisplayName("수정 불가")
		@Test
		@ExpectUpdate(0)
		void merge() {
			String id = "c1";

			CommonCode commonCode = CommonCode.builder()
				.code(id)
				.value("change value")
				.build();
			repository.saveAndFlush(commonCode);

			flushAndClear();
		}

		@DisplayName("삭제 가능")
		@Test
		@ExpectDelete
		void delete() {
			String id = "c1";

			repository.deleteById(id);
			flushAndClear();
		}
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
