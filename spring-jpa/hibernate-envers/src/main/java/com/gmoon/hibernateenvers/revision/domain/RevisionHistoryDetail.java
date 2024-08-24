package com.gmoon.hibernateenvers.revision.domain;

import org.hibernate.annotations.Type;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.internal.entities.RevisionTypeType;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rev_history_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RevisionHistoryDetail extends BaseEntity {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "revision_number")
	@EqualsAndHashCode.Include
	private RevisionHistory revision;

	@TODO("복합키 고려해보기... toString")
	@Column(updatable = false, nullable = false)
	@Lob
	@EqualsAndHashCode.Include
	private byte[] entityId;

	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	@EqualsAndHashCode.Include
	private RevisionTarget target;

	@Enumerated(EnumType.STRING)
	@Type(RevisionTypeType.class)
	@Column(updatable = false, nullable = false)
	private RevisionType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RevisionEventStatus status;

	@ManyToOne
	@JoinColumn(name = "target_member_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
	private Member targetMember;

	@Column
	private String targetMemberName;

	@Builder(access = AccessLevel.PRIVATE)
	private RevisionHistoryDetail(RevisionHistory revision, RevisionType type, Object entityId,
		 RevisionTarget target, RevisionEventStatus status) {
		this.revision = revision;
		this.entityId = RevisionConverter.serializedObject(entityId);
		this.target = target;
		this.type = type;
		this.status = status;
	}

	public static RevisionHistoryDetail newCreate(RevisionHistory revision, RevisionType revisionType,
		 Object entityId, RevisionTarget revisionTarget) {
		return RevisionHistoryDetail.builder()
			 .revision(revision)
			 .type(revisionType)
			 .entityId(entityId)
			 .target(revisionTarget)
			 .status(RevisionEventStatus.WAIT)
			 .build();
	}

}

