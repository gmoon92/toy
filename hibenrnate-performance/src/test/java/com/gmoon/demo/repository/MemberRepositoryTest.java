package com.gmoon.demo.repository;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.domain.Member;
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
        memberRepository.save(Member.newInstance("gmoon"));
        log.debug("Database data setup done...");
    }

    /**
     * Hibernate:
     *
     *     create table member (
     *        id bigint not null,
     *         name varchar(255),
     *         team_id bigint,
     *         primary key (id)
     *     )
     * Hibernate:
     *
     *     create table member_option (
     *        member_id bigint not null,
     *         enabled boolean,
     *         retired boolean,
     *         primary key (member_id)
     *     )
    * Hibernate:
     *
     *     alter table member
     *        add constraint FKcjte2jn9pvo9ud2hyfgwcja0k
     *        foreign key (team_id)
     *        references team
     * Hibernate:
     *
     *     alter table member_option
     *        add constraint FKtn6oplo1yxuba8kk2ag9eu9gy
     *        foreign key (member_id)
     *        references member
    * */
    @Test
    @DisplayName("연관 관계 맵핑 설정 - @OneToOne(mappedBy = \"member\", fetch = FetchType.LAZY, optional = false)")
    void testOneToOne() {
        EntityManager em = getEntityManager();
        Member member = em.find(Member.class, 1L);
        log.debug("member : {}", member);
    }

    @Test
    @DisplayName("queryDSL fetchJoin - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
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
     * @see Member#memberOption
     * @see org.hibernate.annotations.Fetch
     * @see org.hibernate.annotations.FetchMode
     * */
    @Test
    @DisplayName("@Fetch - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
    void testOneToOneWhenFetchAnnotation() {
        EntityManager em = getEntityManager();
        Member member = em.find(Member.class, 1L);
        log.debug("member : {}", member);
    }

    /**
     * @see Member
     * @see MemberRepository
     * @see org.springframework.data.jpa.repository.EntityGraph
     * @see javax.persistence.NamedEntityGraph
     * */
    @Test
    @DisplayName("@EntityGraph - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
    void testOneToOneWhenEntityGraph() {
        Member member = memberRepository.findByName("gmoon");
        log.debug("member : {}", member);
    }

}