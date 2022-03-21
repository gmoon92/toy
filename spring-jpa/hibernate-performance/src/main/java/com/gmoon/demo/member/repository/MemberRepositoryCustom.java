package com.gmoon.demo.member.repository;

import java.util.List;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.model.Members;

public interface MemberRepositoryCustom {

	List<Member> findAllOfQueryDsl();

	Members findAllOfQueryDslWithProjection();
}
