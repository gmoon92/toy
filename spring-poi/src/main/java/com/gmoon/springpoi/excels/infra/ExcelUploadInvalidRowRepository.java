package com.gmoon.springpoi.excels.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springpoi.excels.domain.ExcelUploadInvalidRow;
import com.gmoon.springpoi.excels.domain.ExcelUploadTaskChunk;

public interface ExcelUploadInvalidRowRepository extends JpaRepository<ExcelUploadInvalidRow, String> {
	long countByChunk(ExcelUploadTaskChunk chunk);

	void removeAllByChunk(ExcelUploadTaskChunk chunk);
}
