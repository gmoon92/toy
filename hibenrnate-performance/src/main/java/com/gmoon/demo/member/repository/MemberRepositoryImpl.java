package com.gmoon.demo.member.repository;

import com.gmoon.demo.domain.QMember;
import com.gmoon.demo.domain.QMemberOption;
import com.gmoon.demo.domain.QMemberVO_Data;
import com.gmoon.demo.member.Member;
import com.gmoon.demo.member.model.MemberVO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Member> findAllOfQueryDsl() {
    QMember qMember = QMember.member;
    QMemberOption qMemberOption = QMemberOption.memberOption;

    return jpaQueryFactory
            .select(qMember)
            .from(qMember)
            .innerJoin(qMember.memberOption, qMemberOption).fetchJoin()
            .fetch();
  }

  @Override
  public List<MemberVO.Data> findAllOfQueryDslWithProjection() {
    QMember qMember = QMember.member;
    QMemberOption qMemberOption = QMemberOption.memberOption;

//        cross join 조심
//        return jpaQueryFactory
//                .select(new QMemberVO_Data(qMember.id, qMember.name, qMemberOption.accountOptionEmb.enabled))
//                .from(qMember)
//                .fetch();

//        projection을 사용하여 엔티티 그래프는 도메인을 대상으로한다 vo는 도메인이 아니다.
//        return jpaQueryFactory
//                .select(new QMemberVO_Data(qMember.id, qMember.name, qMemberOption.accountOptionEmb.enabled))
//                .from(qMember)
//                .innerJoin(qMember.memberOption, qMemberOption).fetchJoin()
//                .fetch();

    return jpaQueryFactory
            .select(new QMemberVO_Data(qMember.id, qMember.name, qMemberOption.accountOptionEmb.enabled))
            .from(qMember)
            .innerJoin(qMember.memberOption, qMemberOption)
            .fetch();
  }


}
