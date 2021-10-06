package com.gmoon.hibernateenvers.revision;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import com.gmoon.hibernateenvers.revision.enums.RevisionEventStatus;
import com.gmoon.hibernateenvers.revision.enums.RevisionTarget;
import com.gmoon.hibernateenvers.revision.vo.RevisionListVO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface RevisionHistoryDetailRepositoryQueryDsl {

  void updateEventStatus(Long revisionModifiedEntityId, RevisionEventStatus eventStatus);

  Optional<RevisionHistoryDetail> findPreRevisionHistoryDetail(RevisionHistoryDetail modified);

  List<RevisionHistoryDetail> findAllByRevisionAndTarget(Long revisionNumber, RevisionTarget target);

  Page<RevisionListVO.DataVO> findAllBySearchVO(RevisionListVO.SearchVO searchVO);
}
