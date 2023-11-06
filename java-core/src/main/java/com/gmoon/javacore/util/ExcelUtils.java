package com.gmoon.javacore.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelUtils {

	private static final String FILE_EXTENSION_NAME = ".xlsx";
	private static final int DEFAULT_ROW_SIZE = 1_000;

	public static <T> Path upload(String filePath, List<T> datas, Class<T> clazz) {
		Workbook wb = new SXSSFWorkbook(DEFAULT_ROW_SIZE);
		Sheet sheet = wb.createSheet();

		List<Field> columnFields = getFields(clazz);
		for (int rowNum = 0; rowNum < datas.size(); rowNum++) {
			Row row = sheet.createRow(rowNum);

			T data = datas.get(rowNum);
			writeRow(columnFields, data, row);
		}

		Path path = getLocalStoragePath(filePath);
		uploadExcelFileToLocalStorage(wb, path);
		return path;
	}

	private static Path getLocalStoragePath(String filePath) {
		if (!filePath.endsWith(FILE_EXTENSION_NAME)) {
			filePath += FILE_EXTENSION_NAME;
		}

		return Paths.get(filePath);
	}

	private static void writeRow(List<Field> excelColumnFields, Object data, Row row) {
		int cellNum = 0;
		for (Field field : excelColumnFields) {
			try {
				Object obj = field.get(data);
				String cellValue = String.valueOf(obj);

				Cell cell = row.createCell(cellNum++);
				cell.setCellValue(cellValue);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static List<Field> getFields(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		return Arrays.asList(fields);
	}

	private static void uploadExcelFileToLocalStorage(Workbook wb, Path path) {
		try (OutputStream out = Files.newOutputStream(path)) {
			wb.write(out);
			out.flush();
		} catch (Exception ex) {
			throw new RuntimeException("Excel download error because: ", ex);
		} finally {
			if (wb instanceof SXSSFWorkbook) {
				((SXSSFWorkbook)wb).dispose();
			}

			try {
				wb.close();
			} catch (IOException ex) {
				throw new RuntimeException("Not workbook closed...", ex);
			}
		}
	}
}
