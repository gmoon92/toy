package com.gmoon.springpoi.common.excel.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
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
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.annotation.ExcelProperty;
import com.gmoon.springpoi.common.excel.exception.ExcelInvalidFileException;
import com.gmoon.springpoi.common.excel.exception.SaxParseException;
import com.gmoon.springpoi.common.excel.exception.SaxReadRangeOverflowException;
import com.gmoon.springpoi.common.excel.provider.ExcelValueProvider;
import com.gmoon.springpoi.common.excel.sax.RowCallback;
import com.gmoon.springpoi.common.excel.sax.handler.SaxDataRowCountHandler;
import com.gmoon.springpoi.common.excel.sax.handler.SaxXlsxHandler;
import com.gmoon.springpoi.common.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelCell;
import com.gmoon.springpoi.common.excel.vo.ExcelField;
import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;
import com.gmoon.springpoi.common.utils.ReflectionUtil;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExcelComponent
@RequiredArgsConstructor
public class ExcelHelper {
	private final ApplicationContext ctx;

	public <T extends BaseExcelModel> void write(
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
		} catch (IOException e) {
			throw new ExcelInvalidFileException("Excel download error because : ", e);
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

		for (ExcelField excelField : metadata.getExcelFields()) {
			int cellColIdx = excelField.getCellColIndex();
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
		for (ExcelField excelField : metadata.getExcelFields()) {
			int cellColIdx = excelField.getCellColIndex();

			Field field = excelField.getField();
			Object fieldValue = ReflectionUtil.getFieldValue(data, field);
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
	public <T extends BaseExcelModel> ExcelSheet<T> read(
		 Path filePath,
		 Class<T> clazz,
		 String... excludeFieldName
	) {
		try (
			 InputStream fis = Files.newInputStream(filePath);
			 XSSFWorkbook workbook = new XSSFWorkbook(fis)
		) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			int lastRowNum = sheet.getLastRowNum();
			ExcelSheet<T> excelSheet = new ExcelSheet<>(ctx, clazz, lastRowNum, excludeFieldName);
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

				processRow(excelSheet, rowIdx, metadata, row);
			}

			List<ExcelBatchValidator> batchValidators = metadata.getAllBatchValidators();
			for (ExcelBatchValidator validator : batchValidators) {
				validator.flush(excelSheet::addInvalidRows);
			}
			return excelSheet;
		} catch (IOException e) {
			throw new ExcelInvalidFileException(filePath);
		}
	}

	public <T extends BaseExcelModel> ExcelSheet<T> readSAX(
		 Path filePath,
		 Class<T> excelModelClass,
		 RowCallback<T> rowCallback,
		 long startRowIdx,
		 long endRowIdx,
		 String... excludeFieldName
	) {
		ExcelSheet<T> excelSheet = new ExcelSheet<>(ctx, excelModelClass, excludeFieldName);
		try (
			 InputStream fis = Files.newInputStream(filePath);
			 OPCPackage pkg = OPCPackage.open(fis)
		) {
			XSSFReader xssfReader = new XSSFReader(pkg);

			XMLReader parser = XMLHelper.newXMLReader();
			parser.setContentHandler(new SaxXlsxHandler<>(
				 xssfReader.getSharedStringsTable(),
				 excelSheet,
				 rowCallback,
				 startRowIdx,
				 endRowIdx
			));

			Iterator<InputStream> sheets = xssfReader.getSheetsData();
			while (sheets.hasNext()) {
				InputStream sheet = sheets.next();
				parser.parse(new InputSource(sheet));
			}
			return excelSheet;
		} catch (SaxReadRangeOverflowException e) {
			log.debug("Excel sheet read range done.");
			return excelSheet;
		} catch (IOException | ParserConfigurationException | OpenXML4JException e) {
			throw new ExcelInvalidFileException(filePath);
		} catch (SAXException e) {
			throw new SaxParseException(filePath, e);
		}
	}

	public long getDataRows(
		 Path filePath,
		 ExcelSheetType sheetType,
		 int maxDataRows,
		 String... excludeFieldName
	) {
		try (
			 InputStream fis = Files.newInputStream(filePath);
			 OPCPackage pkg = OPCPackage.open(fis)
		) {
			ExcelSheet<?> excelSheet = new ExcelSheet<>(
				 ctx,
				 sheetType.getExcelModelClass(),
				 excludeFieldName
			);

			XSSFReader xssfReader = new XSSFReader(pkg);
			XMLReader parser = XMLHelper.newXMLReader();
			SaxDataRowCountHandler handler = new SaxDataRowCountHandler(
				 xssfReader.getSharedStringsTable(),
				 excelSheet.getMetadata(),
				 maxDataRows
			);
			parser.setContentHandler(handler);

			Iterator<InputStream> sheets = xssfReader.getSheetsData();
			while (sheets.hasNext()) {
				InputStream sheet = sheets.next();
				parser.parse(new InputSource(sheet));
			}
			return handler.getDataRows();
		} catch (IOException | ParserConfigurationException | OpenXML4JException e) {
			throw new ExcelInvalidFileException(filePath);
		} catch (SAXException e) {
			throw new SaxParseException(filePath, e);
		}
	}

	private <T extends BaseExcelModel> void processRow(
		 ExcelSheet<T> excelSheet,
		 int rowIdx,
		 ExcelModelMetadata metadata,
		 XSSFRow row
	) {
		ExcelRow<T> excelRow = excelSheet.createRow(rowIdx);
		for (ExcelField excelField : metadata.getExcelFields()) {
			int cellColIdx = excelField.getCellColIndex();
			String cellValue = getStringCellValue(row, cellColIdx);
			excelSheet.addOriginValue(rowIdx, cellColIdx, cellValue);

			List<ExcelValidator> failedValidators = excelField.getFailedValidators(cellValue);
			boolean validCellValue = failedValidators.isEmpty();
			if (validCellValue) {
				excelRow.setFieldValue(excelField, cellValue);

				for (ExcelBatchValidator validator : excelField.getBatchValidators()) {
					validator.collect(rowIdx, cellColIdx, cellValue);
					validator.flushBufferIfNeeded(excelSheet::addInvalidRows);
				}
			} else {
				excelSheet.addInvalidRow(rowIdx, new ExcelCell(cellColIdx, cellValue, failedValidators));
			}
		}
	}

	private boolean isBlankRow(ExcelModelMetadata metadata, XSSFRow row) {
		return metadata.getExcelFields()
			 .stream()
			 .map(excelField -> getStringCellValue(row, excelField.getCellColIndex()))
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
