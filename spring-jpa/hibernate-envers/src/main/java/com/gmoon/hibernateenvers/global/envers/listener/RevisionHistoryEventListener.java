package com.gmoon.hibernateenvers.global.envers.listener;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.revision.domain.Revision;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistory;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryRepository;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;
import com.gmoon.hibernateenvers.revision.infra.AuditedEntityRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionHistoryEventListener implements PostCommitInsertEventListener {

	private final AuditedEntityRepository auditedEntityRepository;
	private final RevisionHistoryRepository revisionHistoryRepository;

	@Override
	public void onPostInsert(PostInsertEvent event) {
		log.debug("onPostInsert start...");
		Object entity = event.getEntity();

		if (entity instanceof RevisionHistory revisionHistory) {
			EventSource session = event.getSession();
			session.getActionQueue().registerProcess((success, sessionImplementor) -> {
				if (success) {
					SessionFactoryImplementor factory = sessionImplementor.getFactory();
					EntityManager entityManager = factory.createEntityManager();

					Revision revision = revisionHistory.getRevision();
					Long revisionNumber = revision.getId();
					Object auditedEntity = getAuditedEntity(revisionHistory, revisionNumber);
					updateStatus(entityManager, revisionHistory, auditedEntity);
				}
			});

		}
	}

	private void updateStatus(EntityManager entityManager, RevisionHistory revisionHistory, Object auditedEntity) {
		try {
			Revision revision = revisionHistory.getRevision();
			Long preRevisionNumber = revision.getId() - 1;
			Object preAuditedEntity = getAuditedEntity(revisionHistory, preRevisionNumber);

			RevisionStatus status = obtainStatus(revisionHistory.getTarget(), auditedEntity, preAuditedEntity);
			updateStatus(entityManager, revisionHistory, status);
		} catch (Exception ex) {
			log.warn("Unexpected exception...", ex);
			updateStatus(entityManager, revisionHistory, RevisionStatus.ERROR);
		}
	}

	private Object getAuditedEntity(
		 RevisionHistory revisionHistory,
		 Long revisionNumber
	) {
		RevisionTarget target = revisionHistory.getTarget();

		@SuppressWarnings({"all", "unchecked", "rawtypes"})
		Class<? extends BaseEntity> entityClass = target.getEntityClass();
		Object entityId = RevisionConverter.deSerializedObject(revisionHistory.getEntityId());
		return auditedEntityRepository.get(entityClass, entityId, revisionNumber);
	}

	private RevisionStatus obtainStatus(
		 RevisionTarget target,
		 Object auditedEntity,
		 Object preAuditedEntity
	) {
		if (preAuditedEntity != null
			 && isNotChanged(target, auditedEntity, preAuditedEntity)) {
			return RevisionStatus.UNCHANGED;
		} else {
			return RevisionStatus.DIRTY_CHECKING;
		}
	}

	private boolean isNotChanged(RevisionTarget target, Object auditedEntity, Object anotherAuditedEntity) {
		Object currentVO = target.getCompareVO(auditedEntity);
		Object anotherVO = target.getCompareVO(anotherAuditedEntity);
		return currentVO.equals(anotherVO);
	}

	private void updateStatus(
		 EntityManager entityManager,
		 RevisionHistory revisionHistory,
		 RevisionStatus status
	) {
		EntityTransaction transaction = entityManager.getTransaction();
		Long id = revisionHistory.getId();
		try {
			transaction.begin();
			revisionHistoryRepository.updateStatus(id, status);
			transaction.commit();
		} catch (Exception e) {
			log.warn(String.format("[Error] Update revision history: %d, status : %s", id,
				 status), e);
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
