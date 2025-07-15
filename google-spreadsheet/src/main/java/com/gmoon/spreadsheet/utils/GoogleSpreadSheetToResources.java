package com.gmoon.spreadsheet.utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.spreadsheet.config.GoogleSpreadSheetProperties;
import com.gmoon.spreadsheet.vo.ResourceVO;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class GoogleSpreadSheetToResources {

	private static GoogleSpreadSheetProperties properties;

	private static final String DEFAULT_LANGUAGE = "ko";

	protected GoogleSpreadSheetToResources(GoogleSpreadSheetProperties googleSpreadSheetProperties) {
		this.properties = googleSpreadSheetProperties;
	}

	public static void generate(String targetDirPath) {
		try {
			String sheetId = properties.getId();
			String cellRange = properties.getRange();

			for (String sheetName : getReadSheetNames()) {
				log.debug("sheetId : {}, sheetName : {}, cellRange : {}", sheetId, sheetName, cellRange);
				ValueRange sheet = GoogleApiService.getValueRange(sheetId, sheetName, cellRange);
				List<ResourceVO> list = getResourceVOList(sheet);
				for (ResourceVO vo : list) {
					generatePropertiesResource(sheetName, vo, targetDirPath);
				}

				ResourceVO defaultResourcesVO = getDefaultResourceVO(list);
				generatePropertiesResource(sheetName, defaultResourcesVO, targetDirPath, "");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static ResourceVO getDefaultResourceVO(List<ResourceVO> resourceVOList) {
		return resourceVOList.stream()
			 .filter(vo -> DEFAULT_LANGUAGE.equals(vo.getLanguage()))
			 .findFirst()
			 .orElse(null);
	}

	private static void generatePropertiesResource(String sheetName, ResourceVO vo, String targetDirPath) throws
		 Exception {
		if (StringUtils.isBlank(vo.getLanguage()))
			return;

		generatePropertiesResource(sheetName, vo, targetDirPath, vo.getLanguage());
	}

	private static void generatePropertiesResource(String sheetName, ResourceVO vo, String targetDirPath,
		 String targetLanguage) throws Exception {
		String resourceFilePath = getResourceFilePath(sheetName, targetLanguage, targetDirPath);

		PropertiesConfiguration config = new PropertiesConfiguration();
		config.setListDelimiterHandler(null);
		// config.setDelimiterParsingDisabled(true);
		// config.setEncoding("UTF-8");
		for (Map.Entry<String, String> entry : vo.getDataMap().entrySet()) {
			if (entry != null && entry.getKey() != null) {
				config.addProperty(entry.getKey(), entry.getValue());
			}
		}
		// config.save(resourceFilePath);

		FileHandler handler = new FileHandler(config);
		handler.setEncoding("UTF-8");
		handler.save(new File(resourceFilePath));
	}

	private static List<ResourceVO> getResourceVOList(ValueRange sheet) {
		List<ResourceVO> resourceVOList = createResourceVOList();
		List<List<Object>> values = sheet.getValues();

		if (CollectionUtils.isEmpty(values)) {
			log.error("No data found.");
		} else {
			for (List<Object> row : values) {
				for (int i = 0; i < row.size(); i++) {
					String key = getCellValue(row, "key");
					for (ResourceVO vo : resourceVOList) {
						SortedMap<String, String> data = vo.getDataMap();
						String value = getCellValue(row, vo.getLanguage());
						data.put(key, StringUtils.defaultIfBlank(value, key));
					}
				}
			}
		}
		return resourceVOList;
	}

	private static List<ResourceVO> createResourceVOList() {
		return getReadSheetHeads()
			 .stream()
			 .filter(ResourceVO::verify)
			 .map(ResourceVO::newInstance)
			 .toList();
	}

	private static String getCellValue(List<Object> row, String sheetHeadString) {
		String cellValue = null;
		try {
			cellValue = (String)row.get(indexOfHead(sheetHeadString));
		} catch (IndexOutOfBoundsException e) {
			String rowStr = row.stream()
				 .map(String::valueOf)
				 .collect(Collectors.joining("|"));
			log.error("google sheet value is null : {}, row : {}", sheetHeadString, rowStr);
		} finally {
			return StringUtils.defaultString(cellValue);
		}
	}

	private static List<String> getReadSheetNames() {
		return properties.getSheetNames();
	}

	private static List<String> getReadSheetHeads() {
		return properties.getSheetHeads();
	}

	private static int indexOfHead(String headStr) {
		return getReadSheetHeads()
			 .indexOf(headStr);
	}

	private static String getResourceFilePath(String sheetName, String language, String targetDirPath) {
		if (StringUtils.isBlank(language))
			return String.format("%s/%s.properties", targetDirPath, sheetName);
		return String.format("%s/%s_%s.properties", targetDirPath, sheetName, language);
	}

}
