package com.gmoon.quickperf.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.quickperf.domain.Company;
import com.gmoon.quickperf.domain.Member;
import com.gmoon.quickperf.domain.Team;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class InitTestDataExecutor {
	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void init() {
		log.info("init sample data");
		Company company = merge(Company.create("google"));

		Team newbie = merge(Team.create(company, "newbie"));
		saveMember(newbie, "newbie-park");

		Team backend = merge(Team.create(company, "backend"));
		saveMember(backend, "gmoon");
		saveMember(backend, "kim");
		saveMember(backend, "lee");

		flushAndClear();
	}

	private void saveMember(Team team, String memberName) {
		persist(Member.create(team, memberName));
	}

	private <T> void persist(T entity) {
		entityManager.persist(entity);
	}

	private <T> T merge(T entity) {
		return entityManager.merge(entity);
	}

	protected void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
