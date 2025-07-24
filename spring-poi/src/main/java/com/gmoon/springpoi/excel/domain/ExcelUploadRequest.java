package com.gmoon.springpoi.excel.domain;

import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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
@ToString
public class ExcelUploadRequest {

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
	private ExcelUploadStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public ExcelUploadRequest(ExcelSheetType sheetType) {
		this.signature = sheetType.signature;
		this.sheetType = sheetType;
		this.status = ExcelUploadStatus.REQUEST_RECEIVED;
		this.createdAt = Instant.now();
	}
}
