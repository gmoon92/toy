package com.gmoon.hibernateenvers.revision.infra;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistory;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;

public interface RevisionHistoryJpaRepository extends JpaRepository<RevisionHistory, Long> {

	@EntityGraph(attributePaths = "revision")
	List<RevisionHistory> findAllByRevisionIdAndTarget(Long revisionId, RevisionTarget target);
}
