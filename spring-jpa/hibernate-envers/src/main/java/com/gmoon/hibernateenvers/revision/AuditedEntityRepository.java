package com.gmoon.hibernateenvers.revision;

import java.util.Optional;

import org.hibernate.envers.RevisionType;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;

public interface AuditedEntityRepository {

	<T extends BaseTrackingEntity> Optional<T> findAuditedEntity(Class<T> entityClass, Object entityId,
		Long revisionNumber);

	<T extends BaseTrackingEntity> Optional<T> findAuditedEntity(Class<T> entityClass, Object entityId,
		Long revisionNumber, RevisionType revisionType);

	<T extends BaseTrackingEntity> Optional<T> findPreAuditedEntity(Class<T> entityClass, Object entityId,
		Long revisionNumber);
}
