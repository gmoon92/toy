package com.gmoon.demo.member.repository;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.domain.MemberOption;
import com.gmoon.demo.member.domain.QMember;
import com.gmoon.demo.member.domain.QMemberOption;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class MemberRepositoryTest extends BaseRepositoryTest {

  static Long TEST_MEMBER_ID_FOR_ACCOUNT_OF_GMOON;

  final MemberRepository memberRepository;

  @BeforeAll
  static void setup(@Autowired MemberRepository memberRepository) {
    log.debug("Database data setup start...");
    memberRepository.deleteAllInBatch();
    memberRepository.save(Member.newInstance("kim"));
    memberRepository.save(Member.newInstance("lee"));
    memberRepository.save(Member.newInstance("hong"));
    memberRepository.save(Member.newInstance("kown"));
    Member gmoon = memberRepository.save(Member.newInstance("gmoon"));
    TEST_MEMBER_ID_FOR_ACCOUNT_OF_GMOON = gmoon.getId();
    log.debug("Database data setup done...");
  }

  /**
   * Hibernate:
   * <p>
   * create table member (
   * id bigint not null,
   * name varchar(255),
   * team_id bigint,
   * primary key (id)
   * )
   * Hibernate:
   * <p>
   * create table member_option (
   * member_id bigint not null,
   * enabled boolean,
   * retired boolean,
   * primary key (member_id)
   * )
   * Hibernate:
   * <p>
   * alter table member
   * add constraint FKcjte2jn9pvo9ud2hyfgwcja0k
   * foreign key (team_id)
   * references team
   * Hibernate:
   * <p>
   * alter table member_option
   * add constraint FKtn6oplo1yxuba8kk2ag9eu9gy
   * foreign key (member_id)
   * references member
   */
  @Test
  @DisplayName("연관 관계 맵핑 설정 - @OneToOne(optional = false)")
  void testOneToOneLazy() {
    // given
    Member member = memberRepository.getOne(TEST_MEMBER_ID_FOR_ACCOUNT_OF_GMOON);

    // when
    member.enabled();
    flushAndClear();

    // then
    MemberOption memberOption = memberRepository.getOne(TEST_MEMBER_ID_FOR_ACCOUNT_OF_GMOON)
            .getMemberOption();
    assertThat(memberOption)
            .hasFieldOrPropertyWithValue("retired", true);
  }

  @Test
  @DisplayName("Spring Data JPA - @Query")
  void testSpringDataJpa_JPQL_Query() {
    memberRepository.findAllOfJpqlQuery();
  }

  @Test
  @DisplayName("EntityManager - @EntityGraph, @NamedEntityGraph")
  void testEntityManager_NamedEntityGraph() {

    EntityManager em = getEntityManager();
    EntityGraph graph = em.getEntityGraph("Member.withMemberOption");

//        단일건 조회
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", graph);
    long primaryKey = 1L;
    Member member = em.find(Member.class, primaryKey, hints);

//        리스트 조회
    Query query = em.createQuery("select m from Member m"
            + " inner join fetch m.memberOption");
//                      ^-- JPQL은 글로벌 패치를 고려하지 않고
//                      항상 외부 조인을 사용하기 때문에 inner join fetch 명시한다.
    query.setHint("javax.persistence.fetchgraph", graph);
    List<Member> list = query.getResultList();
  }

  @Test
  @DisplayName("EntityManager - @EntityGraph, @NamedEntityGraph")
  void testEntityManager_EntityGraph() {
    EntityManager em = getEntityManager();
    EntityGraph<Member> graph = em.createEntityGraph(Member.class);
    graph.addAttributeNodes("memberOption");

    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", graph);
    Member member = em.find(Member.class, 1L, hints);
  }

  @Test
  @DisplayName("Spring Data JPA - @EntityGraph, @NamedEntityGraph")
  void testSpringDataJpa_EntityGraph() {
    memberRepository.findAllOfEntityGraph();
  }

  @Test
  void testSpringDataJpaFindAll_n_plus_one() {
    memberRepository.findAll();
  }

  /**
   * spring data jpa - findAll logic
   *
   * @see findAll : org.springframework.data.jpa.repository.support.SimpleJpaRepository#getQuery(Specification, Class, Sort)
   */
  @Test
  @DisplayName("JPQL fetch")
  void testJpqlFetch() {
    EntityManager em = getEntityManager();
//        em.createQuery("select m from Member as m", Member.class).getResultList();
    em.createQuery("select m "
            + "from Member as m "
            + "inner join fetch m.memberOption mo", Member.class)
            .getResultList();
  }

  /**
   * spring data jpa - findAll logic
   *
   * @see findAll : org.springframework.data.jpa.repository.support.SimpleJpaRepository#getQuery(Specification, Class, Sort)
   */
  @Test
  @DisplayName("Criteria Spring Data JPA findAll()")
  void testCriteria() {
    EntityManager em = getEntityManager();
//        List<Member> list = em.createQuery("select m from Member as m", Member.class)
//                .getResultList();

    Class<Member> domainClass = Member.class;
    CriteriaBuilder cb = em.getCriteriaBuilder();

    CriteriaQuery<Member> query = cb.createQuery(domainClass);
    Root<Member> m = query.from(domainClass);
    m.alias("m");
    query.select(m);
    List<Member> list = em.createQuery(query).getResultList();
  }

  @Test
  @DisplayName("Criteria 특정 컬럼을 선택하여 조회하기")
  void testCriteria_should_specify_columns() {
    EntityManager em = getEntityManager();

    Class<Member> domainClass = Member.class;
    CriteriaBuilder cb = getEntityManager()
            .getCriteriaBuilder();

    CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

    Root<Member> root = query.from(domainClass);
    Selection<Long> id = root.get("id");
    Selection<String> name = root.get("name");

    query.select(cb.construct(Object[].class, id, name));
    em.createQuery(query).getResultList();
  }

  @Test
  @DisplayName("queryDSL fetchJoin - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
  void testOneToOneWhenQueryDsl() {
//        memberRepository.findById(1L);
    QMember member = QMember.member;
    QMemberOption memberOption = QMemberOption.memberOption;

//      select member1 from Member member1 left join fetch member1.memberOption as memberOption
    getJPAQuery().select(member)
            .from(member)
            .leftJoin(member.memberOption, memberOption).fetchJoin()
            .limit(1)
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
   */
  @Test
  @DisplayName("@EntityGraph - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
  void testOneToOneWhenEntityGraph() {
    Member member = memberRepository.findByName("gmoon");
    log.debug("member : {}", member);
  }

  @Test
  void testFetchJoin_QueryDsl() {
    memberRepository.findAllOfQueryDsl();
  }

  @Test
  void testFetchJoin_QueryDsl_With_Projection() {
    memberRepository.findAllOfQueryDslWithProjection();
  }

  /**
   * org.springframework.orm.jpa.JpaSystemException: ids for this class must be manually assigned before calling save()
   * -> 영속 상태가 아니기 때문에 ID가 존재하지 않았음, Id가 아니기 때문
   *
   * @see MemberOption#setMember(Member)
   */
  @Test
  @DisplayName("대상 테이블 OneToOne 저장")
  void testSaveMemberOption() {
    // given
    Member member = memberRepository.getOne(TEST_MEMBER_ID_FOR_ACCOUNT_OF_GMOON);

    // when
    member.enabled();

    // then
    memberRepository.save(member);
  }
}