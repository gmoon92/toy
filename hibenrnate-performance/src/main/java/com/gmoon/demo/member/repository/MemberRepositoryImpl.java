package com.gmoon.demo.member.repository;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.domain.QMember;
import com.gmoon.demo.member.domain.QMemberOption;
import com.gmoon.demo.member.model.Members;
import com.gmoon.demo.member.model.QMembers_Data;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Member> findAllOfQueryDsl() {
    QMember member = QMember.member;
    QMemberOption memberOption = QMemberOption.memberOption;

    return jpaQueryFactory
            .select(member)
            .from(member)
            .innerJoin(member.memberOption, memberOption).fetchJoin()
            .fetch();
  }

  @Override
  public Members findAllOfQueryDslWithProjection() {
    QMember member = QMember.member;
    QMemberOption memberOption = QMemberOption.memberOption;

//        cross join 조심
//        List<Members.Data> list = jpaQueryFactory
//                .select(new QMembers_Data(member.id, member.name, memberOption.embeddedMemberOption.enabled))
//                .from(member)
//                .fetch();

//        projection을 사용하여 엔티티 그래프는 도메인을 대상으로한다 vo는 도메인이 아니다.
//        List<Members.Data> list = jpaQueryFactory
//                .select(new QMembers_Data(member.id, member.name, memberOption.embeddedMemberOption.enabled))
//                .from(member)
//                .innerJoin(member.memberOption, memberOption).fetchJoin()
//                .fetch();

    List<Members.Data> list = jpaQueryFactory
            .select(new QMembers_Data(member.id, member.name, memberOption.embeddedMemberOption.enabled))
            .from(member)
            .innerJoin(member.memberOption, memberOption)
            .fetch();

    return new Members(list);
  }

}
