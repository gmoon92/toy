package com.gmoon.commons.commonsapachepoi.excel.sample;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * <ul>
 *   <li><a href="https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api">Apache POI - XSSF SAX (Event API)</a></li>
 *   <li><a href="https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/FromHowTo.java">Apache POI - Streaming Sample</a></li>
 *   <li><a href="http://www.saxproject.org/about.html">About SAX</a></li>
 * </ul>
 */
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
		// try (OPCPackage pkg = OPCPackage.open(filePath, PackageAccess.READ)) {
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
				System.out.printf("==========Start sheet parse %s==========\n", sheetName);
				// 'Relationship' 객체로부터 실제 rId 얻기
				PackagePart sheetPart = sheetIterator.getSheetPart();
				PackageRelationshipCollection relationships = sheetPart.getRelationships();
				for (PackageRelationship relationship : relationships) {
					// To look up the Sheet Name / Sheet Order / rID,
					//  you need to process the core Workbook stream.
					// Normally it's of the form rId# or rSheet#
					String rId = relationship.getId();
					System.out.printf("parse sheet %s rId: %s", sheetName, rId);
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

}
