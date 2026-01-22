package com.gmoon.hibernateperformance.member.domain;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	/**
	 * 특히 특정 로직의 경우 항상 모든 필드를 로드에 해줘야 하는데 lazy로 되어 있으니
	 * 꼭 한번 더 쿼리가 날라 가는 경우 짜증나니까
	 * 어떤 쿼리 로직을 실행할 때만큼음 한번에 모든 데이터를 로드할 때 사용
	 * <p>
	 * 다시 위에 코드에서 보면 memberOption 관련은 lazy로 로드하지만
	 * findByName 를 호출하를 경우 "Member.options" 라고
	 * 이름을 정한 EntityGraph 정보를 이용하여 호출함으로
	 * lazy가 아닌 eager로 로드 함
	 *
	 * @see org.springframework.data.jpa.repository.EntityGraph.EntityGraphType
	 */
	//    https://docs.spring.io/spring-data/data-jpa/docs/current/reference/html/#jpa.entity-graph
	//    @EntityGraph(value = "Member.withMemberOption", type = EntityGraph.EntityGraphType.FETCH)
	//    @EntityGraph(value = "Member.withMemberOption", type = EntityGraph.EntityGraphType.LOAD)
	@EntityGraph(attributePaths = "memberOption")
	Member findByName(String name);

	@Query("select m from Member m inner join fetch m.memberOption mo")
	List<Member> findAllOfJpqlQuery();

	//    @EntityGraph(value = "Member.withMemberOption", attributePaths = {"memberOption"})
	//    @EntityGraph(attributePaths = {"memberOption"})
	@EntityGraph(value = "Member.withMemberOption")
	@Query("select m from Member m")
	// 엔티티 그래프 root 설정
	List<Member> findAllOfEntityGraph();
}
