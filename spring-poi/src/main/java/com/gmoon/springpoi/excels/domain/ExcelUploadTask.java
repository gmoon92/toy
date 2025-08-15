package com.gmoon.springpoi.excels.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class ExcelUploadTask {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ExcelUploadJob job;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private ExcelUploadTaskStatus status;

	@Column(nullable = false, updatable = false)
	private long startRow;

	@Column(nullable = false, updatable = false)
	private long endRow;

	@Column(nullable = false, updatable = false)
	private long totalRows;

	@Column(nullable = false)
	private long processedRows;

	@Column(nullable = false)
	private long invalidRows;

	public ExcelUploadTask(ExcelUploadJob job, long startRow, long endRow) {
		this.job = job;
		this.status = ExcelUploadTaskStatus.STARTING;
		this.startRow = startRow;
		this.endRow = endRow;
		this.totalRows = endRow - startRow;
		this.processedRows = 0;
		this.invalidRows = 0;
	}
}
