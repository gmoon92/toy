package com.gmoon.hibernateannotation.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {

	@Autowired MemberRepository memberRepository;
	@Autowired EntityManager entityManager;

	@Test
	void testDelete() {
		// given
		long id = 0l;

		// when
		memberRepository.deleteById(id);
		flushAndClear();

		// then
		assertThat(memberRepository.findById(id))
			.isEqualTo(Optional.empty());
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
