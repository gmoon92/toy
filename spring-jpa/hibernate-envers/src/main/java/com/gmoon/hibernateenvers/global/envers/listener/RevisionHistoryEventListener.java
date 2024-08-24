package com.gmoon.hibernateenvers.global.envers.listener;

import static com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus.*;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.revision.AuditedEntityRepository;
import com.gmoon.hibernateenvers.revision.AuditedEntityRepositoryImpl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDsl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDslImpl;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistory;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RevisionHistoryEventListener implements PostCommitInsertEventListener {

	@Override
	public void onPostInsert(PostInsertEvent event) {
		log.debug("onPostInsert start...");
		Object entity = event.getEntity();

		if (entity instanceof RevisionHistoryDetail detail) {
			EventSource session = event.getSession();
			session.getActionQueue().registerProcess((success, sessionImplementor) -> {
				if (success) {
					SessionFactoryImplementor factory = sessionImplementor.getFactory();
					EntityManager entityManager = factory.createEntityManager();

					RevisionHistory revision = detail.getRevision();
					Long revisionNumber = revision.getId();
					Object auditedEntity = getAuditedEntity(entityManager, detail, revisionNumber);
					updateStatus(entityManager, detail, auditedEntity);
				}
			});

		}
	}

	private void updateStatus(EntityManager entityManager, RevisionHistoryDetail detail, Object auditedEntity) {
		try {
			RevisionHistory revision = detail.getRevision();
			Long preRevisionNumber = revision.getId() - 1;
			Object preAuditedEntity = getAuditedEntity(entityManager, detail, preRevisionNumber);

			RevisionEventStatus eventStatus = getEventStatus(detail.getTarget(), auditedEntity, preAuditedEntity);
			updateEventStatus(entityManager, detail, eventStatus);
		} catch (Exception ex) {
			log.warn("Unexpected exception...", ex);
			updateEventStatus(entityManager, detail, ERROR);
		}
	}

	private Object getAuditedEntity(
		 EntityManager entityManager,
		 RevisionHistoryDetail detail,
		 Long revisionNumber
	) {
		RevisionTarget target = detail.getTarget();

		@SuppressWarnings({"all", "unchecked", "rawtypes"})
		Class<? extends BaseTrackingEntity> entityClass = target.getEntityClass();
		Object entityId = RevisionConverter.deSerializedObject(detail.getEntityId());
		AuditedEntityRepository auditedEntityRepository = getAuditedEntityRepository(entityManager);
		return auditedEntityRepository.get(entityClass, entityId, revisionNumber);
	}

	private RevisionEventStatus getEventStatus(
		 RevisionTarget target,
		 Object auditedEntity,
		 Object preAuditedEntity
	) {
		if (preAuditedEntity != null
			 && isNotChanged(target, auditedEntity, preAuditedEntity)) {
			return UNCHANGED;
		} else {
			return RevisionEventStatus.DIRTY_CHECKING;
		}
	}

	private boolean isNotChanged(RevisionTarget target, Object auditedEntity, Object anotherAuditedEntity) {
		Object currentVO = target.getCompareVO(auditedEntity);
		Object anotherVO = target.getCompareVO(anotherAuditedEntity);
		return currentVO.equals(anotherVO);
	}

	private void updateEventStatus(
		 EntityManager entityManager,
		 RevisionHistoryDetail detail,
		 RevisionEventStatus eventStatus
	) {
		EntityTransaction transaction = entityManager.getTransaction();
		Long detailId = detail.getId();
		try {
			transaction.begin();
			getRevisionHistoryDetailRepositoryQueryDsl(entityManager).updateEventStatus(detailId, eventStatus);
			transaction.commit();
		} catch (Exception e) {
			log.warn(String.format("[Error] Update revision detail status detailId: %d, status : %s", detailId,
				 eventStatus), e);
			transaction.rollback();
		}
	}

	private AuditedEntityRepository getAuditedEntityRepository(
		 EntityManager entityManager
	) {
		return new AuditedEntityRepositoryImpl(entityManager);
	}

	private RevisionHistoryDetailRepositoryQueryDsl getRevisionHistoryDetailRepositoryQueryDsl(
		 EntityManager entityManager
	) {
		return new RevisionHistoryDetailRepositoryQueryDslImpl(entityManager);
	}

	@Override
	public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
		return true;
	}

	@Override
	public void onPostInsertCommitFailed(PostInsertEvent event) {
		// ignore.
	}
}
