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
import lombok.ToString;

@Entity
@Table(name = "rev_history_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"revision", "entityId", "revisionTarget"})
@ToString(exclude = {"id", "entityId", "revisionTarget", "revisionType", "revisionEventStatus"})
public class RevisionHistoryDetail extends BaseEntity {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "revision_number")
	private RevisionHistory revision;

	@TODO("복합키 고려해보기... toString")
	@Column(name = "entity_id", updatable = false, nullable = false)
	@Lob
	private byte[] entityId;

	@Enumerated(EnumType.STRING)
	@Column(name = "target", updatable = false)
	private RevisionTarget revisionTarget;

	@Enumerated(EnumType.STRING)
	@Type(RevisionTypeType.class)
	@Column(name = "revision_type", updatable = false, nullable = false)
	private RevisionType revisionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_status", nullable = false)
	private RevisionEventStatus revisionEventStatus;

	@ManyToOne
	@JoinColumn(name = "target_member_id", foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
	private Member targetMember;

	@Column(name = "target_member_name")
	private String targetMemberName;

	@Builder(access = AccessLevel.PRIVATE)
	private RevisionHistoryDetail(RevisionHistory revision, RevisionType revisionType, Object entityId,
		 RevisionTarget revisionTarget, RevisionEventStatus revisionEventStatus) {
		this.revision = revision;
		this.entityId = RevisionConverter.serializedObject(entityId);
		this.revisionTarget = revisionTarget;
		this.revisionType = revisionType;
		this.revisionEventStatus = revisionEventStatus;
	}

	public static RevisionHistoryDetail newCreate(RevisionHistory revision, RevisionType revisionType,
		 Object entityId, RevisionTarget revisionTarget) {
		return RevisionHistoryDetail.builder()
			 .revision(revision)
			 .revisionType(revisionType)
			 .entityId(entityId)
			 .revisionTarget(revisionTarget)
			 .revisionEventStatus(RevisionEventStatus.WAIT)
			 .build();
	}

}

