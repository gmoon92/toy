package com.gmoon.hibernateenvers.revision.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus;
import com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget;
import com.gmoon.hibernateenvers.revision.model.RevisionListVO;

@Transactional(readOnly = true)
public interface RevisionHistoryRepository {

	List<RevisionHistory> findAll();

	List<RevisionHistory> findAll(Long revisionNumber, RevisionTarget target);

	Page<RevisionListVO.DataVO> findAll(RevisionListVO.SearchVO searchVO);

	Optional<RevisionHistory> findPrevious(RevisionHistory revisionHistory);

	void updateStatus(Long id, RevisionStatus status);
}
