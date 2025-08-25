package com.gmoon.springpoi.excels.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;
import com.gmoon.springpoi.base.persistence.converter.ExcelCellErrorMessageConverter;
import com.gmoon.springpoi.base.persistence.converter.ExcelCellValuesConverter;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@ToString
public class ExcelUploadInvalidRow {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "chunk_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ExcelUploadTaskChunk chunk;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelSheetType sheetType;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false, updatable = false)
	private ExcelInvalidRowType type;

	@Setter
	@Lob
	@Convert(converter = ExcelCellValuesConverter.class)
	@Column(nullable = false)
	private ExcelCellValues originCellValues = new ExcelCellValues();

	@Lob
	@Convert(converter = ExcelCellErrorMessageConverter.class)
	@Column
	private ExcelCellErrorMessages errorMessages = new ExcelCellErrorMessages();

	public ExcelUploadInvalidRow(ExcelUploadTaskChunk chunk) {
		this.chunk = chunk;
		this.sheetType = chunk.getSheetType();
		this.type = ExcelInvalidRowType.VALIDATION;
		this.originCellValues = new ExcelCellValues();
		this.errorMessages = new ExcelCellErrorMessages();
	}

	public void setInvalidCellValues(ExcelCellValues cellValues, ExcelCellErrorMessages errorMessages) {
		this.originCellValues = cellValues;
		this.errorMessages = errorMessages;
	}

	public void setInvalidType(ExcelInvalidRowType type) {
		this.type = type;
	}
}
