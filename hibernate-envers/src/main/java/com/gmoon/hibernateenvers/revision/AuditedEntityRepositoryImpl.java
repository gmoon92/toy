package com.gmoon.hibernateenvers.revision;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.exception.NotAuditedException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQueryCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AuditedEntityRepositoryImpl implements AuditedEntityRepository {

  private final EntityManager em;

  protected AuditReader getAuditReader() {
    return AuditReaderFactory.get(em);
  }

  protected AuditQueryCreator getAuditQuery() {
    return getAuditReader().createQuery();
  }

  @Override
  @Transactional(readOnly = true)
  public <T extends BaseTrackingEntity> Optional<T> findAuditedEntity(Class<T> entityClass, Object entityId, Long revisionNumber) {
    Object auditedEntity = null;
    try {
      auditedEntity = getAuditQuery()
              .forEntitiesModifiedAtRevision(entityClass, revisionNumber)
              .add(AuditEntity.id().eq(entityId))
              .getSingleResult();
    } catch (AuditException | NonUniqueResultException | NoResultException ex) {
      log.warn(String.format("Not found audited entity... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId), ex);
    } catch (Exception ex) {
      throw new RuntimeException(String.format("Unexpected exception... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId), ex);
    } finally {
      return Optional.ofNullable(entityClass.cast(auditedEntity));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public <T extends BaseTrackingEntity> Optional<T> findAuditedEntity(Class<T> entityClass, Object entityId, Long revisionNumber, RevisionType revisionType) {
    Object auditedEntity = null;
    try {
      auditedEntity = getAuditQuery()
              .forEntitiesModifiedAtRevision(entityClass, revisionNumber)
              .add(AuditEntity.id().eq(entityId))
              .add(AuditEntity.revisionType().eq(revisionType))
              .getSingleResult();
    } catch (AuditException | NonUniqueResultException | NoResultException ex) {
      log.warn(String.format("Not found audited entity... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId), ex);
    } catch (Exception ex) {
      throw new RuntimeException(String.format("Unexpected exception... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId), ex);
    } finally {
      return Optional.ofNullable(entityClass.cast(auditedEntity));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public <T extends BaseTrackingEntity> Optional<T> findPreAuditedEntity(Class<T> entityClass, Object entityId, Long revisionNumber) {
    T auditedEntity = null;
    try {
      auditedEntity = getAuditReader().find(entityClass, entityId, revisionNumber - 1);
    } catch (IllegalArgumentException | NotAuditedException | IllegalStateException ex) {
      String errorMessage = String.format("Not found audited entity... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId);
      log.warn(errorMessage, ex);
    } catch (Exception ex) {
      String errorMessage = String.format("Unexpected exception... revisionNumber : %s, entityClass : %s, entityId : %s", revisionNumber, entityClass, entityId);
      log.error(errorMessage, ex);
      throw new RuntimeException(errorMessage, ex);
    } finally {
      return Optional.ofNullable(auditedEntity);
    }
  }
}
