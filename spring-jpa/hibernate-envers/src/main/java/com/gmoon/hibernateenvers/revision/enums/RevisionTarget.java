package com.gmoon.hibernateenvers.revision.enums;

import java.util.Arrays;

import com.gmoon.hibernateenvers.global.domain.BaseTrackingEntity;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.global.vo.BaseRevisionCompareVO;
import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.member.vo.MemberCompareVO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum RevisionTarget {

	MEMBER(Member.class, MemberCompareVO.class);

	private final Class<? extends BaseTrackingEntity> entityClass;
	private final Class<? extends BaseRevisionCompareVO> compareVOClass;

	public static RevisionTarget of(Class entityClass) {
		return Arrays.stream(RevisionTarget.values())
			 .filter(target -> target.getEntityClass().equals(entityClass))
			 .findFirst()
			 .orElse(null);
	}

	public Object getCompareVO(Object entity) {
		return RevisionConverter.convertTo(getEntityClass().cast(entity), getCompareVOClass());
	}
}
