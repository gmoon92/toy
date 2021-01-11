package com.gmoon.demo.repository;

import com.gmoon.demo.domain.Member;
import com.gmoon.demo.domain.MemberVO;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findAllOfQueryDsl();

    List<MemberVO.Data> findAllOfQueryDslWithProjection();
}
