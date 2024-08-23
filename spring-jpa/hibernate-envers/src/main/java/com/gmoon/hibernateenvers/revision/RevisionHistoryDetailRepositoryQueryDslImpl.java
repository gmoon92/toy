package com.gmoon.hibernateenvers.revision;

import static com.gmoon.hibernateenvers.revision.domain.QRevisionHistory.*;
import static com.gmoon.hibernateenvers.revision.domain.QRevisionHistoryDetail.*;

import java.util.List;
import java.util.Optional;

import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import com.gmoon.hibernateenvers.revision.vo.QRevisionListVO_DataVO;
import com.gmoon.hibernateenvers.revision.vo.RevisionListVO;
import com.gmoon.javacore.util.StringUtils;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RevisionHistoryDetailRepositoryQueryDslImpl extends QuerydslRepositorySupport
	 implements RevisionHistoryDetailRepositoryQueryDsl {

	private final EntityManager entityManager;

	public RevisionHistoryDetailRepositoryQueryDslImpl(EntityManager entityManager) {
		super(RevisionHistoryDetail.class);
		this.entityManager = entityManager;
	}

	@Override
	public void updateEventStatus(Long id, RevisionEventStatus eventStatus) {
		new JPAUpdateClause(entityManager, revisionHistoryDetail)
			 .set(revisionHistoryDetail.status, eventStatus)
			 .where(revisionHistoryDetail.id.eq(id))
			 .execute();
	}

	@Override
	public Optional<RevisionHistoryDetail> findPreRevisionHistoryDetail(RevisionHistoryDetail detail) {
		JPAQuery<RevisionHistoryDetail> query = new JPAQuery<>(entityManager);
		query.select(revisionHistoryDetail)
			 .from(revisionHistory)
			 .innerJoin(revisionHistory.details, revisionHistoryDetail)
			 .where(revisionHistoryDetail.revision.id.eq(JPAExpressions.select(revisionHistory.id.max())
				  .from(revisionHistory)
				  .innerJoin(revisionHistory.details, revisionHistoryDetail)
				  .where(revisionHistoryDetail.entityId.eq(detail.getEntityId())
					   .and(revisionHistoryDetail.target.eq(detail.getTarget()))
					   .and(revisionHistoryDetail.revision.id.lt(detail.getRevision().getId())))));
		return Optional.ofNullable(query.fetchOne());
	}

	@Override
	public List<RevisionHistoryDetail> findAllByRevisionAndTarget(Long revisionNumber, RevisionTarget target) {
		JPAQuery<RevisionHistoryDetail> query = new JPAQuery(entityManager);
		return query.select(revisionHistoryDetail)
			 .from(revisionHistoryDetail)
			 .where(revisionHistoryDetail.revision.id.eq(revisionNumber)
				  .and(revisionHistoryDetail.target.eq(target)))
			 .fetch();
	}

	@Override
	public Page<RevisionListVO.DataVO> findAllBySearchVO(RevisionListVO.SearchVO searchVO) {
		JPAQuery<RevisionListVO.DataVO> query = new JPAQuery<>(entityManager);

		Pageable pageable = searchVO.getPageable();
		query.select(new QRevisionListVO_DataVO(revisionHistory.id, revisionHistory.createdDt
				  , revisionHistory.updatedBy, revisionHistory.updatedByUsername
				  , revisionHistoryDetail.target, revisionHistoryDetail.entityId
				  , revisionHistoryDetail.targetMemberName))
			 .from(revisionHistory)
			 .innerJoin(revisionHistory.details, revisionHistoryDetail)
			 .where(revisionHistoryDetail.status.eq(RevisionEventStatus.DIRTY_CHECKING)
				  .and(revisionHistoryDetail.type.eq(RevisionType.MOD))
				  .and(getSearchCondition(searchVO)));

		List<RevisionListVO.DataVO> list = getQuerydsl().applyPagination(pageable, query).fetch();
		return new PageImpl(list, pageable, query.fetchCount());
	}

	private BooleanBuilder getSearchCondition(RevisionListVO.SearchVO search) {
		BooleanBuilder builder = new BooleanBuilder();
		if (search.getStartDt() != null) {
			builder.and(revisionHistory.createdDt.goe(search.getStartDt()));
		}

		if (search.getEndDt() != null) {
			builder.and(revisionHistory.createdDt.loe(search.getEndDt()));
		}

		String keyword = StringUtils.defaultString(search.getSearchKeyword());
		RevisionListVO.SearchVO.SearchType searchType = search.getSearchType();
		return switch (searchType) {
			case MEMBER_NAME -> builder.and(revisionHistory.updatedByUsername.contains(keyword));
			case TARGET_MEMBER_NAME -> builder.and(revisionHistoryDetail.targetMemberName.contains(keyword));
			default -> builder;
		};
	}
}
