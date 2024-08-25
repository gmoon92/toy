package com.gmoon.hibernateenvers.revision.infra;

import static com.gmoon.hibernateenvers.revision.domain.QRevision.*;
import static com.gmoon.hibernateenvers.revision.domain.QRevisionHistory.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistory;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryRepository;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;
import com.gmoon.hibernateenvers.revision.model.QRevisionListVO_DataVO;
import com.gmoon.hibernateenvers.revision.model.RevisionListVO;
import com.gmoon.javacore.util.StringUtils;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class RevisionHistoryRepositoryImpl extends QuerydslRepositorySupport
	 implements RevisionHistoryRepository {

	private final RevisionHistoryJpaRepository repository;
	private final EntityManager entityManager;

	public RevisionHistoryRepositoryImpl(RevisionHistoryJpaRepository revisionHistoryJpaRepository,
		 EntityManager entityManager) {
		super(RevisionHistory.class);
		this.repository = revisionHistoryJpaRepository;
		this.entityManager = entityManager;
	}

	@Transactional
	@Override
	public void updateStatus(Long id, RevisionStatus status) {
		new JPAUpdateClause(entityManager, revisionHistory)
			 .set(revisionHistory.status, status)
			 .where(revisionHistory.id.eq(id))
			 .execute();
	}

	@Override
	public Optional<RevisionHistory> findPrevious(RevisionHistory detail) {
		JPAQuery<RevisionHistory> query = new JPAQuery<>(entityManager);
		query.select(revisionHistory)
			 .from(revision)
			 .innerJoin(revision.details, revisionHistory)
			 .where(revisionHistory.revision.id.eq(JPAExpressions.select(revision.id.max())
				  .from(revision)
				  .innerJoin(revision.details, revisionHistory)
				  .where(revisionHistory.entityId.eq(detail.getEntityId())
					   .and(revisionHistory.target.eq(detail.getTarget()))
					   .and(revisionHistory.revision.id.lt(detail.getRevision().getId())))));
		return Optional.ofNullable(query.fetchOne());
	}

	@Override
	public List<RevisionHistory> findAll() {
		return repository.findAll();
	}

	@Override
	public List<RevisionHistory> findAll(Long revisionNumber, RevisionTarget target) {
		return repository.findAllByRevisionIdAndTarget(revisionNumber, target);
	}

	@Override
	public Page<RevisionListVO.DataVO> findAll(RevisionListVO.SearchVO searchVO) {
		JPAQuery<RevisionListVO.DataVO> query = new JPAQuery<>(entityManager);

		Pageable pageable = searchVO.getPageable();
		query.select(new QRevisionListVO_DataVO(
				  revision.id, revision.createdAt,
				  revisionHistory.entityId, revisionHistory.target,
				  revision.updatedBy, revision.updatedByUsername,
				  revisionHistory.targetMemberName
			 ))
			 .from(revision)
			 .innerJoin(revision.details, revisionHistory)
			 .where(
				  revisionHistory.status.eq(RevisionStatus.DIRTY_CHECKING)
					   .and(revisionHistory.type.eq(RevisionType.MOD))
					   .and(betweenCreatedAt(searchVO.getStartTime(), searchVO.getEndTime()))
					   .and(searchKeyword(searchVO.getSearchKeyword(), searchVO.getKeywordCondition()))
			 );

		List<RevisionListVO.DataVO> list = getQuerydsl().applyPagination(pageable, query).fetch();
		return new PageImpl(list, pageable, query.fetchCount());
	}

	private Predicate betweenCreatedAt(Long startTime, Long endTime) {
		BooleanBuilder predicate = new BooleanBuilder();
		if (startTime != null) {
			predicate.and(revision.createdAt.goe(Instant.ofEpochMilli(startTime)));
		}

		if (endTime != null) {
			predicate.and(revision.createdAt.loe(Instant.ofEpochMilli(endTime)));
		}
		return predicate;
	}

	private Predicate searchKeyword(String keyword, RevisionListVO.SearchVO.KeywordCondition keywordCondition) {
		if (StringUtils.isBlank(keyword)) {
			return null;
		}

		return switch (keywordCondition) {
			case UPDATED_BY -> revision.updatedByUsername.contains(keyword);
			case MEMBER_NAME -> revisionHistory.targetMemberName.contains(keyword);
		};
	}
}
