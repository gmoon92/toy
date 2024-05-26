package com.gmoon.hibernateannotation.members.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

@DataJpaTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void testDelete() {
		// given
		Member newbie = new Member();
		Member savedMember = memberRepository.save(newbie);

		// when
		memberRepository.delete(savedMember);
		flushAndClear();

		// then
		assertThat(memberRepository.findById(savedMember.getId()))
			 .isEqualTo(Optional.empty());
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
