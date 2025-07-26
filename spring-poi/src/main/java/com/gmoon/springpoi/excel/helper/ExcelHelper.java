package com.gmoon.springpoi.excel.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.excel.processor.CompositeEventListener;
import com.gmoon.springpoi.excel.processor.EventListener;
import com.gmoon.springpoi.excel.processor.ExcelBatchPostListener;
import com.gmoon.springpoi.excel.processor.SaxRowEventListener;
import com.gmoon.springpoi.excel.provider.ExcelValueProvider;
import com.gmoon.springpoi.excel.sax.handler.SaxXlsxSheetHandler;
import com.gmoon.springpoi.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.excel.vo.ExcelCell;
import com.gmoon.springpoi.excel.vo.ExcelField;
import com.gmoon.springpoi.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.excel.vo.ExcelRow;
import com.gmoon.springpoi.excel.vo.ExcelSheet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExcelComponent
@RequiredArgsConstructor
public class ExcelHelper {
	private final ApplicationContext ctx;

	public <T> void write(
		 OutputStream out,
		 Class<T> clazz,
		 List<T> dataList
	) {
		ExcelSheet<T> excelSheet = new ExcelSheet<>(ctx, clazz, dataList.size());
		ExcelModelMetadata metadata = excelSheet.getMetadata();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(1_000)) {
			String sheetName = metadata.getSheetName();
			Sheet sheet = wb.createSheet(sheetName);

			writeTitle(wb, sheet, metadata);
			writeData(sheet, metadata, dataList);

			wb.write(out);
			out.flush();
		} catch (IOException | NullPointerException ex) {
			throw new RuntimeException("Excel download error because : ", ex);
		}
	}

	private <T> void writeData(Sheet sheet, ExcelModelMetadata metadata, List<T> dataList) {
		int headerRowCount = metadata.getHeaderRowTotalCount();
		for (T data : dataList) {
			Row row = sheet.createRow(headerRowCount++);
			writeRow(row, metadata, data);
		}
	}

	private void writeTitle(Workbook wb, Sheet sh, ExcelModelMetadata metadata) {
		Row row = sh.createRow(0);
		Drawing<?> drawing = sh.createDrawingPatriarch();
		CreationHelper factory = wb.getCreationHelper();

		for (Map.Entry<Integer, ExcelField> entry : metadata.entrySet()) {
			Integer cellColIdx = entry.getKey();
			ExcelField excelField = entry.getValue();
			Field field = excelField.getField();

			ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
			Comment comment = createCellComment(row, cellColIdx, drawing, factory, annotation);

			String colTitle = Objects.toString(annotation.title(), field.getName());
			Cell cell = row.createCell(cellColIdx);
			cell.setCellValue(colTitle);
			cell.setCellComment(comment);
		}
	}

	private Comment createCellComment(
		 Row row,
		 int cellColIdx,
		 Drawing<?> drawing,
		 CreationHelper factory,
		 ExcelProperty annotation
	) {
		if (StringUtils.isBlank(annotation.comment())) {
			return null;
		}

		final int defaultAnchorColSize = 6;
		final int defaultAnchorRowSize = 4;

		ClientAnchor anchor = factory.createClientAnchor();
		anchor.setCol1(cellColIdx);
		anchor.setCol2(cellColIdx + defaultAnchorColSize);
		anchor.setRow1(row.getRowNum());
		anchor.setRow2(row.getRowNum() + defaultAnchorRowSize);

		Comment cellComment = drawing.createCellComment(anchor);
		cellComment.setString(factory.createRichTextString(annotation.comment()));
		return cellComment;
	}

	private <T> void writeRow(Row row, ExcelModelMetadata metadata, T data) {
		for (Map.Entry<Integer, ExcelField> entry : metadata.entrySet()) {
			int cellColIdx = entry.getKey();
			ExcelField excelField = entry.getValue();

			Field field = excelField.getField();
			try {
				Object fieldValue = field.get(data);
				if (fieldValue == null) {
					continue;
				}

				String cellValue = switch (fieldValue) {
					case Integer i -> Integer.toString(i);
					case Boolean b -> BooleanUtils.toString(b, "Y", "N");
					case ExcelValueProvider provider -> provider.getExcelCellValue();
					default -> String.valueOf(fieldValue);
				};

				Cell cell = row.createCell(cellColIdx);
				cell.setCellValue(cellValue);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 대용량 엑셀 파일 처리 시, 메모리 효율을 위해 Stream 또는 Callback 기반 처리 방식 권장.
	 *
	 * <p><b>문제점 (Bad Case):</b>
	 * <pre>
	 *   {@code
	 *   List<MyVO> resultList = new ArrayList<>();
	 *     for (int row = 1; row < 1_000_000; row++) {
	 *       MyVO vo = readRow(row);
	 *       // 모든 데이터를 메모리에 누적 -> OutOfMemory 위험
	 *       resultList.add(vo);
	 *     }
	 *   }
	 * </pre>
	 *
	 * <p><b>개선 방법 (Good Case):</b>
	 * <pre>
	 *   {@code
	 *   public void processEachRow(File file, Consumer<MyVO> consumer) {
	 *     for (int row = 1; row < 1_000_000; row++) {
	 *       MyVO vo = readRow(row);
	 *       // 한 건씩 즉시 처리, 누적하지 않음
	 *       consumer.accept(vo);
	 *     }
	 *   }
	 *
	 * // 사용 예:
	 * processEachRow(file, vo -> saveToDB(vo)); // 실시간 저장
	 * }
	 * </pre>
	 *
	 * <ul>
	 *   <li>Row를 리스트에 누적하지 않음</li>
	 *   <li>한 건씩 처리하고 참조 해제 → GC 대상</li>
	 *   <li>메모리에는 '현재 처리 중인 Row 한두 건'만 유지됨</li>
	 * </ul>
	 *
	 * <p><b>참고 자료:</b>
	 * <ul>
	 *   <li><a href="https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api">Apache POI - XSSF SAX (Event API)</a></li>
	 *   <li><a href="https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/FromHowTo.java">Apache POI - Streaming Sample</a></li>
	 * </ul>
	 */
	public <T> ExcelSheet<T> read(String filePath, Class<T> clazz) {
		try (FileInputStream fis = new FileInputStream(filePath);
			 XSSFWorkbook workbook = new XSSFWorkbook(fis)
		) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			ExcelSheet<T> excelSheet = new ExcelSheet<>(ctx, clazz, lastRowNum);
			if (lastRowNum == 0) {
				return excelSheet;
			}

			ExcelModelMetadata metadata = excelSheet.getMetadata();
			int headerRowCount = metadata.getHeaderRowTotalCount();
			for (int rowIdx = headerRowCount; rowIdx <= lastRowNum; rowIdx++) {
				XSSFRow row = sheet.getRow(rowIdx);
				if (isBlankRow(metadata, row)) {
					continue;
				}

				ExcelRow<T> excelRow = excelSheet.createRow(rowIdx, clazz);
				processRow(excelSheet, excelRow, metadata, row);
			}

			EventListener eventListener = ctx.getBean(ExcelBatchPostListener.class);
			eventListener.onEvent(excelSheet);
			return excelSheet;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read excel file.", e);
		}
	}

	public <T> ExcelSheet<T> readSAX(
		 InputStream inputStream,
		 Class<T> excelModelClass,
		 String... excludeFieldName
	) {
		ExcelSheet<T> excelSheet = new ExcelSheet<>(ctx, excelModelClass, excludeFieldName);
		try (OPCPackage pkg = OPCPackage.open(inputStream)) {
			XSSFReader xssfReader = new XSSFReader(pkg);

			EventListener evenListener = new CompositeEventListener(
				 ctx.getBean(ExcelBatchPostListener.class),
				 ctx.getBean(SaxRowEventListener.class)
			);
			XMLReader parser = XMLHelper.newXMLReader();
			parser.setContentHandler(new SaxXlsxSheetHandler<>(
				 xssfReader.getSharedStringsTable(),
				 excelModelClass,
				 excelSheet,
				 evenListener
			));

			Iterator<InputStream> sheets = xssfReader.getSheetsData();
			while (sheets.hasNext()) {
				InputStream sheet = sheets.next();
				parser.parse(new InputSource(sheet));

				evenListener.onEvent(excelSheet);
			}
			return excelSheet;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void processRow(
		 ExcelSheet<T> excelSheet,
		 ExcelRow<T> excelRow,
		 ExcelModelMetadata metadata,
		 XSSFRow row
	) {
		int rowIdx = excelRow.getRowIdx();

		for (Map.Entry<Integer, ExcelField> entry : metadata.entrySet()) {
			Integer cellColIdx = entry.getKey();
			ExcelField excelField = metadata.getExcelField(cellColIdx);
			String cellValue = getStringCellValue(row, cellColIdx);

			boolean validCellValue = excelField.isValidCellValue(cellValue);
			if (validCellValue) {
				excelRow.setFieldValue(excelField, cellValue);

				for (ExcelBatchValidator validator : excelField.getBatchValidators()) {
					validator.collect(rowIdx, new ExcelCell(cellColIdx, cellValue));
					validator.flushBufferIfNeeded(excelSheet::addInvalidRows);
				}
			} else {
				excelSheet.addInvalidRow(rowIdx, new ExcelCell(cellColIdx, cellValue));
			}
		}
	}

	private boolean isBlankRow(ExcelModelMetadata metadata, XSSFRow row) {
		return metadata.entrySet()
			 .stream()
			 .map(entry -> getStringCellValue(row, entry.getKey()))
			 .allMatch(StringUtils::isBlank);
	}

	private String getStringCellValue(XSSFRow row, Integer cellColIdx) {
		if (row == null) {
			return null;
		}
		XSSFCell cell = row.getCell(cellColIdx);
		if (cell == null) {
			return null;
		}

		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		return switch (cellType) {
			case NUMERIC -> {
				if (DateUtil.isCellDateFormatted(cell)) {
					DataFormatter formatter = new DataFormatter();
					// Date date = cell.getDateCellValue();
					// LocalDateTime localDateTimeCellValue = cell.getLocalDateTimeCellValue();
					// yield localDateTimeCellValue.toString();
					yield formatter.formatCellValue(cell);
				}

				double cellValue = cell.getNumericCellValue();
				boolean doubleType = cellValue % 1.0 > 0;
				yield doubleType ? Double.toString(cellValue)
					 : Integer.toString((int)cellValue);
			}
			case STRING -> cell.getStringCellValue();
			case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
			case BLANK -> "";
			default -> null;
		};
	}
}
