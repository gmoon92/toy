package com.gmoon.hibernateenvers.revision.infra;

import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.exception.NotAuditedException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.revision.infra.exception.AuditedEntityNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuditedEntityRepositoryImpl implements AuditedEntityRepository {

	private final EntityManager entityManager;

	@Override
	public <T extends BaseEntity> Optional<T> find(
		 Class<T> entityClass,
		 Object entityId,
		 Long revisionNumber
	) {
		return Optional.ofNullable(get(entityClass, entityId, revisionNumber));
	}

	@Override
	public <T extends BaseEntity> T get(
		 Class<T> entityClass,
		 Object entityId,
		 Long revisionNumber
	) {
		return get(entityClass, entityId, revisionNumber, RevisionType.values());
	}

	private <T extends BaseEntity> T get(
		 Class<T> entityClass,
		 Object entityId,
		 Long revisionNumber,
		 RevisionType... revisionTypes
	) {
		if (revisionNumber > 0) {
			try {
				AuditReader auditReader = AuditReaderFactory.get(entityManager);
				AuditQuery query = auditReader.createQuery()
					 .forEntitiesModifiedAtRevision(entityClass, revisionNumber)
					 .add(AuditEntity.id().eq(entityId))
					 .add(AuditEntity.revisionType().in(revisionTypes));

				@SuppressWarnings({"unchecked", "cast"})
				T singleResult = (T)query.getSingleResult();
				return singleResult;
			} catch (AuditException | NonUniqueResultException | NoResultException e) {
				log.warn(
					 String.format("Not found audited entity revision: %d, entityId: %s"
							   + "%n entityClass: %s",
						  revisionNumber, entityId, entityClass), e);
				return null;
			} catch (Exception e) {
				throw new AuditedEntityNotFoundException(entityClass, entityId, revisionNumber, e);
			}
		}
		return null;
	}

	public <T extends BaseEntity> Optional<T> findAuditedEntity(
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
