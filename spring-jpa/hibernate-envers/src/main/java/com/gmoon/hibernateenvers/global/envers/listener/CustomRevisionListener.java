package com.gmoon.hibernateenvers.global.envers.listener;

import java.io.Serializable;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomRevisionListener implements EntityTrackingRevisionListener {

	@Override
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType,
		 Object revisionEntity) {
		log.debug("EntityTrackingRevisionListener entityChanged start...");
		try {
			RevisionHistory history = RevisionHistory.class.cast(revisionEntity);
			history.trace(entityClass, entityId, revisionType);
		} catch (Exception ex) {
			throw new RuntimeException("Dose not trace revision...", ex);
		}
	}

	@Override
	@TODO("Spring Security를 사용하여 회원의 저장")
	public void newRevision(Object revisionEntity) {
	}
}
