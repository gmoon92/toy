package com.gmoon.quickperf.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.quickperf.domain.Member;
import com.gmoon.quickperf.domain.Team;
import com.gmoon.quickperf.test.InitTestDataExecutor;

@Disabled("24.05.26 기준으로 spring boot 3 호환성 문제 발생으로 테스트 검증 제외"
	 + "참고 링크 https://github.com/quick-perf/quickperf/issues/196")
@SpringBootTest
@ExtendWith(QuickPerfTestExtension.class)
class TeamServiceTest extends InitTestDataExecutor {

	@Autowired
	private TeamService service;

	private Team backend;

	@BeforeEach
	void setUp() {
		backend = service.findByName("backend");
	}

	@Test
	@Transactional
	@ExpectUpdate
	@ExpectSelect(3)
	@DisplayName("기존의 팀의 맴버들을 새로운 팀으로 할당한다")
	void testMoveAllMemberToAssignedNewTeam() {
		// given
		Team newbie = service.findByName("newbie");
		List<Member> newbies = newbie.getMembers();

		// when
		service.moveAllMemberToAssignedNewTeam(newbies, backend.getId());

		// then
		flushAndClear();
	}
}
