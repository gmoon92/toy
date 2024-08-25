package com.gmoon.hibernateenvers.revision.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.test.RepositoryTest;

@RepositoryTest
class AuditedEntityRepositoryImplTest {

	@Autowired
	private AuditedEntityRepository repository;

	@Test
	void find() {
		Class<Member> entityClass = Member.class;
		Long memberId = 1L;
		long revisionNumber = 2L;

		assertThatCode(() -> repository.find(entityClass, memberId, revisionNumber))
			 .doesNotThrowAnyException();
	}

	@Test
	void get() {
		Class<Member> entityClass = Member.class;
		Long memberId = 1L;
		long revisionNumber = 2L;

		assertThatCode(() -> repository.get(entityClass, memberId, revisionNumber))
			 .doesNotThrowAnyException();
	}

	@Test
	void findAuditedEntity() {
		Class<Member> entityClass = Member.class;
		Long memberId = 1L;
		long revisionNumber = 2L;

		assertThatCode(() -> repository.findAuditedEntity(entityClass, memberId, revisionNumber))
			 .doesNotThrowAnyException();
	}
}
