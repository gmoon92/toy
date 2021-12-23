package com.gmoon.spreadsheet.utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoogleSpreadSheetToXmlDataSet {

	private static Environment properties;

	private static final String TARGET_DIR_PATH = "src/test/resources/";

	private static final String DEFAULT_DATA_YELLOW_RGB_COLOR = "1.0#1.0#null";

	public GoogleSpreadSheetToXmlDataSet(Environment environment) {
		properties = environment;
		log.debug("Environment load...", environment);
	}

	public static void generate(String sampleDataFileName, String defaultDataFileName) throws Exception {
		log.debug("Create a DataSet XML file read from Google Spreadsheet...");
		String spreadsheetId = properties.getProperty("google.spreadsheet.data-set.id");
		Spreadsheet spreadsheet = GoogleApiService.getSpreadsheetIncludeGridData(spreadsheetId);

		generateSampleData(sampleDataFileName, spreadsheet);
		generateDefaultData(defaultDataFileName, spreadsheet);
		log.debug("Created a DataSet XML files...");
	}

	public static void generateSampleData(String fileName, Spreadsheet spreadsheet) throws Exception {
		String xmlString = getDataSetXmlString(spreadsheet, false);
		output(fileName, xmlString);
	}

	public static void generateDefaultData(String fileName, Spreadsheet spreadsheet) throws Exception {
		String xmlString = getDataSetXmlString(spreadsheet, true);
		output(fileName, xmlString);
	}

	private static void output(String fileName, String xmlString) throws Exception {
		String pathname = TARGET_DIR_PATH.concat(fileName).concat(".xml");
		File file = new File(pathname);
		PrintWriter out = new PrintWriter(file, "UTF-8");
		out.println(xmlString);
		out.close();
		log.info("Created file : {}", pathname);
	}

	private static String getDataSetXmlString(Spreadsheet spreadsheet, boolean ignoreTestData) {
		log.trace("generate start...");
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("\n<dataset>");
			for (TableNode tableNode : getTableNodeList(spreadsheet, ignoreTestData)) {
				sb.append(String.format("\n<table name=\"%s\">", tableNode.getNameAttribute()))
					.append(getColumnElementStringBuffer(tableNode))
					.append(getRowElementStringBuffer(tableNode))
					.append("\n</table>");
			}
			sb.append("\n</dataset>");
			log.trace("generate end...");
		} catch (Exception e) {
			throw new RuntimeException("Error creating DataSet XML file...", e);
		}
		return sb.toString();
	}

	private static StringBuffer getColumnElementStringBuffer(TableNode tableNode) {
		StringBuffer sb = new StringBuffer();
		for (String columnNode : tableNode.getColumnElements()) {
			sb.append(String.format("\n    <column>%s</column>", columnNode));
		}
		return sb;
	}

	private static StringBuffer getRowElementStringBuffer(TableNode tableNode) {
		StringBuffer sb = new StringBuffer();
		for (TableNode.RowNode rowNode : tableNode.getRowElements()) {
			sb.append("\n    <row>")
				.append(getValueElementStringBuffer(rowNode))
				.append("\n    </row>");
		}
		return sb;
	}

	private static StringBuffer getValueElementStringBuffer(TableNode.RowNode rowNode) {
		StringBuffer sb = new StringBuffer();
		for (TableNode.RowNode.ValueNode valueNode : rowNode.getValueElements()) {
			String columnValue = valueNode.getValue();
			if (StringUtils.isBlank(columnValue)) {
				sb.append("\n         <null />");
			} else {
				String columnName = valueNode.getDescription();
				sb.append(String.format("\n         <value description=\"%s\">%s</value>", columnName,
					StringEscapeUtils.escapeXml(columnValue)));
			}
		}
		return sb;
	}

	private static List<TableNode> getTableNodeList(Spreadsheet spreadsheet, boolean ignoreTestData) {
		List<TableNode> result = new ArrayList<>();
		List<Sheet> sheets = spreadsheet.getSheets();
		for (int i = 0; i < sheets.size(); i++) {
			TableNode tableNode = new TableNode();

			Sheet sheet = sheets.get(i);
			String tableName = sheet.getProperties().getTitle();
			tableNode.setNameAttribute(tableName);

			List<RowData> rowDataList = sheet.getData()
				.stream()
				.flatMap(gridData -> gridData.getRowData().stream())
				.collect(Collectors.toList());

			setColumnElements(tableNode, rowDataList);
			setRowElements(tableNode, rowDataList, ignoreTestData);

			result.add(tableNode);
		}
		return result.stream()
			.filter(TableNode::verify)
			.collect(Collectors.toList());
	}

	private static void setRowElements(TableNode tableNode, List<RowData> rowDataList, boolean ignoreTestData) {
		rowDataList.stream()
			.filter(GoogleSpreadSheetToXmlDataSet::isCellDataNotEmpty)
			.skip(1)
			.forEach(rowData -> {
				List<String> columnElements = tableNode.getColumnElements();
				TableNode.RowNode rowNode = getRowNode(columnElements, rowData.getValues(), ignoreTestData);
				tableNode.addRowElement(rowNode);
			});
	}

	private static void setColumnElements(TableNode tableNode, List<RowData> rowDataList) {
		rowDataList.stream()
			.filter(GoogleSpreadSheetToXmlDataSet::isCellDataNotEmpty)
			.findFirst()
			.ifPresent(rowData -> tableNode.setColumnElements(getColumnElements(rowData.getValues())));
	}

	private static boolean isCellDataNotEmpty(RowData rowData) {
		return !CollectionUtils.isEmpty(rowData.getValues());
	}

	private static List<String> getColumnElements(List<CellData> cellDataList) {
		return cellDataList.stream()
			.map(CellData::getFormattedValue)
			.filter(StringUtils::isNotBlank)
			.collect(Collectors.toList());
	}

	private static TableNode.RowNode getRowNode(List<String> columnElements, List<CellData> cellDataList,
		boolean ignoreTestData) {
		TableNode.RowNode rowNode = new TableNode.RowNode();

		for (int i = 0; i < columnElements.size(); i++) {
			String value = null;
			try {
				CellData cellData = cellDataList.get(i);
				value = StringUtils.defaultString(cellData.getFormattedValue());
				if (i == 0) {
					if (StringUtils.isBlank(value))
						break;
					boolean isDefaultData = isDefaultRowData(cellData);
					if (ignoreTestData && !isDefaultData)
						break;
					rowNode.setDefaultData(isDefaultData);
				}
			} catch (IndexOutOfBoundsException ex) {
				//        log.trace("cell value is null...");
			}

			rowNode.addValueElement(columnElements.get(i), value);
		}

		return rowNode;
	}

	private static boolean isDefaultRowData(CellData cellData) {
		CellFormat format = cellData.getEffectiveFormat();
		if (format != null && !format.isEmpty()) {
			Color backgroundColor = format.getBackgroundColor();
			Float red = backgroundColor.getRed();
			Float green = backgroundColor.getGreen();
			Float blue = backgroundColor.getBlue();
			return DEFAULT_DATA_YELLOW_RGB_COLOR.equals(String.format("%s#%s#%s", red, green, blue));
		}
		return false;
	}

	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	private static class TableNode {

		private String nameAttribute;

		private List<String> columnElements = new ArrayList<>();

		private List<RowNode> rowElements = new ArrayList<>();

		public boolean verify() {
			return !CollectionUtils.isEmpty(this.getRowElements());
		}

		public void addRowElement(RowNode rowNode) {
			if (rowNode.verify())
				this.getRowElements().add(rowNode);
		}

		@Getter
		@Setter
		@ToString
		@NoArgsConstructor(access = AccessLevel.PRIVATE)
		private static class RowNode {

			private boolean defaultData;

			private List<ValueNode> valueElements = new ArrayList<>();

			public boolean verify() {
				return !CollectionUtils.isEmpty(this.getValueElements());
			}

			public void addValueElement(String description, String value) {
				ValueNode valueNode = ValueNode.createNew(description, value);
				this.getValueElements().add(valueNode);
			}

			@Getter
			@Setter
			@ToString
			@NoArgsConstructor(access = AccessLevel.PRIVATE)
			private static class ValueNode {

				private String description;

				private String value;

				public static ValueNode createNew(String description, String value) {
					ValueNode valueNode = new ValueNode();
					valueNode.setDescription(description);
					valueNode.setValue(value);
					return valueNode;
				}
			}
		}
	}
}
