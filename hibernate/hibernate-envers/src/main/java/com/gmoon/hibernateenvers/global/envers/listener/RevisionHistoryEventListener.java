package com.gmoon.hibernateenvers.global.envers.listener;

import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.revision.AuditedEntityRepository;
import com.gmoon.hibernateenvers.revision.AuditedEntityRepositoryImpl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDsl;
import com.gmoon.hibernateenvers.revision.RevisionHistoryDetailRepositoryQueryDslImpl;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Optional;

import static com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus.ERROR;
import static com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus.UNCHANGED;

@Slf4j
public class RevisionHistoryEventListener implements PostCommitInsertEventListener {

  private final EntityManager entityManager;

  private AuditedEntityRepository auditedEntityRepository;

  private RevisionHistoryDetailRepositoryQueryDsl historyDetailRepositoryQueryDsl;

  public RevisionHistoryEventListener(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.auditedEntityRepository = new AuditedEntityRepositoryImpl(entityManager);
    this.historyDetailRepositoryQueryDsl = new RevisionHistoryDetailRepositoryQueryDslImpl(entityManager);
  }

  @Override
  public void onPostInsert(PostInsertEvent event) {
    log.debug("onPostInsert start...");
    Object entity = event.getEntity();

    if (entity instanceof RevisionHistoryDetail) {
      RevisionHistoryDetail detail = RevisionHistoryDetail.class.cast(entity);
      Object auditedEntity = getAuditedEntity(detail).get();

      RevisionEventStatus eventStatus = ERROR;
      try {
        Optional<Object> maybePreAuditedEntity = getPreAuditedEntity(detail);
        eventStatus = getEventStatus(detail.getRevisionTarget(), auditedEntity, maybePreAuditedEntity);
      } catch (Exception ex) {
        log.warn("Unexpected exception...", ex);
      } finally {
        updateEventStatus(detail, eventStatus);
      }
    }
  }

  private Optional<Object> getAuditedEntity(RevisionHistoryDetail detail) {
    RevisionTarget target = detail.getRevisionTarget();

    Long revisionNumber = detail.getRevision().getId();
    Class entityClass = target.getEntityClass();
    Object entityId = RevisionConverter.deSerializedObject(detail.getEntityId());
    return auditedEntityRepository.findAuditedEntity(entityClass, entityId, revisionNumber);
  }

  private Optional<Object> getPreAuditedEntity(RevisionHistoryDetail detail) {
    RevisionTarget target = detail.getRevisionTarget();

    Long revisionNumber = detail.getRevision().getId();
    Class entityClass = target.getEntityClass();
    Object entityId = RevisionConverter.deSerializedObject(detail.getEntityId());
    return auditedEntityRepository.findPreAuditedEntity(entityClass, entityId, revisionNumber);
  }

  private RevisionEventStatus getEventStatus(RevisionTarget target, Object auditedEntity, Optional<Object> maybePreAuditedEntity) {
    if (maybePreAuditedEntity.isPresent()
            && isNotChanged(target, auditedEntity, maybePreAuditedEntity.get())) {
      return UNCHANGED;
    } else {
      return RevisionEventStatus.DIRTY_CHECKING;
    }
  }

  private boolean isNotChanged(RevisionTarget target, Object auditedEntity, Object anotherEntity) {
    Object currentVO = target.getCompareVO(auditedEntity);
    Object anotherVO = target.getCompareVO(anotherEntity);
    return currentVO.equals(anotherVO);
  }

  private void updateEventStatus(RevisionHistoryDetail detail, RevisionEventStatus eventStatus) {
    EntityTransaction transaction = entityManager.getTransaction();
    Long id = detail.getId();
    try {
      transaction.begin();
      historyDetailRepositoryQueryDsl.updateEventStatus(id, eventStatus);
      transaction.commit();
    } catch (Exception ex) {
      log.warn(String.format("[Error] Update RevisionModifiedEntity id : %s, target : %s, entityId : %s", id, eventStatus), ex);
      transaction.rollback();
    }
  }

  @Override
  public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
    return true;
  }

  @Override
  public void onPostInsertCommitFailed(PostInsertEvent event) {

  }
}