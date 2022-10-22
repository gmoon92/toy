package com.gmoon.demo.team.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.demo.global.base.BaseRepositoryTest;
import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class TeamRepositoryTest extends BaseRepositoryTest {

	final TeamRepository teamRepository;
	final MemberRepository memberRepository;

	@BeforeEach
	@Transactional
	void setup() {
		log.info("Database data setup start...");
		memberRepository.deleteAll();
		teamRepository.deleteAll();

		List<Member> savedMembers = memberRepository.saveAll(
			Arrays.asList(
				Member.newInstance("gmoon"),
				Member.newInstance("anonymousUser1")
			));

		Team web1 = Team.newInstance("web1");
		web1.addMembers(savedMembers);
		teamRepository.save(web1);
		flushAndClear();
		log.info("Database data setup done...");
	}

	@Test
	@DisplayName("컬렉션 다루기")
	void addMember() {
		// given
		Member anonymousUser2 = memberRepository.save(Member.newInstance("anonymousUser2"));
		Member gmoon = memberRepository.findByName("gmoon");

		// when
		Team web1 = teamRepository.findByName("web1");
		web1.addMembers(Arrays.asList(gmoon, anonymousUser2));

		// then
		assertThat(teamRepository.save(web1).getTeamMembers())
			.hasSize(2)
			.contains(new TeamMember(anonymousUser2, web1), new TeamMember(gmoon, web1));
	}

	@Test
	void testFindAll() {
		List<Team> teamList = teamRepository.findAll();
		//        Hibernate.isInitialized();

		teamList.forEach(team -> {
			log.debug("team : {}", team);
			//            team.getMembers().forEach(member -> log.debug("member : {}, {}, {}", member.getName()
			//                    , Hibernate.isInitialized(member.getMemberOption())
			//                    , member.getMemberOption().isEnabled()));
		});
	}

	@AfterEach
	void tearDown() {
		flushAndClear();
	}
}
