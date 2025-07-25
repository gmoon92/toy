
package com.gmoon.springpoi.excel.sax.handler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.gmoon.springpoi.excel.sax.SaxCell;
import com.gmoon.springpoi.excel.sax.XlsxOoxml;
import com.gmoon.springpoi.excel.vo.ExcelModelMetadata;

import io.micrometer.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * SAX 이벤트 기반의 엑셀(xlsx) OOXML 파서
 *
 * <pre>
 * 예시 XML 구조:
 *   &lt;row r="1"&gt;
 *
 *     &lt;c r="A1" t="inlineStr"&gt;
 *        &lt;is&gt;
 *          &lt;t&gt;사용자 아이디&lt;/t&gt;
 *        &lt;/is&gt;
 *     &lt;/c&gt;
 *
 *     &lt;c r="B1" t="s"&gt;
 *       &lt;v&gt;0&lt;/v&gt;
 *     &lt;/c&gt;
 *
 *     &lt;c r="C1"&gt;
 *       &lt;v&gt;123&lt;/v&gt; // 숫자 또는 날짜
 *     &lt;/c&gt;
 *   &lt;/row&gt;
 * </pre>
 * {@link DefaultHandler} 기반으로 row/cell/value 등의 태그 이벤트를 처리하며,
 * ExcelSheet<T> 및 ExcelFields 와 연동해 DTO 조립에 활용할 수 있다.
 *
 * <p>
 * 이벤트는 다음과 같은 순으로 동작된다.
 * <ul>
 *     <li>{@link AbstractSaxXlsxSheetHandler#startElement(String, String, String, Attributes)}</li>
 *     <li>{@link AbstractSaxXlsxSheetHandler#characters(char[], int, int)}</li>
 *     <li>{@link AbstractSaxXlsxSheetHandler#endElement(String, String, String)}</li>
 * </ul>
 * </p>
 *
 * @author gmoon
 */
@Slf4j
public abstract class AbstractSaxXlsxSheetHandler extends DefaultHandler {
	private final LruCache<Integer, String> sharedStringCache = new LruCache<>(50);
	private final SharedStrings sharedStringsTable;
	private final ExcelModelMetadata metadata;
	private final Map<Integer, String> cellValues;

	private final StringBuilder textBuffer = new StringBuilder();
	private String currentContents;

	private boolean blankRow;
	private int currentRowIdx;

	private boolean cellOpen;
	private SaxCell currentCell;

	protected AbstractSaxXlsxSheetHandler(SharedStrings sst, ExcelModelMetadata metadata) {
		this.sharedStringsTable = sst;
		this.metadata = metadata;
		this.cellValues = new HashMap<>();
	}

	public abstract void handle(int rowIdx, Map<Integer, String> cellValues);

	@Override
	public void startElement(
		 String uri,
		 String localName,
		 String qName,
		 Attributes attributes
	) {
		XlsxOoxml.Element element = XlsxOoxml.Element.from(qName);
		if (XlsxOoxml.Element.ROW == element) {
			handleRowStart(attributes, element);
		}

		if (XlsxOoxml.Element.CELL == element) {
			cellOpen = true;
			currentCell = new SaxCell(
				 getAttributeValue(attributes, XlsxOoxml.Attribute.TYPE),
				 getCellColIdx(element, attributes)
			);
		}
	}

	private void handleRowStart(Attributes attributes, XlsxOoxml.Element element) {
		blankRow = true;
		currentRowIdx = getRowIndex(element, attributes);
		log.debug("======================[ROW START {}]======================", currentRowIdx);
	}

	private int getRowIndex(XlsxOoxml.Element element, Attributes attributes) {
		if (XlsxOoxml.Element.ROW == element) {
			String rowReference = getAttributeValue(attributes, XlsxOoxml.Attribute.REFERENCE);
			return Integer.parseInt(rowReference) - 1; // 0-based index
		}

		throw new RuntimeException(String.format("%s is not row element.", element));
	}

	/**
	 * 셀 참조 문자열(ex: "A1")에서 열 인덱스를 계산한다.
	 *
	 * <p>
	 * "A" → 0, "B" → 1, ..., "Z" → 25, "AA" → 26 등
	 * 알파벳 열 번호를 0 기반 인덱스로 변환한다.
	 * </p>
	 */
	private int getCellColIdx(XlsxOoxml.Element element, Attributes attributes) {
		if (XlsxOoxml.Element.CELL == element) {
			String cellReference = getAttributeValue(attributes, XlsxOoxml.Attribute.REFERENCE);
			String cellColIndex = cellReference.replaceAll("\\d", "");

			int index = 0;
			for (int i = 0; i < cellColIndex.length(); i++) {
				char c = cellColIndex.charAt(i);
				index = index * 26 + (c - 'A' + 1);
			}
			return index - 1; // 0-based index
		}

		throw new RuntimeException(String.format("%s is not cell element.", element));
	}

	/**
	 * 태그(<t>, <v> 등) 사이의 값을 저장한다.
	 *
	 * <p>
	 * 예: {@code <t>사용자 아이디</t>} 의 경우 이 메서드가 호출되어
	 * 해당 값("사용자 아이디")이 저장된다.
	 * </p>
	 * <pre>
	 *   &lt;row r="1"&gt;
	 *     &lt;c r="A1" t="inlineStr"&gt;
	 *        &lt;is&gt;
	 *          &lt;t&gt;사용자 아이디&lt;/t&gt;
	 *        &lt;/is&gt;
	 *     &lt;/c&gt;
	 *   &lt;/row&gt;
	 * </pre>
	 * <p>
	 * 주의: {@code <row>}, {@code <c>}와 같이 값이 없는 태그에서는
	 * 이 메서드가 호출되지 않으므로, 로직 설계 시 고려가 필요하다.
	 * </p>
	 * <p>
	 */
	@Override
	public void characters(char[] ch, int start, int length) {
		textBuffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		currentContents = textBuffer.toString().trim();
		textBuffer.setLength(0);

		XlsxOoxml.Element element = XlsxOoxml.Element.from(qName);
		extractCellValue(element);

		if (XlsxOoxml.Element.CELL == element) {
			log.debug("[{}] type: {} value: {}",
				 currentCell.getColIdx(),
				 currentCell.getType(),
				 currentCell.getValue()
			);

			handleCellEnd();
			resetCellContext();
		}

		if (XlsxOoxml.Element.ROW == element) {
			handleRowEnd();
		}
	}

	/**
	 * 한 행의 끝. 필요 시 여기서 row-level 로직 처리
	 */
	private void handleRowEnd() {
		if (isCurrentRowInDataArea() && !blankRow) {
			handle(currentRowIdx, cellValues);
		}

		cellValues.clear();
		log.debug("======================[ROW END   {}]======================", currentRowIdx);
	}

	private void handleCellEnd() {
		if (blankRow) {
			blankRow = StringUtils.isBlank(currentCell.getValue());
		}

		if (isCurrentRowInDataArea()) {
			cellValues.put(currentCell.getColIdx(), currentCell.getValue());
		}
	}

	private boolean isCurrentRowInDataArea() {
		return currentRowIdx >= metadata.getHeaderRowTotalCount()
			 && metadata.getFieldSize() > currentCell.getColIdx();
	}

	/**
	 * 셀 종료 시 셀 값을 추출하고 설정한다.
	 *
	 * <pre>
	 * 예시:
	 *
	 *   &lt;c r="A1" t="inlineStr"&gt;
	 *     &lt;is&gt;
	 *       &lt;t&gt;사용자 아이디&lt;/t&gt;
	 *     &lt;/is&gt;
	 *   &lt;/c&gt;
	 *
	 *   &lt;c r="B1" t="s"&gt;
	 *     &lt;v&gt;0&lt;/v&gt; &lt;!-- sharedStrings 인덱스 --&gt;
	 *   &lt;/c&gt;
	 * </pre>
	 * <p>
	 * 셀 타입에 따라 다음과 같이 처리된다:
	 *
	 * <ul>
	 *   <li>inlineStr: {@code <t>} 내부 값 직접 사용</li>
	 *   <li>s (shared string): sharedStrings 인덱스로 문자열 조회 (LRU 캐시 활용)</li>
	 *   <li>그 외: 숫자, 날짜 등 값은 그대로 사용</li>
	 * </ul>
	 * <p>
	 *
	 * @see XlsxOoxml
	 */
	private void extractCellValue(XlsxOoxml.Element element) {
		if (!cellOpen) {
			return;
		}

		XlsxOoxml.CellType cellType = currentCell.getType();
		if (XlsxOoxml.Element.TEXT_ELEMENT == element && cellType.isInlineString()) {
			currentCell = currentCell.withValue(currentContents);
			return;
		}

		if (XlsxOoxml.Element.VALUE == element) {
			currentCell = currentCell.withValue(extractValueByType(currentContents));
		}
	}

	private String extractValueByType(String contents) {
		if (XlsxOoxml.CellType.SHARED_STRING == currentCell.getType()) {
			return sharedStringCache.computeIfAbsent(
				 Integer.parseInt(contents),
				 idx -> sharedStringsTable.getItemAt(idx).getString()
			);
		}
		return contents;
	}

	protected String getAttributeValue(Attributes attributes, XlsxOoxml.Attribute attribute) {
		return attributes.getValue(attribute.value);
	}

	/**
	 * 셀 메타데이터를 초기화한다.
	 *
	 * <p>
	 * 다음 셀 파싱 시 이전 상태가 영향을 미치지 않도록
	 * {@code cellType}, {@code cellValue} 등의 상태를 초기화한다.
	 * </p>
	 */
	private void resetCellContext() {
		cellOpen = false;
		currentCell = SaxCell.EMPTY;
	}

	/**
	 * 간단한 LRU 캐시 구현체
	 *
	 * <p>
	 * SharedStrings 테이블에서 문자열을 가져올 때,
	 * 동일 인덱스에 대한 반복 조회를 피하기 위해 캐시를 사용한다.
	 * </p>
	 */
	@EqualsAndHashCode(of = "maxEntries", callSuper = false)
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
}
