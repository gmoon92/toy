package com.gmoon.springpoi.excels.domain;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
public class ExcelUploadTaskChunk {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ExcelUploadTask task;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	@ColumnDefault("'PREPARING'")
	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private ExcelUploadChunkStatus status;

	@ColumnDefault("0")
	@Column(nullable = false)
	private int attemptCount;

	@ColumnDefault("0")
	@Column(nullable = false, updatable = false)
	private long totalRowCount;

	@ColumnDefault("0")
	@Column(nullable = false)
	private long processedRowCount;

	@ColumnDefault("0")
	@Column(nullable = false)
	private long invalidRowCount;

	@ColumnDefault("0")
	@Column(nullable = false, updatable = false)
	private long startIdx;

	@ColumnDefault("0")
	@Column(nullable = false, updatable = false)
	private long endIdx;

	@Column
	private Instant processingStartedAt;

	@ColumnDefault("0")
	@Column(nullable = false)
	private boolean completed;

	@Column
	private Instant completedAt;

	@Lob
	@Column
	private String errorMessage;

	public ExcelUploadTaskChunk(ExcelUploadTask task, long startIdx, long endIdx) {
		this.task = task;
		this.sheetType = task.getSheetType();
		this.status = ExcelUploadChunkStatus.PREPARING;
		this.attemptCount = 0;
		this.totalRowCount = endIdx - startIdx;
		this.processedRowCount = 0;
		this.invalidRowCount = 0;
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.completed = false;
	}

	public void startProcessing() {
		this.status = ExcelUploadChunkStatus.STARTED;
		this.attemptCount++;
		this.completed = false;
		this.processedRowCount = 0;
		this.invalidRowCount = 0;
		this.processingStartedAt = Instant.now();
	}

	public void complete(long invalidRowCount) {
		if (completed) {
			return;
		}

		this.completed = true;
		this.completedAt = Instant.now();
		this.invalidRowCount = invalidRowCount;
		this.processedRowCount = totalRowCount - invalidRowCount;

		if (status != ExcelUploadChunkStatus.FAILED) {
			boolean valid = invalidRowCount == 0;
			this.status = valid ? ExcelUploadChunkStatus.SUCCESS : ExcelUploadChunkStatus.PARTIAL_SUCCESS;
		}
	}

	public void fail(String errorMessage) {
		this.status = ExcelUploadChunkStatus.FAILED;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccessful() {
		return ExcelUploadChunkStatus.SUCCESS == status;
	}

	public String getTaskId() {
		return task.getId();
	}
}
