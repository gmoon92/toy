package com.gmoon.springpoi.excels.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(of = "id")
public class ExcelUploadTask {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@OneToMany(mappedBy = "task", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private List<ExcelUploadTaskChunk> chunks = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	@Column(length = ColumnLength.SHA_256, nullable = false, updatable = false)
	private String signature;

	@ColumnDefault("'PREPARING'")
	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private ExcelUploadTaskStatus status;

	@ColumnDefault("'ko'")
	@Column(length = ColumnLength.CODE10, updatable = false)
	private String language;

	@ColumnDefault("'KR'")
	@Column(length = ColumnLength.CODE10, updatable = false)
	private String country;

	@Column(length = ColumnLength.CODE10, nullable = false, updatable = false)
	private String timezone;

	@Column(length = ColumnLength.FILENAME, nullable = false, updatable = false)
	private String filename;

	@Column(length = ColumnLength.FILENAME, nullable = false, updatable = false)
	private String originFilename;

	@Column(nullable = false, updatable = false)
	private long totalRowCount;

	@ColumnDefault("0")
	@Column(nullable = false)
	private long processedRowCount;

	@ColumnDefault("0")
	@Column(nullable = false)
	private long invalidRowCount;

	@Column
	private Instant processingStartedAt;

	@ColumnDefault("0")
	@Column(nullable = false)
	private boolean completed;

	@Column
	private Instant completedAt;

	@Column(updatable = false)
	private Instant createdAt;

	public ExcelUploadTask(ExcelSheetType sheetType, long totalRowCount, long chunkSize) {
		this.signature = sheetType.getSignature();
		this.sheetType = sheetType;
		this.status = ExcelUploadTaskStatus.PREPARING;

		this.totalRowCount = totalRowCount;
		this.processedRowCount = 0;
		this.invalidRowCount = 0;
		this.completed = false;
		this.chunks = createChunks(totalRowCount, chunkSize);
		this.createdAt = Instant.now();
	}

	public ExcelUploadTask withRequesterLocale(Locale locale, String timezone) {
		this.language = locale.getLanguage();
		this.country = locale.getCountry();
		this.timezone = timezone;
		return this;
	}

	public ExcelUploadTask withFilename(String originFilename) {
		this.originFilename = originFilename;
		this.filename = UUID.randomUUID() + getFileExtension(originFilename);
		return this;
	}

	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new IllegalArgumentException("Invalid file name: missing file extension.");
		}

		int lastDotIndex = filename.lastIndexOf(".");
		if (lastDotIndex == filename.length() - 1) {
			throw new IllegalArgumentException("Invalid file name: extension not found.");
		}

		String extension = filename.substring(lastDotIndex).toLowerCase();
		if (!extension.matches("\\.(xlsx)")) {
			throw new IllegalArgumentException("Unsupported file extension: " + extension);
		}
		return extension;
	}

	private List<ExcelUploadTaskChunk> createChunks(long totalRowCount, long chunkSize) {
		long totalRows = 0;
		List<ExcelUploadTaskChunk> chunks = new ArrayList<>();
		for (long startIdx = 0; startIdx < totalRowCount; startIdx += chunkSize) {
			long endIdx = Math.min(startIdx + chunkSize, totalRowCount);
			ExcelUploadTaskChunk chunk = new ExcelUploadTaskChunk(this, startIdx, endIdx);
			chunks.add(chunk);

			totalRows += chunk.getTotalRowCount();
		}
		if (totalRowCount != totalRows) {
			throw new IllegalStateException(
				 "The sum of rows in all chunks does not match the total data row count. "
					  + "taskId=" + id + ", dataRowCount=" + totalRowCount + ", chunkSum=" + totalRows
			);
		}
		return chunks;
	}

	public void startProcessing() {
		if (completed || isProcessing()) {
			return;
		}

		this.status = ExcelUploadTaskStatus.PROCESSING;
		this.processingStartedAt = Instant.now();
	}

	public boolean isProcessing() {
		return ExcelUploadTaskStatus.PROCESSING == status;
	}

	public void mergeChunkResults() {
		if (completed) {
			return;
		}

		this.processedRowCount = getAllChunkProcessedRowCount();
		this.invalidRowCount = getAllChunkInvalidRowCount();

		if (isAllChunkCompleted()) {
			boolean successful = isAllSuccessful();
			this.status = successful ? ExcelUploadTaskStatus.SUCCESS : ExcelUploadTaskStatus.FAILED;
			this.completed = true;
			this.completedAt = Instant.now();
		}
	}

	private boolean isAllChunkCompleted() {
		return chunks.stream()
			 .allMatch(ExcelUploadTaskChunk::isCompleted);
	}

	private boolean isAllSuccessful() {
		return chunks.stream()
			 .allMatch(ExcelUploadTaskChunk::isSuccessful);
	}

	private long getAllChunkProcessedRowCount() {
		return chunks.stream()
			 .filter(ExcelUploadTaskChunk::isCompleted)
			 .mapToLong(ExcelUploadTaskChunk::getProcessedRowCount)
			 .sum();
	}

	private long getAllChunkInvalidRowCount() {
		return chunks.stream()
			 .filter(ExcelUploadTaskChunk::isCompleted)
			 .mapToLong(ExcelUploadTaskChunk::getInvalidRowCount)
			 .sum();
	}

	public Locale getLocale() {
		return Locale.of(language, country);
	}

	public Path getExcelUploadPath(Path storagePath) {
		LocalDate yyyyMMdd = LocalDate.ofInstant(createdAt, ZoneId.systemDefault());
		String tmpPath = yyyyMMdd.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		Path excelUploadPath = Path.of(tmpPath, filename);

		return storagePath.resolve(excelUploadPath);
	}
}
