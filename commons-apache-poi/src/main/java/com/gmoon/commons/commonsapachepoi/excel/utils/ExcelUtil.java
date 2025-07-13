package com.gmoon.commons.commonsapachepoi.excel.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.annotation.AnnotationUtils;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelModel;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;
import com.gmoon.commons.commonsapachepoi.excel.predicate.ExcelValidator;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelFields;
import com.gmoon.commons.commonsapachepoi.excel.vo.ExcelParseResult;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelUtil {

	public static <T> void download(
		 OutputStream out,
		 Class<T> clazz,
		 List<T> dataList
	) {
		ExcelModel excelModelAnnotation = getExcelModelAnnotation(clazz);
		ExcelFields excelFields = ExcelFields.from(clazz);
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

		for (Map.Entry<Integer, ExcelFields.ExcelField> entry : excelFields.entrySet()) {
			Integer cellNum = entry.getKey();
			ExcelFields.ExcelField value = entry.getValue();
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

		final int DEFAULT_ANCHOR_COL_SIZE = 6;
		final int DEFAULT_ANCHOR_ROW_SIZE = 4;

		ClientAnchor anchor = factory.createClientAnchor();
		anchor.setCol1(cellNum);
		anchor.setCol2(cellNum + DEFAULT_ANCHOR_COL_SIZE);
		anchor.setRow1(row.getRowNum());
		anchor.setRow2(row.getRowNum() + DEFAULT_ANCHOR_ROW_SIZE);

		Comment cellComment = drawing.createCellComment(anchor);
		cellComment.setString(factory.createRichTextString(annotation.comment()));
		return cellComment;
	}

	private static <T> void writeRow(Row row, ExcelFields excelFields, T data) {
		for (Map.Entry<Integer, ExcelFields.ExcelField> entry : excelFields.entrySet()) {
			int cellNum = entry.getKey();
			ExcelFields.ExcelField excelField = entry.getValue();
			Field field = excelField.getField();
			try {
				Object obj = field.get(data);
				String cellValue;
				if (obj == null) {
					cellValue = null;
				} else if (obj instanceof Boolean b) {
					cellValue = BooleanUtils.toString(b, "Y", "N");
				} else {
					cellValue = String.valueOf(obj);
				}

				Cell cell = row.createCell(cellNum);
				cell.setCellValue(cellValue);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static String getStringCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}

		CellType cellType = cell.getCellType();
		return switch (cellType) {
			case NUMERIC -> {
				double cellValue = cell.getNumericCellValue();
				boolean isDoubleType = cellValue % 1.0 > 0;
				yield isDoubleType ? Double.toString(cellValue)
					 : Integer.toString((int)cellValue);
			}
			case STRING -> cell.getStringCellValue();
			case FORMULA -> Integer.toString((int)cell.getNumericCellValue());
			case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
			default -> null;
		};
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
	public static <T> ExcelParseResult<T> read(
		 HttpServletRequest request,
		 String filePath,
		 Class<T> clazz
	) {
		ExcelModel excelModelAnnotation = getExcelModelAnnotation(clazz);
		ExcelFields excelFields = ExcelFields.from(clazz, request);

		try (FileInputStream fis = new FileInputStream(filePath);
			 XSSFWorkbook workbook = new XSSFWorkbook(fis)
		) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			if (lastRowNum == 0) {
				return ExcelParseResult.empty();
			}

			ExcelParseResult<T> parseResult = ExcelParseResult.create(lastRowNum, excelFields.size());

			int dataStartRowIndex = excelModelAnnotation.totalTitleRowCount();
			for (int rowNum = dataStartRowIndex; rowNum <= lastRowNum; rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				if (row == null) {
					continue;
				}

				boolean isValidRow = true;
				T excelModel = ReflectionUtil.newInstance(clazz);
				for (Map.Entry<Integer, ExcelFields.ExcelField> entry : excelFields.entrySet()) {
					Integer cellIndex = entry.getKey();
					XSSFCell cell = row.getCell(cellIndex);
					String cellValue = getStringCellValue(cell);

					ExcelFields.ExcelField excelField = entry.getValue();
					ExcelConverter<?> converter = excelField.getConverter();
					Field field = excelField.getField();
					field.set(excelModel, converter.convert(cellValue));

					boolean required = excelField.isRequired();
					boolean skipValidation = StringUtils.isBlank(cellValue) && !required;
					if (skipValidation) {
						continue;
					}

					Optional<ExcelValidator> failedValidator = excelField.getValidators()
						 .stream()
						 .filter(validator -> !validator.isValid(cellValue))
						 .findFirst();
					if (failedValidator.isPresent()) {
						String fieldName = field.getName();
						isValidRow = false;
						parseResult.addInvalidData(rowNum, excelModel, fieldName, cellValue);
						log.debug("[invalid] {}: {}, violated validator: {}", fieldName, cellValue,
							 failedValidator.get());
					}
				}

				if (isValidRow) {
					parseResult.add(rowNum, excelModel);
				}
			}
			return parseResult;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static ExcelModel getExcelModelAnnotation(Class<?> clazz) {
		ExcelModel excelModel = AnnotationUtils.findAnnotation(clazz, ExcelModel.class);
		if (excelModel == null) {
			throw new UnsupportedOperationException(
				 String.format("@ExcelAutoDetect annotation not found in class %s", clazz.getName())
			);
		}

		return excelModel;
	}
}
