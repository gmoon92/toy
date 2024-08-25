package com.gmoon.hibernateenvers.revision.infra;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;

@Transactional(readOnly = true)
public interface AuditedEntityRepository {

	<T extends BaseEntity> Optional<T> find(Class<T> entityClass, Object entityId, Long revisionNumber);

	<T extends BaseEntity> T get(Class<T> entityClass, Object entityId, Long revisionNumber);

	<T extends BaseEntity> Optional<T> findAuditedEntity(Class<T> entityClass, Object entityId, Long revisionNumber);
}
