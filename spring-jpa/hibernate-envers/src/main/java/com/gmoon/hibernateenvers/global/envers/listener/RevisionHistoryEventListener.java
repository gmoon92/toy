package com.gmoon.hibernateenvers.global.envers.listener;

import static com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus.*;

import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.revision.AuditedEntityRepositoryImpl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDsl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDslImpl;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RevisionHistoryEventListener implements PostCommitInsertEventListener {

	private static RevisionHistoryDetailRepositoryQueryDsl getRevisionHistoryDetailRepositoryQueryDsl(EntityManager entityManager) {
		return new RevisionHistoryDetailRepositoryQueryDslImpl(entityManager);
	}

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

					Object auditedEntity = getAuditedEntity(entityManager, detail).get();
					updateStatus(entityManager, detail, auditedEntity);
				}
			});

		}
	}

	private void updateStatus(EntityManager entityManager, RevisionHistoryDetail detail, Object auditedEntity) {
		RevisionEventStatus eventStatus = ERROR;
		try {
			Optional<Object> maybePreAuditedEntity = getPreAuditedEntity(entityManager, detail);
			eventStatus = getEventStatus(detail.getTarget(), auditedEntity, maybePreAuditedEntity);
		} catch (Exception ex) {
			log.warn("Unexpected exception...", ex);
		} finally {
			updateEventStatus(entityManager, detail, eventStatus);
		}
	}

	private Optional<Object> getAuditedEntity(EntityManager entityManager, RevisionHistoryDetail detail) {
		RevisionTarget target = detail.getTarget();

		Long revisionNumber = detail.getRevision().getId();
		Class entityClass = target.getEntityClass();

		Object entityId = RevisionConverter.deSerializedObject(detail.getEntityId());
		return getAuditedEntityRepository(entityManager)
			 .findAuditedEntity(entityClass, entityId, revisionNumber);
	}

	private Optional<Object> getPreAuditedEntity(EntityManager entityManager, RevisionHistoryDetail detail) {
		RevisionTarget target = detail.getTarget();

		Long revisionNumber = detail.getRevision().getId();
		Class entityClass = target.getEntityClass();
		Object entityId = RevisionConverter.deSerializedObject(detail.getEntityId());
		return getAuditedEntityRepository(entityManager)
			 .findPreAuditedEntity(entityClass, entityId, revisionNumber);
	}

	private AuditedEntityRepositoryImpl getAuditedEntityRepository(EntityManager entityManager) {
		return new AuditedEntityRepositoryImpl(entityManager);
	}

	private boolean isNotChanged(RevisionTarget target, Object auditedEntity, Object anotherEntity) {
		Object currentVO = target.getCompareVO(auditedEntity);
		Object anotherVO = target.getCompareVO(anotherEntity);
		return currentVO.equals(anotherVO);
	}

	private RevisionEventStatus getEventStatus(
		 RevisionTarget target,
		 Object auditedEntity,
		 Optional<Object> maybePreAuditedEntity
	) {
		if (
			 maybePreAuditedEntity.isPresent()
			 && isNotChanged(target, auditedEntity, maybePreAuditedEntity.get())
		) {
			return UNCHANGED;
		} else {
			return RevisionEventStatus.DIRTY_CHECKING;// todo gmoon
		}
	}

	private void updateEventStatus(
		 EntityManager entityManager,
		 RevisionHistoryDetail detail,
		 RevisionEventStatus eventStatus
	) {
		EntityTransaction transaction = entityManager.getTransaction();
		Long id = detail.getId();
		try {
			transaction.begin();
			getRevisionHistoryDetailRepositoryQueryDsl(entityManager).updateEventStatus(id, eventStatus);
			transaction.commit();
		} catch (Exception ex) {
			String errorMessage = String.format(
				 "[Error] Update RevisionModifiedEntity id : %d, target : %s, entityId : %s",
				 id,
				 eventStatus
			);
			log.warn(errorMessage, ex);
			transaction.rollback();
		}
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
