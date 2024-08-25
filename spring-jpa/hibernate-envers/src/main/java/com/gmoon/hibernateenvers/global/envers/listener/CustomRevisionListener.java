package com.gmoon.hibernateenvers.global.envers.listener;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.revision.domain.Revision;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomRevisionListener implements EntityTrackingRevisionListener {

	@Override
	public void entityChanged(
		 Class entityClass,
		 String entityName,
		 Object entityId,
		 RevisionType revisionType,
		 Object revisionEntity
	) {
		log.debug("EntityTrackingRevisionListener entityChanged start...");
		try {
			Revision history = Revision.class.cast(revisionEntity);
			history.changeEntity(entityClass, entityId, revisionType);
		} catch (Exception ex) {
			throw new RuntimeException("Dose not trace revision...", ex);
		}
	}

	@TODO("Spring Security를 사용하여 회원의 저장")
	@Override
	public void newRevision(Object revisionEntity) {

	}
}
