package com.gmoon.springpoi.excels.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import com.gmoon.springpoi.base.persistence.constants.ColumnLength;
import com.gmoon.springpoi.base.persistence.converter.IntegerSetConverter;
import com.gmoon.springpoi.base.persistence.converter.JsonStringConverter;
import com.gmoon.springpoi.base.persistence.vo.JsonString;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@ToString
public class ExcelInvalidRow {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(length = ColumnLength.ID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ExcelUploadTask task;

	@Enumerated(EnumType.STRING)
	@Column(length = ColumnLength.ENUM, nullable = false)
	private ExcelInvalidRowType type;

	@Lob
	@Convert(converter = JsonStringConverter.class)
	@Column(nullable = false)
	private JsonString originCellValues = new JsonString();

	@Lob
	@Convert(converter = IntegerSetConverter.class)
	@Column(nullable = false)
	private Set<Integer> invalidColumnIndexes = new HashSet<>();

	public ExcelInvalidRow(ExcelUploadTask task) {
		this.task = task;
		this.originCellValues = new JsonString();
		this.invalidColumnIndexes = new HashSet<>();
		this.type = ExcelInvalidRowType.VALIDATION;
	}

	public void setOriginCellValues(Map<String, String> cellValues) {
		originCellValues.setValues(cellValues);
	}

	public void addInvalidCellColIndex(int colIndex) {
		invalidColumnIndexes.add(colIndex);
	}
}
