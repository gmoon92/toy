package com.gmoon.commons.commonsapachepoi.excel.sax;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Excel .xlsx 파일의 OOXML 구조에서 사용되는 주요 상수 정의
 *
 * <p> 예시 XML: </p>
 * <pre>
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
 *       &lt;v&gt;123&lt;/v&gt; &lt;!-- 숫자 또는 날짜 --&gt;
 *     &lt;/c&gt;
 *   &lt;/row&gt;
 * </pre>
 *
 * <ul>
 *     <li>{@link Element} : 엘리먼트 이름 (예: c, v, is)</li>
 *     <li>{@link Attribute} : 속성명 (예: r, t)</li>
 *     <li>{@link CellType} : 셀 타입 값 (예: s, inlineStr)</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XlsxOoxml {

	/**
	 * OOXML에서 사용되는 주요 엘리먼트(태그) 이름 정의.
	 * <p>
	 * 예: &lt;c&gt;, &lt;v&gt;, &lt;is&gt;, &lt;t&gt;, ...
	 * </p>
	 */
	@RequiredArgsConstructor
	public enum Element {
		ROW("row"),
		CELL("c"),
		VALUE("v"),
		TEXT_ELEMENT("t"),

		IS("is"), // inline string container
		RICH_TEXT_RUN("r"), // 텍스트 안의 스타일 요소
		PHONETIC_RUN("rPh"), // 한자 병음 등
		STYLE_INDEX("s"), // 셀 스타일 속성
		UNKNOWN;

		public final String value;

		Element() {
			this("");
		}

		private static final Map<String, Element> ALL = Arrays.stream(values())
			 .collect(Collectors.toUnmodifiableMap(e -> e.value, Function.identity()));

		/**
		 * XML 태그 이름을 Enum 값으로 변환
		 *
		 * @param value XML 태그 이름 (예: "row", "c", "v", ...)
		 * @return 해당하는 XlsxElement 값, 정의되지 않은 경우 {@link Element#UNKNOWN} 반환
		 */
		public static Element from(String value) {
			return Optional.ofNullable(value)
				 .map(ALL::get)
				 .orElse(UNKNOWN);
		}
	}

	/**
	 * &lt;c r="A1" t="s"&gt; 와 같이 셀 엘리먼트에 사용되는 속성명 정의.
	 */
	@RequiredArgsConstructor
	public enum Attribute {
		REFERENCE("r"),
		TYPE("t");

		public final String value;
	}

	/**
	 * 셀의 t 속성 값으로 정의된 Cell Type.
	 * <p>
	 * 예: t="s" → Shared String, t="inlineStr" → Inline String
	 * </p>
	 */
	@RequiredArgsConstructor
	public enum CellType {
		SHARED_STRING("s"),
		INLINE_STRING("inlineStr"),
		UNKNOWN;

		public final String value;

		CellType() {
			this("");
		}

		private static final Map<String, CellType> ALL = Arrays.stream(values())
			 .collect(Collectors.toUnmodifiableMap(e -> e.value, Function.identity()));

		/**
		 * OOXML 't' 속성 값을 셀 타입으로 변환
		 *
		 * @param value 셀의 t 속성 (예: "s", "inlineStr", 등)
		 * @return 해당 타입에 대응하는 Enum, 알 수 없으면 {@link CellType#UNKNOWN}
		 */
		public static CellType from(String value) {
			return Optional.ofNullable(value)
				 .map(ALL::get)
				 .orElse(UNKNOWN);
		}

		public boolean isInlineString() {
			return this == INLINE_STRING;
		}

	}
}
