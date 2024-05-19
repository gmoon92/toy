package com.gmoon.hibernateenvers.revision;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.RevisionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

import com.gmoon.hibernateenvers.revision.domain.QRevisionHistory;
import com.gmoon.hibernateenvers.revision.domain.QRevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import com.gmoon.hibernateenvers.revision.vo.QRevisionListVO_DataVO;
import com.gmoon.hibernateenvers.revision.vo.RevisionListVO;

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
		QRevisionHistoryDetail qHistoryDetail = QRevisionHistoryDetail.revisionHistoryDetail;

		new JPAUpdateClause(entityManager, qHistoryDetail)
			 .set(qHistoryDetail.revisionEventStatus, eventStatus)
			 .where(qHistoryDetail.id.eq(id))
			 .execute();
	}

	@Override
	public Optional<RevisionHistoryDetail> findPreRevisionHistoryDetail(RevisionHistoryDetail detail) {
		QRevisionHistory history = QRevisionHistory.revisionHistory;
		QRevisionHistoryDetail historyDetail = QRevisionHistoryDetail.revisionHistoryDetail;

		JPAQuery<RevisionHistoryDetail> query = new JPAQuery(entityManager);
		query.select(historyDetail)
			 .from(history)
			 .innerJoin(history.details, historyDetail)
			 .where(historyDetail.revision.id.eq(JPAExpressions.select(history.id.max())
				  .from(history)
				  .innerJoin(history.details, historyDetail)
				  .where(historyDetail.entityId.eq(detail.getEntityId())
					   .and(historyDetail.revisionTarget.eq(detail.getRevisionTarget()))
					   .and(historyDetail.revision.id.lt(detail.getRevision().getId())))));
		return Optional.ofNullable(query.fetchOne());
	}

	@Override
	public List<RevisionHistoryDetail> findAllByRevisionAndTarget(Long revisionNumber, RevisionTarget target) {
		QRevisionHistoryDetail historyDetail = QRevisionHistoryDetail.revisionHistoryDetail;

		JPAQuery<RevisionHistoryDetail> query = new JPAQuery(entityManager);
		return query.select(historyDetail)
			 .from(historyDetail)
			 .where(historyDetail.revision.id.eq(revisionNumber)
				  .and(historyDetail.revisionTarget.eq(target)))
			 .fetch();
	}

	@Override
	public Page<RevisionListVO.DataVO> findAllBySearchVO(RevisionListVO.SearchVO searchVO) {
		QRevisionHistory history = QRevisionHistory.revisionHistory;
		QRevisionHistoryDetail historyDetail = QRevisionHistoryDetail.revisionHistoryDetail;

		JPAQuery<RevisionListVO.DataVO> query = new JPAQuery(entityManager);

		Pageable pageable = searchVO.getPageable();
		query.select(new QRevisionListVO_DataVO(history.id, history.createdDt
				  , history.updatedBy, history.updatedByUsername
				  , historyDetail.revisionTarget, historyDetail.entityId
				  , historyDetail.targetMemberName))
			 .from(history)
			 .innerJoin(history.details, historyDetail)
			 .where(historyDetail.revisionEventStatus.eq(RevisionEventStatus.DIRTY_CHECKING)
				  .and(historyDetail.revisionType.eq(RevisionType.MOD))
				  .and(getSearchCondition(searchVO)));

		List<RevisionListVO.DataVO> list = getQuerydsl().applyPagination(pageable, query).fetch();
		return new PageImpl(list, pageable, query.fetchCount());
	}

	private BooleanBuilder getSearchCondition(RevisionListVO.SearchVO search) {
		BooleanBuilder builder = new BooleanBuilder();

		QRevisionHistory history = QRevisionHistory.revisionHistory;
		QRevisionHistoryDetail historyDetail = QRevisionHistoryDetail.revisionHistoryDetail;

		Date startDt = search.getStartDt();
		Date endDt = search.getEndDt();

		if (startDt != null)
			builder.and(history.createdDt.goe(startDt));

		if (endDt != null)
			builder.and(history.createdDt.loe(endDt));

		String searchKeyword = StringUtils.defaultString(search.getSearchKeyword());
		RevisionListVO.SearchVO.SearchType keywordCondition = search.getSearchType();
		switch (keywordCondition) {
			case MEMBER_NAME:
				builder.and(history.updatedByUsername.contains(searchKeyword));
				break;
			case TARGET_MEMBER_NAME:
				builder.and(historyDetail.targetMemberName.contains(searchKeyword));
				break;
		}
		return builder;
	}
}
