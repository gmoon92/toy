package com.gmoon.springpoi.excel.sample;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * <ul>
 *   <li><a href="https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api">Apache POI - XSSF SAX (Event API)</a></li>
 *   <li><a href="https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/FromHowTo.java">Apache POI - Streaming Sample</a></li>
 *   <li><a href="http://www.saxproject.org/about.html">About SAX</a></li>
 * </ul>
 */
@Slf4j
class XSSFSaxTest {

	private Path excelFilePath;

	@BeforeEach
	void setUp() {
		// int dataSize = 1;
		int dataSize = 100;
		// int dataSize = 1_000;
		// int dataSize = 10_000;
		excelFilePath = Paths.get("src/test/resources/sample/sample-" + dataSize + ".xlsx");
	}

	@Test
	void test() throws Exception {
		try (OPCPackage pkg = OPCPackage.open(Files.newInputStream(excelFilePath))) {
			XSSFReader xssfReader = new XSSFReader(pkg);
			SharedStrings sst = xssfReader.getSharedStringsTable();

			XMLReader parser = XMLHelper.newXMLReader();
			parser.setContentHandler(new SAXSheetHandler(sst));

			// process the first sheet
			try (InputStream sheet = xssfReader.getSheetsData().next()) {
				InputSource sheetSource = new InputSource(sheet);
				parser.parse(sheetSource);
			}
		}
	}

	@DisplayName("rId 가 동작안될 수도 있으니 권장하지 않음.")
	@Test
	void processOneSheet() throws Exception {
		// try (OPCPackage pkg = OPCPackage.open(filePath, PackageAccess.READ)) {
		try (OPCPackage pkg = OPCPackage.open(Files.newInputStream(excelFilePath))) {
			XSSFReader reader = new XSSFReader(pkg);
			SharedStrings sst = reader.getSharedStringsTable();

			XMLReader parser = XMLHelper.newXMLReader();
			parser.setContentHandler(new SAXSheetHandler(sst));

			XSSFReader.SheetIterator sheetIterator = (XSSFReader.SheetIterator)reader.getSheetsData();
			while (sheetIterator.hasNext()) {
				InputStream sheetStream = sheetIterator.next();

				String sheetName = sheetIterator.getSheetName();
				log.info("==========Start sheet parse {}==========", sheetName);
				// 'Relationship' 객체로부터 실제 rId 얻기
				PackagePart sheetPart = sheetIterator.getSheetPart();
				PackageRelationshipCollection relationships = sheetPart.getRelationships();
				for (PackageRelationship relationship : relationships) {
					// To look up the Sheet Name / Sheet Order / rID,
					//  you need to process the core Workbook stream.
					// Normally it's of the form rId# or rSheet#
					String rId = relationship.getId();
					log.info("parse sheet {} rId: {}", sheetName, rId);
					InputStream sheet = reader.getSheet(rId);
					InputSource sheetSource = new InputSource(sheet);

					parser.parse(sheetSource);
					sheet.close();
				}

				sheetStream.close();
			}
		}
	}

	@Test
	void processAllSheets() throws Exception {
		// try (OPCPackage pkg = OPCPackage.open(filePath, PackageAccess.READ)) {
		try (OPCPackage pkg = OPCPackage.open(Files.newInputStream(excelFilePath))) {
			XSSFReader reader = new XSSFReader(pkg);
			SharedStrings sst = reader.getSharedStringsTable();

			XMLReader parser = XMLHelper.newXMLReader();
			parser.setContentHandler(new SAXSheetHandler(sst));

			Iterator<InputStream> sheets = reader.getSheetsData();
			while (sheets.hasNext()) {
				System.out.println("Processing new sheet:\n");

				InputStream sheetStream = sheets.next();
				InputSource sheetSource = new InputSource(sheetStream);
				parser.parse(sheetSource);
				sheetStream.close();
			}
		}
	}

	/**
	 * See {@link DefaultHandler} javadocs
	 */
	@Slf4j
	public static class SAXSheetHandler extends DefaultHandler {
		private final SAXSheetHandler.LruCache<Integer, String> lruCache = new SAXSheetHandler.LruCache<>(50);
		private final SharedStrings sharedStringsTable;

		private String lastContents;

		private Integer rowIdx;
		private boolean startCell;
		private Integer cellColIdx;
		private String cellType;
		private String cellValue;

		private static class LruCache<A, B> extends LinkedHashMap<A, B> {
			private final int maxEntries;

			public LruCache(final int maxEntries) {
				super(maxEntries + 1, 1.0f, true);
				this.maxEntries = maxEntries;
			}

			@Override
			protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
				return super.size() > maxEntries;
			}
		}

		public SAXSheetHandler(SharedStrings sst) {
			this.sharedStringsTable = sst;
		}

		@Override
		public void startElement(
			 String uri,
			 String localName,
			 String name,
			 Attributes attributes
		) {
			if ("row".equals(name)) {
				String rowReference = attributes.getValue("r");
				rowIdx = Integer.parseInt(rowReference);
				log.debug("======================[ROW START {}]======================", rowIdx);
			}

			if ("c".equals(name)) {
				startCell = true;
				cellColIdx = getCellColIdx(attributes);

				// Figure out if the value is an index in the SST
				cellType = attributes.getValue("t");
			}
		}

		private int getCellColIdx(Attributes attributes) {
			String cellReference = attributes.getValue("r");
			String cellColIdx = cellReference.replaceAll("\\d", "");

			int index = 0;
			for (int i = 0; i < cellColIdx.length(); i++) {
				char c = cellColIdx.charAt(i);
				index = index * 26 + (c - 'A' + 1);
			}
			return index - 1; // 0-based index
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			// 태그 사이에 실제 값(문자, 숫자든 인덱스든) 저장 (row, c, v 태그 내 값)
			lastContents = new String(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String name) {
			setCellValue(name);

			if ("c".equals(name)) {
				log.info("[{}] type: {} value: {}", cellColIdx, cellType, cellValue);
				clearCellMetadata();
			}

			// 한 행의 끝.
			// 필요 시 여기서 row-level 로직 처리 가능
			// 필요한 후처리(예: 한 줄 완성 후 리스트에 추가 등)
			if ("row".equals(name)) {
				log.debug("======================[ROW END   {}]======================", rowIdx);
			}
		}

		private void setCellValue(String name) {
			if (!startCell) {
				cellValue = null;
				return;
			}

			if ("t".equals(name)
				 && "inlineStr".equals(cellType)) {
				cellValue = lastContents;
			}

			if ("v".equals(name)) {
				if ("s".equals(cellType)) {
					// 문자열(shared string):
					// index를 SharedStrings 인덱스를 참조하여 실제 문자열 가져오기
					int idx = Integer.parseInt(cellValue);
					String shardString = lruCache.get(idx);
					if (shardString != null && !lruCache.containsKey(idx)) {
						RichTextString textString = sharedStringsTable.getItemAt(idx);
						shardString = textString.getString();
						lruCache.put(idx, shardString);
					}

					lastContents = shardString;
				} else {
					cellValue = lastContents;
				}
			}
		}

		// Clear contents cache
		// 필요시 cellType/currentValue 초기화
		private void clearCellMetadata() {
			startCell = false;
			cellType = null;
			cellValue = null;
		}
	}
}
