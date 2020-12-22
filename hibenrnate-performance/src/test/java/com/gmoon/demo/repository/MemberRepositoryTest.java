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

    /**
     * 학습 목표
     * 1. OneToOne 성능 향상 방법을 생각해보자.
     * <p>
     * Member - MemberOption 도메인은 OneToOne 양방향 관계로 설정되어 있다.
     * 이때 Member 도메인에 MemberOption의 글로벌 패치 전략을 Lazy로 설정했음에도
     * Member를 조회할때, MemberOption을 조회하는 쿼리가 발생하게 된다.
     * <p>
     * 이는 성능상 문제가 발생할 수 있다. 어떻게 해결할 수 있을까?
     */
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

    /**
     * 1. fetchJoin vs fetchAll
     *
     * http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/jpa/JPAQueryBase.html
     * [1] fetchJoin : 마지막으로 정의 된 조인에 "fetchJoin" 플래그 추가.
     *             컬렉션 조인(@OneToMany)은 중복 행을 초래할 수 있으며,
     *             "inner join fetchJoin" 플래그(fetchJoin)를 통해 결과 집합을 제한할 수 있다.
     *
     * [2] fetchAll : 마지막으로 정의 된 조인에 "fetchJoin all properties" 플래그 추가
     *              Lazy 패치 타입의 프로퍼티를 초기화 없이 가져 오도록 강제할 수 있다.
     *
     *  [1] select member1 from Member member1 left join fetch member1.memberOption as memberOption
     *  queryFactory.select(qMember).from(qMember)
     *        .leftJoin(qMember.memberOption, qMemberOption).fetchJoin()
     *        .fetchOne();
     *
     *
     *  [2] select member1 from Member member1 left join member1.memberOption as memberOption fetch all properties
     *  queryFactory.select(qMember) .from(qMember)
     *        .leftJoin(qMember.memberOption, qMemberOption).fetchAll()
     *        .fetchOne();
     *
     * http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/jpa/JPAQueryBase.html
     *
     * fetchAll 참고
     * - If you are using property-level lazy fetching (with bytecode instrumentation), it is possible to force Hibernate to fetch the lazy properties in the first query immediately using fetch all properties.
     *   https://docs.jboss.org/hibernate/core/3.3/reference/en/html/queryhql.html
     * - Fetch All Properties not working
     *   https://forum.hibernate.org/viewtopic.php?p=2249643
     *
     * 2. @OneToMany fetchJoin과 pagination을 같이 사용되면 발생되는 문제점
     * https://bottom-to-top.tistory.com/45
     * */


}