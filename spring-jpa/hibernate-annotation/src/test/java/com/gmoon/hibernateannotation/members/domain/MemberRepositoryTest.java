package com.gmoon.hibernateannotation.members.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateannotation.base.BaseRepositoryTest;

class MemberRepositoryTest extends BaseRepositoryTest {

	@Autowired
	private MemberRepository repository;

	@Test
	void test() {
		Member savedMember = repository.saveAndFlush(new Member());

		repository.delete(savedMember);

		assertThat(repository.findById(savedMember.getId())).isNotPresent();
	}
}
