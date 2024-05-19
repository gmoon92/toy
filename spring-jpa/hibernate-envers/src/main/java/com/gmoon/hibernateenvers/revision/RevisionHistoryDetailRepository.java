package com.gmoon.hibernateenvers.revision;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryDetail;

public interface RevisionHistoryDetailRepository extends JpaRepository<RevisionHistoryDetail, Long>
	 , RevisionHistoryDetailRepositoryQueryDsl
	 , AuditedEntityRepository {

}
