package com.gmoon.demo.repository;

import com.gmoon.demo.domain.Member;
import com.gmoon.demo.domain.MemberVO;
import com.gmoon.demo.domain.QMember;
import com.gmoon.demo.domain.QMemberVO_Data;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Member> findAllOfQueryDsl() {
        QMember qMember = QMember.member;

        return jpaQueryFactory
                .select(qMember)
                .from(qMember)
                .fetch();
    }

    @Override
    public List<MemberVO.Data> findAllOfQueryDslWithProjection() {
        QMember qMember = QMember.member;

        return jpaQueryFactory
                .select(new QMemberVO_Data(qMember.id, qMember.name, qMember.memberOption.enabled))
                .from(qMember)
                .fetch();
    }


}
