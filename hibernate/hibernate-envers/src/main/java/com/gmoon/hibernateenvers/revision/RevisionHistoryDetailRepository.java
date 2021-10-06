package com.gmoon.hibernateenvers.revision;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionHistoryDetailRepository extends JpaRepository<RevisionHistoryDetail, Long>
        , RevisionHistoryDetailRepositoryQueryDsl
        , AuditedEntityRepository {

}