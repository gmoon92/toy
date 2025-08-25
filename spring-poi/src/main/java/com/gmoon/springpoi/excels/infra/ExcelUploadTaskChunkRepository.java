package com.gmoon.springpoi.excels.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springpoi.excels.domain.ExcelUploadTaskChunk;

public interface ExcelUploadTaskChunkRepository extends JpaRepository<ExcelUploadTaskChunk, String> {
}
