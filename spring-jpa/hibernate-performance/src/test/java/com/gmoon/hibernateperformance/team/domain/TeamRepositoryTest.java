package com.gmoon.hibernateperformance.team.domain;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.gmoon.hibernateperformance.global.base.BaseRepositoryTest;
import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.member.domain.MemberOption;
import com.gmoon.hibernateperformance.member.domain.MemberRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TeamRepositoryTest extends BaseRepositoryTest {

	private final TeamRepository teamRepository;
	private final MemberRepository memberRepository;

	@BeforeEach
	void setup() {
		log.info("Database data setup start...");
		memberRepository.deleteAll();
		teamRepository.deleteAll();

		List<Member> savedMembers = memberRepository.saveAll(
			 asList(
				  Member.newInstance("gmoon"),
				  Member.newInstance("anonymousUser1")
			 ));

		Team web1 = Team.newInstance("web1");
		web1.addMembers(savedMembers);
		teamRepository.save(web1);
		flushAndClear();
		log.info("Database data setup done...");
	}

	@DisplayName("컬렉션 다루기")
	@Test
	void addMember() {
		// given
		Member anonymousUser2 = memberRepository.save(Member.newInstance("anonymousUser2"));
		Member gmoon = memberRepository.findByName("gmoon");

		// when
		Team web1 = teamRepository.findByName("web1");
		web1.addMembers(asList(gmoon, anonymousUser2));

		// then
		assertThat(teamRepository.save(web1).getTeamMembers())
			 .hasSize(2)
			 .contains(new TeamMember(anonymousUser2, web1), new TeamMember(gmoon, web1));
	}

	@Test
	void findAll() {
		Set<Member> members = teamRepository.findAll().stream()
			 .map(Team::getTeamMembers)
			 .flatMap(Collection::stream)
			 .map(TeamMember::getMember)
			 .collect(Collectors.toSet());

		for (Member member : members) {
			MemberOption memberOption = member.getMemberOption();

			boolean initialized = Hibernate.isInitialized(memberOption);
			log.debug("member : {}, {}, {}", member.getName(), initialized, memberOption.getEnabled());
			assertThat(initialized).isTrue();
		}
	}
}
