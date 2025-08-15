package com.gmoon.springpoi.excels.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springpoi.excels.domain.ExcelUploadJob;

public interface ExcelUploadJobRepository extends JpaRepository<ExcelUploadJob, String> {
}
