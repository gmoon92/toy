package com.gmoon.commons.commonsapachepoi.excel.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelModel;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.provider.ExcelValueProvider;
import com.gmoon.commons.commonsapachepoi.excel.validator.ExcelBatchValidator;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelField;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelFields;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelRow;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelSheet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelUtil {

	public static <T> void write(
		 HttpServletRequest request,
		 OutputStream out,
		 Class<T> clazz,
		 List<T> dataList
	) {
		ServletContext servletContext = request.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		ExcelFields excelFields = ExcelFields.of(clazz, ctx);
		ExcelModel excelModelAnnotation = excelFields.getExcelModelAnnotation();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(1_000)) {
			String sheetName = excelModelAnnotation.sheetName();
			Sheet sheet = wb.createSheet(sheetName);

			writeTitle(wb, sheet, excelFields);
			writeData(excelModelAnnotation, sheet, excelFields, dataList);

			wb.write(out);
			out.flush();
		} catch (IOException | NullPointerException ex) {
			throw new RuntimeException("Excel download error because : ", ex);
		}
	}

	private static <T> void writeData(
		 ExcelModel excelModel,
		 Sheet sh,
		 ExcelFields excelFields,
		 List<T> dataList
	) {
		int rowNum = excelModel.totalTitleRowCount();
		for (T data : dataList) {
			Row row = sh.createRow(rowNum++);
			writeRow(row, excelFields, data);
		}
	}

	private static void writeTitle(Workbook wb, Sheet sh, ExcelFields excelFields) {
		Row row = sh.createRow(0);
		Drawing<?> drawing = sh.createDrawingPatriarch();
		CreationHelper factory = wb.getCreationHelper();

		for (Map.Entry<Integer, ExcelField> entry : excelFields.entrySet()) {
			Integer cellNum = entry.getKey();
			ExcelField value = entry.getValue();
			Field field = value.getField();

			ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
			Comment comment = createCellComment(row, cellNum, drawing, factory, annotation);

			String colTitle = Objects.toString(annotation.title(), field.getName());
			Cell cell = row.createCell(cellNum);
			cell.setCellValue(colTitle);
			cell.setCellComment(comment);
		}
	}

	private static Comment createCellComment(
		 Row row,
		 int cellNum,
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
		anchor.setCol1(cellNum);
		anchor.setCol2(cellNum + defaultAnchorColSize);
		anchor.setRow1(row.getRowNum());
		anchor.setRow2(row.getRowNum() + defaultAnchorRowSize);

		Comment cellComment = drawing.createCellComment(anchor);
		cellComment.setString(factory.createRichTextString(annotation.comment()));
		return cellComment;
	}

	private static <T> void writeRow(Row row, ExcelFields excelFields, T data) {
		for (Map.Entry<Integer, ExcelField> entry : excelFields.entrySet()) {
			int cellNum = entry.getKey();
			ExcelField excelField = entry.getValue();
			Field field = excelField.getField();
			try {
				Object obj = field.get(data);
				if (obj == null) {
					continue;
				}

				String cellValue = switch (obj) {
					case Integer i -> Integer.toString(i);
					case Boolean b -> BooleanUtils.toString(b, "Y", "N");
					case ExcelValueProvider provider -> provider.getExcelCellValue();
					default -> String.valueOf(obj);
				};
				Cell cell = row.createCell(cellNum);
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
	 *   public static void processEachRow(File file, Consumer<MyVO> consumer) {
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
	public static <T> ExcelSheet<T> read(
		 HttpServletRequest request,
		 String filePath,
		 Class<T> clazz
	) {
		ServletContext servletContext = request.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		ExcelFields excelFields = ExcelFields.of(clazz, ctx);
		ExcelModel excelModelAnnotation = excelFields.getExcelModelAnnotation();
		try (FileInputStream fis = new FileInputStream(filePath);
			 XSSFWorkbook workbook = new XSSFWorkbook(fis)
		) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			if (lastRowNum == 0) {
				return ExcelSheet.empty();
			}

			ExcelSheet<T> excelSheet = ExcelSheet.create(lastRowNum);
			int dataStartRowIndex = excelModelAnnotation.totalTitleRowCount();
			for (int rowNum = dataStartRowIndex; rowNum <= lastRowNum; rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				if (isBlankRow(excelFields, row)) {
					continue;
				}

				ExcelRow<T> excelRow = new ExcelRow<>(rowNum, clazz);
				processRow(excelSheet, excelRow, excelFields, row);
			}

			postProcess(excelSheet, excelFields);
			return excelSheet;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read excel file.", e);
		}
	}

	private static <T> void postProcess(ExcelSheet<T> excelSheet, ExcelFields excelFields) {
		List<ExcelBatchValidator> batchValidators = excelFields.getAllBatchValidators();
		for (ExcelBatchValidator validator : batchValidators) {
			validator.flush(excelSheet::addInvalidRows);
		}
	}

	private static <T> void processRow(
		 ExcelSheet<T> excelSheet,
		 ExcelRow<T> excelRow,
		 ExcelFields excelFields,
		 XSSFRow row
	) {
		int rowNum = excelRow.getRowNum();
		boolean validRow = true;
		for (Map.Entry<Integer, ExcelField> entry : excelFields.entrySet()) {
			ExcelField excelField = entry.getValue();
			Integer cellNum = entry.getKey();
			String cellValue = getStringCellValue(row, cellNum);

			if (shouldSkipValidation(excelField, cellValue)) {
				continue;
			}

			excelRow.setFieldValue(excelField, cellValue);
			if (isValidCell(excelField, cellValue)) {
				List<ExcelBatchValidator> batchValidators = excelField.getBatchValidators();
				boolean flushed = flushInvalidRowIfBufferFull(
					 batchValidators,
					 excelSheet,
					 rowNum,
					 cellValue
				);
				if (flushed) {
					validRow = false;
				}
			} else {
				excelSheet.addInvalidRow(rowNum, excelRow);
				validRow = false;
			}
		}

		if (validRow) {
			excelSheet.add(rowNum, excelRow);
		}
	}

	private static boolean shouldSkipValidation(ExcelField excelField, String cellValue) {
		boolean required = excelField.isRequired();
		return !required && cellValue == null;
	}

	private static <T> boolean flushInvalidRowIfBufferFull(
		 List<ExcelBatchValidator> batchValidators,
		 ExcelSheet<T> excelSheet,
		 int rowNum,
		 String cellValue
	) {
		boolean result = false;
		for (ExcelBatchValidator validator : batchValidators) {
			validator.collect(rowNum, cellValue);
			boolean flushed = validator.flushBufferIfNeeded(excelSheet::addInvalidRows);
			if (flushed) {
				result = true;
			}
		}
		return result;
	}

	private static boolean isValidCell(ExcelField excelField, String cellValue) {
		return excelField.getValidators()
			 .stream()
			 .filter(validator -> !validator.isValid(cellValue))
			 .peek(validator -> log.debug("[excel validation failed] validator: {}, {}: {}",
				  validator,
				  excelField.getFieldName(),
				  cellValue
			 ))
			 .findFirst()
			 .isEmpty();
	}

	private static boolean isBlankRow(ExcelFields excelFields, XSSFRow row) {
		return excelFields.entrySet()
			 .stream()
			 .map(entry -> getStringCellValue(row, entry.getKey()))
			 .allMatch(StringUtils::isBlank);
	}

	private static String getStringCellValue(XSSFRow row, Integer cellNum) {
		if (row == null) {
			return null;
		}
		XSSFCell cell = row.getCell(cellNum);
		return getStringCellValue(cell);
	}

	public static String getStringCellValue(Cell cell) {
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
					yield formatter.formatCellValue(cell);
					// yield localDateTimeCellValue.toString();
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
