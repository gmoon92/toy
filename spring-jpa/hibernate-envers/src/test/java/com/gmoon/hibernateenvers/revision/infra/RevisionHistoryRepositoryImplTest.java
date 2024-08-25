package com.gmoon.hibernateenvers.revision.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryRepository;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;
import com.gmoon.hibernateenvers.revision.model.RevisionListVO;
import com.gmoon.hibernateenvers.test.RepositoryTest;

@RepositoryTest
class RevisionHistoryRepositoryImplTest {

	@Autowired
	private RevisionHistoryRepository repository;

	@Test
	void findAll() {
		assertThatCode(() -> repository.findAll()).doesNotThrowAnyException();
		assertThatCode(() -> repository.findAll(1L, RevisionTarget.MEMBER)).doesNotThrowAnyException();
		assertThatCode(() -> repository.findAll(new RevisionListVO.SearchVO())).doesNotThrowAnyException();
	}

	@Test
	void updateStatus() {
		assertThatCode(() -> repository.updateStatus(1L, RevisionStatus.DIRTY_CHECKING))
			 .doesNotThrowAnyException();
	}
}
