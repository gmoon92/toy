package com.gmoon.springpoi.excels.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gmoon.springpoi.excels.domain.ExcelUploadTask;

import jakarta.persistence.LockModeType;

public interface ExcelUploadTaskRepository extends JpaRepository<ExcelUploadTask, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from ExcelUploadTask t where t.id = :id")
	ExcelUploadTask getWithLock(@Param("id") String id);
}
