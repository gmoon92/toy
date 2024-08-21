package com.gmoon.hibernateperformance.member.repository;

import static com.gmoon.hibernateperformance.member.domain.QMember.member;
import static com.gmoon.hibernateperformance.member.domain.QMemberOption.memberOption;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.gmoon.hibernateperformance.global.base.BaseRepositoryTest;
import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.member.domain.MemberOption;
import com.gmoon.hibernateperformance.member.domain.MemberRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class MemberRepositoryTest extends BaseRepositoryTest {

	private final MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		log.debug("Database data setup start...");
		memberRepository.deleteAllInBatch();
		memberRepository.saveAllAndFlush(
			 asList(
				  Member.newInstance("kim"),
				  Member.newInstance("lee"),
				  Member.newInstance("hong"),
				  Member.newInstance("kown"),
				  Member.newInstance("gmoon")
			 )
		);

		log.debug("Database data setup done...");
		flushAndClear();
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
	@DisplayName("연관 관계 맵핑 설정 - @OneToOne(optional = false)")
	@Test
	void oneToOneLazy() {
		Member member = memberRepository.findByName("gmoon");
		member.enabled();

		assertThat(member.getMemberOption())
			 .hasFieldOrPropertyWithValue("retired", true);
	}

	@Test
	@DisplayName("@Query fetch join")
	void fetchJoinQueryAnnotation() {
		assertThatCode(memberRepository::findAllOfJpqlQuery)
			 .doesNotThrowAnyException();
	}

	@DisplayName("findAll n+1")
	@Test
	void nPlusOneWhenSpringDataJpaFindAll() {
		assertThatCode(memberRepository::findAll)
			 .doesNotThrowAnyException();
	}

	/**
	 * spring data jpa - findAll logic
	 * entityManager.createQuery("select m from Member as m", Member.class).getResultList();
	 * @see findAll : org.springframework.data.jpa.repository.support.SimpleJpaRepository#getQuery(Specification, Class, Sort)
	 */
	@DisplayName("JPQL fetch")
	@Test
	void jpqlFetch() {
		String jpql = "select m "
			 + "from Member as m "
			 + "inner join fetch m.memberOption mo";

		assertThatCode(() -> entityManager.createQuery(jpql, Member.class).getResultList())
			 .doesNotThrowAnyException();
	}

	/**
	 * org.springframework.orm.jpa.JpaSystemException: ids for this class must be manually assigned before calling save()
	 * -> 영속 상태가 아니기 때문에 ID가 존재하지 않았음, Id가 아니기 때문
	 *
	 * @see MemberOption#setMember(Member)
	 */
	@DisplayName("대상 테이블 OneToOne 저장")
	@Test
	void saveMemberOption() {
		Member member = memberRepository.findByName("gmoon");
		member.enabled();

		assertThatCode(() -> memberRepository.save(member))
			 .doesNotThrowAnyException();
	}

	@DisplayName("update where sub-query")
	@Test
	void bulkUpdateRetireMembers() {
		assertThatCode(memberRepository::bulkUpdateRetireMembers)
			 .doesNotThrowAnyException();
	}

	@Nested
	class EntityGraphTest {

		@DisplayName("EntityManager - @NamedEntityGraph")
		@Test
		void namedEntityGraphAnnotation() {
			EntityGraph<?> graph = entityManager.getEntityGraph("Member.withMemberOption");
			Map<String, Object> hints = new HashMap<>();
			hints.put("jakarta.persistence.fetchgraph", graph);

			assertThatCode(() -> entityManager.find(Member.class, 1L, hints))
				 .doesNotThrowAnyException();

			String jpql = "select m from Member m"
				 + " inner join fetch m.memberOption";
			//                      ^-- JPQL은 글로벌 패치를 고려하지 않고
			//                      항상 외부 조인을 사용하기 때문에 inner join fetch 명시한다.
			Query query = entityManager.createQuery(jpql)
				 .setHint("jakarta.persistence.fetchgraph", graph);

			assertThatCode(query::getResultList)
				 .doesNotThrowAnyException();
		}

		@DisplayName("EntityManager - @EntityGraph")
		@Test
		void entityManager() {
			EntityGraph<Member> graph = entityManager.createEntityGraph(Member.class);
			graph.addAttributeNodes("memberOption");

			Map<String, Object> hints = new HashMap<>();
			hints.put("jakarta.persistence.fetchgraph", graph);

			assertThatCode(() -> entityManager.find(Member.class, 1L, hints))
				 .doesNotThrowAnyException();
		}

		@DisplayName("SpringDataJPA - @EntityGraph")
		@Test
		void springDataJpa() {
			assertThatCode(memberRepository::findAllOfEntityGraph)
				 .doesNotThrowAnyException();
		}
	}

	@Nested
	class CriteriaTest {

		/**
		 * spring data jpa - findAll logic
		 * List<Member> list = entityManager.createQuery("select m from Member as m", Member.class).getResultList()
		 * @see findAll : org.springframework.data.jpa.repository.support.SimpleJpaRepository#getQuery(Specification, Class, Sort)
		 */
		@DisplayName("Criteria Spring Data JPA findAll()")
		@Test
		void getResultList() {
			CriteriaQuery<Member> query = entityManager.getCriteriaBuilder()
				 .createQuery(Member.class);

			Selection<Member> projections = query.from(Member.class)
				 .alias("m");

			List<Member> list = entityManager.createQuery(
					  query.select(projections)
				 )
				 .getResultList();

			assertThat(list).isNotEmpty();
		}

		@DisplayName("Criteria 특정 컬럼을 선택하여 조회하기")
		@Test
		void projections() {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<MemberProjections> query = cb.createQuery(MemberProjections.class);

			Root<Member> root = query.from(Member.class);
			Selection<Long> id = root.get("id").as(Long.class).alias("id");
			Selection<String> name = root.get("name").as(String.class).alias("name");

			List<MemberProjections> result = entityManager.createQuery(
					  query.select(cb.construct(MemberProjections.class, id, name))
				 )
				 .getResultList();

			assertThat(result).isNotEmpty();
		}

		record MemberProjections(Long id, String name) {}
	}

	@Nested
	class OneToOneTest {

		/**
		 * memberRepository.findById(1L);
		 * select member1 from Member member1 left join fetch member1.memberOption as memberOption
		 */
		@DisplayName("queryDSL fetchJoin - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
		@Test
		void whenQueryDsl() {
			JPAQuery<Member> query = new JPAQueryFactory(entityManager)
				 .select(member)
				 .from(member)
				 .leftJoin(member.memberOption, memberOption).fetchJoin()
				 .limit(1);

			assertThatCode(query::fetchOne)
				 .doesNotThrowAnyException();
		}

		/***
		 * @see Member#memberOption
		 * @see org.hibernate.annotations.Fetch
		 * @see org.hibernate.annotations.FetchMode
		 * */
		@DisplayName("@Fetch - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
		@Test
		void whenFetchAnnotation() {
			assertThatCode(() -> entityManager.find(Member.class, 1L))
				 .doesNotThrowAnyException();
		}

		/**
		 * @see Member
		 * @see MemberRepository
		 * @see org.springframework.data.jpa.repository.EntityGraph
		 * @see jakarta.persistence.NamedEntityGraph
		 */
		@DisplayName("@EntityGraph - 식별 관계 주 테이블(Member) OneToOne 양방향 관계")
		@Test
		void whenEntityGraph() {
			assertThatCode(() -> memberRepository.findByName("gmoon"))
				 .doesNotThrowAnyException();
		}
	}

	@Nested
	class FetchJoinTest {

		@Test
		void queryDsl() {
			assertThatCode(memberRepository::findAllOfQueryDsl)
				 .doesNotThrowAnyException();
		}

		@Test
		void queryDslWithProjection() {
			assertThatCode(memberRepository::findAllOfQueryDslWithProjection)
				 .doesNotThrowAnyException();
		}
	}
}
