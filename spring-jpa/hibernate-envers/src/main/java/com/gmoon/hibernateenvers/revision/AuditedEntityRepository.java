package com.gmoon.hibernateenvers.revision;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;

@Transactional(readOnly = true)
public interface AuditedEntityRepository {

	<T extends BaseTrackingEntity> Optional<T> find(Class<T> entityClass, Object entityId, Long revisionNumber);
	<T extends BaseTrackingEntity> T get(Class<T> entityClass, Object entityId, Long revisionNumber);
}
