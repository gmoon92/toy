package com.gmoon.hibernateenvers.revision.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.global.envers.listener.CustomRevisionListener;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@RevisionEntity(CustomRevisionListener.class)
@Table(name = "lt_revision")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Revision implements Serializable {

	@Id
	@GeneratedValue
	@RevisionNumber
	private Long id;

	@RevisionTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Instant createdAt;

	private String updatedBy;

	private String updatedByUsername;

	/**
	 * 일반적으로 각 Revision에서 변경된 Entity Type을 추적하지 않는다.
	 * 수정된 Entity 이름을 추적하는 세 가지 방법으로 활성화할 수 있다.
	 * 1) @org.hibernate.envers.ModifiedEntityNames 애노테이션 방식 : Property는 Set<String> 유형이어야한다.
	 * https://docs.jboss.org/hibernate/core/4.1/devguide/en-US/html/ch15.html#envers-tracking-properties-changes
	 */
	@OneToMany(mappedBy = "revision", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<RevisionHistory> details = new HashSet<>();

	public void changeEntity(Class<? extends BaseEntity> entityClass, Object entityId, RevisionType revisionType) {
		details.add(RevisionHistory.create(this, revisionType, entityId, RevisionTarget.of(entityClass)));
	}
}
