package com.gmoon.springpoi.excels.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(callSuper = false, of = {"id"})
@ToString(of = "id")
public class ExcelUploadJob {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@Column(length = ColumnLength.SHA_256, nullable = false, updatable = false)
	private String signature;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private ExcelUploadJobStatus status;

	@OneToMany(mappedBy = "job", cascade = CascadeType.PERSIST)
	private List<ExcelUploadTask> tasks = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private int totalRows;

	@Column(nullable = false)
	private int processedRows;

	@Column(nullable = false)
	private int invalidRows;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public ExcelUploadJob(ExcelSheetType sheetType, int totalRows, int chunkSize) {
		this.signature = sheetType.signature;
		this.sheetType = sheetType;
		this.status = ExcelUploadJobStatus.REQUEST_RECEIVED;

		this.totalRows = totalRows;
		this.processedRows = 0;
		this.invalidRows = 0;

		this.tasks = getExcelUploadTasks(totalRows, chunkSize);
		this.createdAt = Instant.now();
	}

	private List<ExcelUploadTask> getExcelUploadTasks(int totalRows, int chunkSize) {
		int total = 0;
		List<ExcelUploadTask> result = new ArrayList<>();
		for (int start = 0; start < totalRows; start += chunkSize) {
			int end = Math.min(start + chunkSize, totalRows); // exclusive
			ExcelUploadTask task = new ExcelUploadTask(this, start, end);
			result.add(task);

			total += task.getTotalRows();
		}

		if (total != totalRows) {
			throw new RuntimeException("total rows mismatch");
		}
		return result;
	}
}
