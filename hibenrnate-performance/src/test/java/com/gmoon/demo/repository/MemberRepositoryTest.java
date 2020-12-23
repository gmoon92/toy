package com.gmoon.demo.repository;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.domain.Member;
import com.gmoon.demo.domain.QMember;
import com.gmoon.demo.domain.QMemberOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest extends BaseRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @BeforeAll
    static void setup(@Autowired MemberRepository memberRepository) {
        log.debug("Database data setup start...");

        memberRepository.deleteAllInBatch();
        memberRepository.save(Member.builder()
                .name("gmoon")
                .build());

        log.debug("Database data setup done...");
    }

    @Test
    @DisplayName("식별 관계 주 테이블(Member) OneToOne 양방향 관계")
    void testOneToOne() {
//        memberRepository.findById(1L);
        QMember qMember = QMember.member;
        QMemberOption qMemberOption = QMemberOption.memberOption;

//      select member1 from Member member1 left join fetch member1.memberOption as memberOption
        getJPAQuery().select(qMember)
                .from(qMember)
                .leftJoin(qMember.memberOption, qMemberOption).fetchJoin()
                .fetchOne();
    }


}