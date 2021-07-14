package com.gmoon.demo.member.repository;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.model.Members;

import java.util.List;

public interface MemberRepositoryCustom {

  List<Member> findAllOfQueryDsl();
  Members findAllOfQueryDslWithProjection();
}
