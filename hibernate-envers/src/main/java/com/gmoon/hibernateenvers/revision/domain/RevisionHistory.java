package com.gmoon.hibernateenvers.revision.domain;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.global.envers.listener.CustomRevisionListener;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rev_history")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@ToString(exclude = { "details" })
@EqualsAndHashCode(of = { "id" })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionHistory extends BaseEntity {

  @Id
  @GeneratedValue
  @RevisionNumber
  private Long id;

  @RevisionTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date createdDt;

  private String updatedBy;

  private String updatedByUsername;

  /**
   * 일반적으로 각 Revision에서 변경된 Entity Type을 추적하지 않는다.
   * 수정된 Entity 이름을 추적하는 세 가지 방법으로 활성화할 수 있다.
   * 1) @org.hibernate.envers.ModifiedEntityNames 애노테이션 방식 : Property는 Set<String> 유형이어야한다.
   * https://docs.jboss.org/hibernate/core/4.1/devguide/en-US/html/ch15.html#envers-tracking-properties-changes
   */
  @OneToMany(mappedBy = "revision", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
  private Set<RevisionHistoryDetail> details = new HashSet<>();

  public void trace(Class entityClass, Serializable entityId, RevisionType revisionType) {
    details.add(RevisionHistoryDetail.newCreate(this, revisionType, entityId, RevisionTarget.of(entityClass)));
  }
}
