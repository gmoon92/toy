package com.gmoon.demo.repository;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.domain.Member;
import com.gmoon.demo.domain.MemberOption;
import com.gmoon.demo.domain.QMember;
import com.gmoon.demo.domain.QMemberOption;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

@DataJpaTest
@RequiredArgsConstructor
class MemberRepositoryTest extends BaseRepositoryTest {

    final MemberRepository memberRepository;

    @BeforeAll
    static void setup(@Autowired MemberRepository memberRepository) {
        log.debug("Database data setup start...");
        memberRepository.deleteAllInBatch();
        Member member = memberRepository.save(Member.newInstance("gmoon"));
        member.setMemberOption(MemberOption.defaultOption(member));
        log.debug("Database data setup done...");
    }

    @Test
    @DisplayName("쿼리 DSL - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
    void testOneToOneWhenQueryDsl() {
//        memberRepository.findById(1L);
        QMember qMember = QMember.member;
        QMemberOption qMemberOption = QMemberOption.memberOption;

//      select member1 from Member member1 left join fetch member1.memberOption as memberOption
        getJPAQuery().select(qMember)
                .from(qMember)
                .leftJoin(qMember.memberOption, qMemberOption).fetchJoin()
                .fetchOne();
    }

    /***
     *
     * @see Member#memberOption
     * @see org.hibernate.annotations.Fetch
     * @see org.hibernate.annotations.FetchMode
     * */
    @Test
    @DisplayName("@Fetch - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
    void testOneToOneWhenFetch() {
        EntityManager em = getEntityManager();
        Member member = em.find(Member.class, 1L);
        log.debug("member : {}", member);
    }



    /**
     * 1. @Fetch
     * 2.EntityGraph
     *
     * I think that Spring Data ignores the FetchMode. I always use the @NamedEntityGraph and @EntityGraph annotations when working with Spring Data
     *
     * https://docs.spring.io/spring-data/data-jpa/docs/current/reference/html/#jpa.entity-graph
     *
     * https://stackoverflow.com/questions/29602386/how-does-the-fetchmode-work-in-spring-data-jpa
     * */


}