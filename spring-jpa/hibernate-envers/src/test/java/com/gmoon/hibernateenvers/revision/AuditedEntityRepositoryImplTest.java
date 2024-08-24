package com.gmoon.hibernateenvers.revision;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.exception.NotAuditedException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;
import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.revision.exception.AuditedEntityNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@SpringBootTest
class AuditedEntityRepositoryImplTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void test() {
		Class<Member> entityClass = Member.class;
		Long memberId = 1L;
		long revisionNumber = 2L;

		assertThatCode(() -> find(entityClass, memberId, revisionNumber))
			 .doesNotThrowAnyException();
	}

	public <T extends BaseTrackingEntity> Optional<T> find(
		 Class<T> entityClass,
		 Object entityId,
		 Long revisionNumber
	) {
		try {
			AuditReader auditReader = AuditReaderFactory.get(entityManager);

			T auditedEntity = auditReader.find(entityClass, entityId, revisionNumber);
			return Optional.ofNullable(auditedEntity);
		} catch (IllegalArgumentException | NotAuditedException | IllegalStateException e) {
			log.warn(
				 String.format("Not found audited entity revision: %d, entityId: %s"
						   + "%n entityClass: %s",
					  revisionNumber, entityId, entityClass), e);
			return Optional.empty();
		} catch (Exception e) {
			throw new AuditedEntityNotFoundException(entityClass, entityId, revisionNumber, e);
		}
	}
}
