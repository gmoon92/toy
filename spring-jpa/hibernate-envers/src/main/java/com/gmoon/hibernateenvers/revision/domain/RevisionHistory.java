package com.gmoon.hibernateenvers.revision.domain;

import java.io.Serial;
import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.internal.entities.RevisionTypeType;

import com.gmoon.hibernateenvers.global.annotation.TODO;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "lt_revision_history")
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RevisionHistory implements Serializable {

	@Serial
	private static final long serialVersionUID = 892401811841730874L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "revision_number")
	@EqualsAndHashCode.Include
	private Revision revision;

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
	private RevisionStatus status;

	@ManyToOne
	@JoinColumn(
		 name = "target_member_id",
		 foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT)
	)
	private Member targetMember;

	@Column
	private String targetMemberName;

	public static RevisionHistory create(
		 Revision revision,
		 RevisionType revisionType,
		 Object entityId,
		 RevisionTarget revisionTarget
	) {
		return RevisionHistory.builder()
			 .revision(revision)
			 .type(revisionType)
			 .entityId(RevisionConverter.serializedObject(entityId))
			 .target(revisionTarget)
			 .status(RevisionStatus.WAIT)
			 .build();
	}

	public void changeStatus(RevisionStatus status) {
		this.status = status;
	}
}

