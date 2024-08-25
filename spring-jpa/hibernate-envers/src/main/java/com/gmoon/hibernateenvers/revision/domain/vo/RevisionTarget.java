package com.gmoon.hibernateenvers.revision.domain.vo;

import java.util.Arrays;

import com.gmoon.hibernateenvers.global.domain.BaseEntity;
import com.gmoon.hibernateenvers.global.model.BaseRevisionCompareVO;
import com.gmoon.hibernateenvers.global.utils.RevisionConverter;
import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.member.model.MemberCompareVO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Getter
public enum RevisionTarget {

	MEMBER(Member.class, MemberCompareVO.class);

	private final Class<? extends BaseEntity> entityClass;
	private final Class<? extends BaseRevisionCompareVO> compareVOClass;

	public static RevisionTarget of(Class<? extends BaseEntity> entityClass) {
		return Arrays.stream(RevisionTarget.values())
			 .filter(target -> target.getEntityClass().equals(entityClass))
			 .findFirst()
			 .orElse(null);
	}

	public Object getCompareVO(Object entity) {
		return RevisionConverter.convertTo(getEntityClass().cast(entity), getCompareVOClass());
	}
}
