package com.gmoon.demo.member.repository;

import com.gmoon.demo.member.Member;
import com.gmoon.demo.member.model.MemberVO;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findAllOfQueryDsl();

    List<MemberVO.Data> findAllOfQueryDslWithProjection();
}
