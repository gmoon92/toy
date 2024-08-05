package com.gmoon.hibernateperformance.member.domain;

import java.util.List;

import com.gmoon.hibernateperformance.member.model.Members;

public interface MemberRepositoryCustom {

	List<Member> findAllOfQueryDsl();

	Members findAllOfQueryDslWithProjection();

	long bulkUpdateRetireMembers();
}
