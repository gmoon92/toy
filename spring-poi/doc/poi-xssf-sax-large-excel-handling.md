# Apache POI와 SAX를 활용한 엑셀 대용량 처리
## 엑셀 파일 구조 이해와 대용량 XML 처리 전략

업무 시스템에서 엑셀은 여전히 많이 쓰인다. 기획자나 운영자는 데이터를 쉽게 열어보고 수정할 수 있고,
개발자도 임시 데이터를 넣거나 테스트 케이스를 만들 때 엑셀을 자주 활용한다.

그런데 문제는 "엑셀 파일이 커질수록 느려진다"는 데 있다.

엑셀을 처리하는 대부분의 자바 라이브러리는 파일 전체를 메모리에 올리는 방식이다.
이 구조는 1만 건 수준에서는 문제가 없지만, 수십만 건만 넘어가도 OutOfMemory가 발생하거나, 시스템 전체가 느려지기 시작한다.

이 글에서는 이런 대용량 엑셀 문제를 해결하기 위해, Apache POI의 XSSF와 SAX를 조합해 "엑셀을 읽는 즉시 처리하고 버리는 방식"을 어떻게 구현했는지 정리한다.

----

## TOC

1. 왜 엑셀 업로드가 문제인가
   - 흔한 구현 방식과 그 한계
   - 파일 크기와 메모리 문제
2. Excel 파일 구조 이해
   - `.xlsx`는 압축된 폴더 구조
   - 시트 데이터는 XML로 저장됨
3. 대용량 XML 처리 전략
   - DOM vs. SAX
   - SAX의 이벤트 기반 처리 방식
   - 엑셀에서 SAX를 활용하려면?
4. Apache POI + SAX로 엑셀 파싱하기
   - Apache POI의 구조
   - `XSSFReader`로 시트 XML 접근
   - `XMLReader`로 SAX 이벤트 파싱
5. SAX 기반 구현 예시
   - 주요 구성 클래스와 역할
   - `SheetHandler` 내부 흐름
   - 이벤트 콜백의 동작 방식
6. 실전 적용 시 고려할 점
   - 성능 테스트 결과 요약
   - 커스텀 유효성 검사/매핑 전략
   - 에러 처리와 사용자 피드백
7. 마무리: 우리가 얻은 교훈
   - 언제 SAX가 유리한가?
   - 단순한 API 호출을 넘어, 파일 포맷을 이해하는 사고방식

---

## 엑셀 파일 포맷의 변화

엑셀 파일은 `.xls`와 `.xlsx` 두 포맷이 대표적이다.

- `.xls`: BIFF(Binary Interchange File Format) 기반의 바이너리 포맷 (Excel 97-2003)
- `.xlsx`: XML 기반, ZIP 압축을 이용한 표준 포맷 (Excel 2007 이상)

| 항목        | `.xls`                  | `.xlsx`                                        |
|-----------|-------------------------|------------------------------------------------|
| 내부 구조/형식  | BIFF (바이너리 파일)          | `Office Open XML` (`OOXML`: ZIP + XML)         |
| 포맷 표준화    | 비표준, 일부만 공개             | 국제 표준(ECMA-376, ISO/IEC 29500), 완전 공개          |
| 최대 행/열    | 65,536 / 256 (IV)       | 1,048,576 / 16,384 (XFD)                       |
| 확장성 및 유연성 | 낮음                      | UTF-16 지원, 구조적 유연성 높음                          |
| 특징        | 속도 빠름, 단순, 파싱·복구 어렵고 취약 | 용량 작음, 도구 호환성 우수, 구조 더 복잡·파싱 도구 필요(POI/XSSF 등) |


### `.xls` vs `.xlsx`

기존 엑셀 파일 바이너리 포맷(`.xls`) 방식은 다음과 같은 문제가 있었다.

- 비공개 이진 포맷 → 구조 분석이나 파싱이 어려움
- MS 독점 → 서드파티 도구에서의 문서 호환성 낮음
- 압축 없음 → 용량이 크고, 손상 시 복구 어려움
- 행/열 수 제약 → 대용량 데이터 처리에 한계

### Office Open XML(OOXML): `.xlsx`는 사실상 ZIP + XML이다

2006년, Microsoft는 기존의 폐쇄적인 바이너리 엑셀 포맷(`.xls`)을 대체하기 위해 **Office Open XML (OOXML)** 포맷을 공개했다. 
이후 `OOXML` 포맷은 **ECMA-376**, **ISO/IEC 29500** 국제 표준으로 제정되며 전 세계적으로 사용되기 시작했다.

`.xlsx` 엑셀 파일은 내부 워크북, 시트, 셀, 스타일, 메타 데이터들을 `OOXML` 포맷으로 역활별로 각각 분리 관리하며 
기존 바이너리에서 XML 데이터 형식으로 변환하면서 늘어난 용량을 해결하기 위해 ZIP 으로 압축한 구조를 띈다.

| 항목       | 기존 `.xls`       | OOXML (`.xlsx`)               |
|----------|-----------------|-------------------------------|
| 저장 방식    | 바이너리            | ZIP + XML                     |
| 구조 공개 여부 | 비공개 (사양 복잡)     | 국제 표준 (ECMA-376 / ISO-29500)  |
| 플랫폼 호환성  | 낮음 (Windows 전용) | 높음 (Mac, 웹, Linux 등에서도 사용 가능) |
| 대용량 지원   | 한계 존재           | 수십만 행 이상도 처리 가능               |
| 파일 복원성   | 손상에 취약          | ZIP 구조로 일부 손상에도 복구 가능         |

- 국제 표준 포맷으로 누구나 구조를 확인 가능
- ZIP 기반으로 용량이 작고 복구 가능성 높음
- XML 구조라 다양한 언어/도구에서 처리 가능

덕분에 Excel뿐 아니라 **Google Sheets, Apple Numbers, OpenOffice** 같은 다양한 도구에서도 `.xlsx`를 자유롭게 읽고 쓸 수 있게 되었다. 

다시 말해, `.xlsx`는 **단순히 MS Excel만을 위한 포맷이 아니라, 다양한 시스템 간 데이터 교환을 위한 공개 표준**이다. OOXML은 "대용량 전용 포맷"은 아니다. `.xlsx`는 대용량 처리에 유리하긴 하지만, 단지 대용량 전용 포맷은 아니다. **문서가 10줄이든, 100만 줄이든**, `.xlsx` 구조는 동일하다.

> 지금도 `.xls`가 쓰이긴 하나요?
> 물론 `.xls`도 여전히 열 수는 있지만, **"호환성 모드"** 로 표시되고, 저장하려고 하면 "일부 기능은 이 포맷에서 지원되지 않습니다"라는 경고가 나타난다. 기본 저장 포맷은 `.xlsx`로 전환되었고, `.xls`는 일부 예외적 상황에만 사용되고 있다.

### 이걸 왜 알아야 할까?

핵심은 `.xlsx` 파일은 사실상 **정형화된 XML**이라는 점이다.

```
unzip -l sample.xlsx

Archive:  sample.xlsx
  Length      Date    Time    Name
---------  ---------- -----   ----
     1397  00-00-1980 00:00   [Content_Types].xml           <- MIME 타입 정보
      586  00-00-1980 00:00   _rels/.rels                   <- 관계(Relationship) 정보
      183  00-00-1980 00:00   docProps/app.xml
      433  00-00-1980 00:00   docProps/core.xml             <- 문서 메타데이터
      519  00-00-1980 00:00   xl/comments1.xml
      130  00-00-1980 00:00   xl/drawings/drawing1.xml
     1894  00-00-1980 00:00   xl/drawings/vmlDrawing0.vml
      137  00-00-1980 00:00   xl/sharedStrings.xml          <- 중복 문자열 캐시
      674  00-00-1980 00:00   xl/styles.xml
      348  00-00-1980 00:00   xl/workbook.xml               <- 워크북 메타 정보
      564  00-00-1980 00:00   xl/_rels/workbook.xml.rels    <- 관계(Relationship) 정보
     1169  00-00-1980 00:00   xl/worksheets/sheet1.xml      <- 실제 시트 데이터
      580  00-00-1980 00:00   xl/worksheets/_rels/sheet1.xml.rels
---------                     -------
     8614                     13 files
```

따라서 우리가 엑셀 데이터를 읽으려면 결국 다음 중 하나를 택해야 한다:

- XML을 전부 메모리에 올려 DOM 방식으로 파싱 → 대용량에 불리
- 이벤트 기반 SAX 방식으로 읽기 → 대용량에 유리하고 안정적

그리고 OOXML의 구조는 **SAX 방식에 딱 맞도록 설계**되어 있다.
**다음 섹션에서는 왜 SAX가 필요한지**, 그리고 **OOXML 엑셀 데이터(XML)의 구조가 어떤지**를 자세히 살펴보자.

---

## Apache POI

Apache POI는 Excel 파일을 **네 가지 방식**으로 처리한다.

### Workbook 인터페이스 종류

| 인터페이스           | 포맷    | 파서 방식                | 설명                                                                  |  
|-----------------|-------|----------------------|---------------------------------------------------------------------|
| `HSSFWorkbook`  | .xls  | 바이너리, DOM 방식         | 읽기/쓰기 지원, 바이너리(구버전) 포맷 엑셀 지원                                        |  
| `XSSFWorkbook`  | .xlsx | XML 기반, DOM 방식       | 읽기/쓰기 지원, DOM 방식으로 동작되며, 셀 스타일 접근/수정과 같은 다양한 기능을 자체 제공하고 있어 개발에 용이함 |   
| `SXSSFWorkbook` | .xlsx | 스트리밍 기반, 쓰기 전용       | 스트리밍 쓰기 전용. 내부 버퍼 기반으로 동작                                           |  
| `XSSF + SAX`    | .xlsx | SAX 기반 이벤트 파싱, 읽기 전용 | SAX를 이용한 이벤트 기반 XML 파싱. 대용량 엑셀 읽기에 적합                               |  

### DOM(Tree) vs SAX

Workbook 인터페이스를 선택할 때는 두 가지 파서 방식—DOM(Tree) 기반과 SAX(Stream) 기반 중 어느 쪽이 더 적합한지를 판단해야 한다.  

이 선택은 다음 두 가지 요구사항 중 어떤 것이 중요한지에 따라 달라진다:

- 대용량 엑셀 데이터를 안정적으로 처리할 것인가?
- 엑셀의 셀, 스타일, 수식 등을 정밀하게 조작할 것인가?

#### DOM 방식: 직관적이고 강력하지만 메모리 부담 있음

DOM 방식은 `.xlsx` 파일 내부의 XML들을 압축 해제한 뒤, 전체 구조를 한꺼번에 메모리에 로드한다.  
이 방식은 문서 전체를 탐색하거나 특정 셀의 스타일, 수식 등을 조작하기에 매우 용이하다.  
또한 Apache POI는 DOM 방식에서 다양한 편의 기능을 기본 제공하기 때문에, SAX에 비해 개발 난이도가 낮고 직관적이다.

#### SAX 방식: 이벤트 기반 스트리밍 처리에 적합

반면 SAX는 XML 문서를 이벤트 방식으로 스트리밍 처리한다.

수십 MB 이상의 대용량 `.xlsx` 파일을 처리할 경우, 
DOM 방식은 메모리 부족 문제를 유발할 수 있다. 이러한 상황에서는 이벤트 기반으로 데이터를 읽어들이는 `SAX` 방식이 훨씬 더 안정적이고 적합하다.

| 비교 항목  | DOM               | SAX                       |
|--------|-------------------|---------------------------|
| 방식     | 전체 XML을 메모리에 로딩   | **이벤트 기반 처리** (Streaming) |
| 메모리 사용 | 높음                | 매우 적음                     |
| 대용량 파일 | 불리                | **유리**                    |
| 예시     | `DocumentBuilder` | `SAXParser`               |

**수십 MB 이상의 대용량 엑셀(XML) 파일을 파싱하려면 반드시 SAX 기반 파서를 활용해야 한다.**

- DOM 방식: 전체 XML을 메모리에 올려야 하므로 대용량에 부적합
- SAX 방식: 이벤트 기반으로 XML을 한 줄씩(이벤트마다) 읽어가며 순차적으로 처리 
  - 메모리 소모 없이 대용량도 안전하게 다룰 수 있다

### SAX 기반 파싱과 XML 처리 방식

앞서 OOXML의 구조와 대용량 처리 필요성을 살펴봤다면, 이제 실제로 어떻게 이 XML을 효율적으로 읽을 수 있을지 알아보자. 

SAX는 대표적인 이벤트 기반 XML 파서로, DOM과는 전혀 다른 방식으로 XML을 다룬다. 다음은 간단한 XML 문서가 SAX에서 어떤 이벤트 순서로 처리되는지를 보여주는 예제다.

```xml
<?xml version="1.0"?>
<doc> 
    <para>Hello, world!</para>
</doc>
```

1. start document
2. start element: `doc`
3. start element: `para`
4. characters: `Hello, world!`
5. end element: `para`
6. end element: `doc`
7. end document

SAX는 이처럼 XML 요소를 "순차적으로 이벤트 처리"하기 때문에, 엑셀 XML의 구조를 어느 정도 알고 있어야만 필요한 데이터에 접근할 수 있다. 전체 문서를 통째로 메모리에 올리지 않기 때문에 성능상 유리하지만, 그만큼 문서 구조에 대한 이해가 선행되어야 한다.

> SAX의 동작 방식과 구현 예제에 대한 더 자세한 설명은 [simple-api-for-xml.md](simple-api-for-xml.md) 문서를 참고하자.

---

## Apache POI XSSF와 SAX 구조

대용량 엑셀 파일을 처리할 때 가장 현실적인 대안은 SAX(Event 기반 XML 처리 방식)를 사용하는 것이다. 

Apache POI의 XSSF 구현체는 내부적으로 Office Open XML (OOXML) 형식의 Excel(`.xlsx`) 파일을 SAX 방식으로 파싱할 수 있는 기능을 제공한다.

### 1. `OPCPackage`와 `.xlsx` 파일 구조

Excel의 `.xlsx` 파일은 사실상 ZIP 압축된 디렉토리 구조이다. 이를 압축 해제하면 여러 개의 XML 파일로 구성된 내부 구조를 확인할 수 있다.

이 구조에 접근하기 위해 POI는 `OPCPackage`를 사용한다.

```text
[Content_Types].xml           <- MIME 타입 정보
_rels/.rels                   <- 관계(Relationship) 정보
docProps/app.xml
docProps/core.xml             <- 문서 메타데이터
xl/comments1.xml
xl/drawings/drawing1.xml
xl/drawings/vmlDrawing0.vml
xl/sharedStrings.xml          <- 중복 문자열 캐시
xl/styles.xml
xl/workbook.xml               <- 워크북 메타 정보
xl/_rels/workbook.xml.rels    <- 관계(Relationship) 정보
xl/worksheets/sheet1.xml      <- 실제 시트 데이터
```

그중에서도 실제 셀 데이터를 담고 있는 핵심 파일은 `sheet1.xml`이며, SAX로 이 XML을 스트리밍하면서 메모리를 최소화할 수 있다.

| 클래스 이름                         | 설명                                                       |
|--------------------------------|----------------------------------------------------------|
| `OPCPackage`                   | `.xlsx`, `.docx` 등 Office 파일 전체를 zip 구조로 여는 클래스.         |
| `PackagePart`                  | zip 내 개별 XML/이미지 등 각 파트(예: `/xl/worksheets/sheet1.xml`)  |
| `PackagePartName`              | 개별 파트의 경로(예: `/xl/worksheets/sheet1.xml`) 지정             |
| `PackageRelationship`          | 파트 간 연결 정보(예: workbook → sheet1.xml 등 관계)                |
| `PackageProperties`            | 문서 메타데이터(제목, 작성자 등) 접근용                                  |

### 2. SAX 기반 Excel 파싱 흐름

Apache POI는 `org.apache.poi.xssf.eventusermodel` 패키지를 통해 SAX 방식 엑셀 파싱을 지원한다.

| 클래스 이름                       | 설명                                                                                            |
|------------------------------|-----------------------------------------------------------------------------------------------|
| `XSSFReader`                 | `.xlsx`파일에서 시트, 스타일, 공유 문자열 등 각 XML 파트를 스트림으로 가져온다.                                           |
| `XSSFReader.SheetIterator`   | 모든 시트(sheetX.xml)에 대해 반복적으로 스트림 제공                                                            |
| `XMLReader`                  | SAX 파서의 핵심 객체.                                                                                |
| `SharedStringsTable`         | 공유 문자열 테이블.                                                                                   |
| `StylesTable`                | 셀 스타일(포맷, 날짜/숫자 포맷 등)을 담은 테이블                                                                 |
| `ReadOnlySharedStringsTable` | 메모리 사용량을 줄이기 위한 공유 문자열의 read-only 버전                                                          |
| `DefaultHandler`             | SAX 이벤트 처리용 핸들러. `startElement()`, `characters()`, `endElement()` 등을 오버라이드하여 XML 파싱 로직을 구현한다. |

#### 셀 데이터 파싱 핵심 태그

SAX 방식으로 `sheet1.xml`을 처리할 때, 엑셀 데이터를 구성하는 핵심 태그는 다음과 같다:

```xml
<row r="1">
  <c r="A1" t="s">
    <v>0</v>
  </c>
  <c r="B1">
    <v>123</v>
  </c>
</row>
```

SAX는 이 구조를 다음과 같이 이벤트 기반으로 처리한다:
- `<row>` 태그 등장: 새로운 행 준비
- `<c>` 태그 등장: 셀 주소(A1 등), 타입(s: 공유 문자열) 파악
- `<v>` 태그 등장 후 characters() 호출: 실제 값 추출
- `</row>` 태그: 한 행의 데이터 완성 및 버퍼 비우기

이 방식은 메모리 점유를 최소화하면서 수십만 행의 데이터도 안정적으로 처리할 수 있다.

> 문자열 셀의 경우 `<c t="s">`로 표시되며, `<v>` 내부 값은 `sharedStrings.xml`의 인덱스를 참조해 실제 문자열을 추출해야 한다.

#### 주의할 점: 스타일, 날짜 공백 셀

SAX 방식은 최소한의 정보만 제공되므로 다음과 같은 경우 직접 처리해야 한다:

- 공백 셀은 `<c>` 태그 자체가 누락될 수 있음 → 셀 주소 기준으로 누락 여부 판단 필요
- 날짜 포맷은 `styles.xml`을 참고해야 실제 날짜/시간인지 파악 가능
- 숫자와 문자열 구분은 `<c>` 태그의 `t` 속성 및 스타일 정보에 따라 달라짐

따라서 완전한 값 파싱을 위해서는 `sharedStrings.xml`, `styles.xml`을 함께 분석하는 전처리 로직이 필요하다.

### 3. Shared Strings 처리

Excel은 문자열이 반복되는 경우, 메모리 절약을 위해 별도의 테이블(`sharedStrings.xml`)에 중복 문자열을 모아두고 인덱스를 참조하는 방식으로 저장한다. 

`XSSFReader`를 통해 공유 문자열 객체(`SharedStringsTable`)를 반환받을 수 있다.

```java
// SAX로 sharedStrings.xml을 먼저 읽어 미리 로딩
XSSFReader reader = new XSSFReader(pkg);
SharedStrings sst = reader.getSharedStringsTable();

XMLReader parser = XMLHelper.newXMLReader();
parser.setContentHandler(new SAXSheetHandler(sst));
```

이후 핸들러에서 `<c t="s">` 셀을 만나면 `<v>` 태그에서 읽은 값(int형 인덱스)을 통해 실제 문자열을 가져온다.

```java
String actualValue = sharedStrings.get(Integer.parseInt(rawValue));

// 문자열(shared string): index를 SharedStrings 인덱스를 참조하여 실제 문자열 가져오기
int idx = Integer.parseInt(currentValue);
String realValue = sharedStringsTable.getItemAt(idx).getString();
```

### 4. 샘플 코드: SAX 방식 엑셀 파싱

앞에서 설명한 흐름을 코드로 구현하면 다음과 같다.

이 코드는 `.xlsx` 파일의 첫 번째 시트를 SAX 방식으로 파싱하며, 문자열 셀과 숫자 셀을 구분해 처리한다.

```java
/**
 * 아파치 POI의 XSSFReader와 XMLReader(SAXParser)를 사용해
 * 엑셀(.xlsx)의 Sheet1 데이터를 SAX 방식으로 이벤트 기반 처리하는 샘플 코드
 */
class XSFFWorkbookSAXTest {

    @Test
    void test() throws Exception {
        // 1. OPCPackage를 통해 .xlsx 파일 열기 (ZIP 압축 해제처럼 동작)
        try (OPCPackage pkg = OPCPackage.open(new File("example.xlsx"))) {
            // 2. XSSFReader 생성: 엑셀 내부 구조(워크북, 시트 등) 접근
            XSSFReader reader = new XSSFReader(pkg);

            // 3. SharedStringsTable 가져오기: 셀 값이 문자열(s) 타입인 경우 참조
            SharedStringsTable sst = reader.getSharedStringsTable();

            // 4. SAX 기반 XML 파서(XMLReader) 생성 및 커스텀 핸들러 등록
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new ExcelSheetHandler(sst));

            // 5. Sheet 데이터(여기선 첫 번째 시트) 스트림 추출
            InputStream sheet = reader.getSheetsData().next();

            // 6. SAX 파서로 XML 스트림 파싱(= Sheet XML 스트리밍 진행)
            parser.parse(new InputSource(sheet));
        }
    }

	/**
	 * SAX 방식의 이벤트 핸들러 
     * 각 엑셀 셀/행의 시작, 종료 시점에 데이터를 직접 제어 가능
	 */
    public static class ExcelSheetHandler extends DefaultHandler {
        private final SharedStringsTable sharedStringsTable;
		private String cellType; // 셀 타입 ("s"이면 SharedStrings에서 읽어야 함)
		private String currentValue; // 현재 셀의 값

        public ExcelSheetHandler(SharedStringsTable sst) {
            this.sharedStringsTable = sst;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("c".equals(qName)) {
				// 셀 시작 <c t="s" r="A1">
                // "c" 태그: 셀(cell) 시작. 
                // "t" 태그: 셀 타입 정보 
                cellType = attributes.getValue("t"); // "s"이면 shared string
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            // 태그 사이에 실제 값(문자, 숫자든 인덱스든) 저장 (row, c, v 태그 내 값)
            currentValue = new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("v".equals(qName)) {
				// 셀 값 <v> 태그 종료 시점: 셀 값의 실제 처리 로직
                // cellType에 따라 실제 값 해석
                if ("s".equals(cellType)) {
                    // 문자열(shared string): index를 SharedStrings 인덱스를 참조하여 실제 문자열 가져오기
                    int idx = Integer.parseInt(currentValue);
                    String realValue = sharedStringsTable.getItemAt(idx).getString();
                    // 여기서 realValue 사용 (로깅, 리스트에 추가 등)
					System.out.println("String Cell: " + realValue);
				} else {
                    // 기타(숫자 등): currentValue 자체가 값
                    // 여기서 currentValue 사용
					System.out.println("Other Cell: " + currentValue);
				}
            } else if ("row".equals(qName)) {
				// 한 행의 끝. 필요 시 여기서 row-level 로직 처리 가능
                // 한 '행'이 끝났으므로, 필요한 후처리(예: 한 줄 완성 후 리스트에 추가 등)
				System.out.println("End of Row");
			}
            // 필요시 cellType/currentValue 초기화
        }
    }
}
```

다음 코드는 SAX 방식으로 엑셀의 셀 데이터를 파싱하는 기본적인 흐름을 보여준다.
흐름을 요약하면 다음과 같다:

1. `<row>` 태그가 등장하면 새로운 행 준비
2. `<c>` 태그에서 열 위치와 타입 확인
3. `<v>` 태그 내부 값 처리 (`characters()` 콜백)
4. `</row>` 태그에서 하나의 행을 완성

이 모든 과정은 SAX의 콜백 메서드인 `startElement()`, `characters()`, `endElement()`를 통해 순차적으로 제어된다.

---

## 마무리: XSSF와 SAX Event API, 그리고 XML 스트리밍 처리의 본질

이번 글은 **Apache POI의 XSSF와 SAX(Event API)** 를 활용해 엑셀을 스트리밍 방식으로 읽는 방법을 다뤘다. 겉보기에 단순한 "엑셀 업로드 최적화" 주제지만, 이 안에는 OOXML이라는 포맷과 XML 스트리밍 파싱이라는 본질적인 기술이 자리하고 있다.

우리가 흔히 사용하는 `.xlsx` 파일은 사실상 압축된 XML 파일들의 모음이다. 이를 ZIP 압축을 풀어 내부의 `sheet1.xml` 등을 꺼내면, 수십만 줄의 XML이 담겨 있다. 여기에 등장하는 주요 태그는 단 세 가지다.

1. `<row>` 태그가 등장하면 새로운 행을 준비하고,
2. `<c>` 태그에서 열 위치와 타입을 파악한 뒤,
3. `<v>` 태그 안의 값을 `characters()`에서 읽고,
4. `</row>` 태그에서 하나의 행을 완성한다.

이 단순한 규칙 덕분에 우리는 전체 XML을 메모리에 올리지 않고도, 이벤트 기반으로 엑셀을 행 단위로 읽어낼 수 있다. 바로 이 점이 SAX 기반 XML 파싱의 핵심이며, 대용량 엑셀 처리를 가능하게 해준다.

### 중요한 건 엑셀이 아니라 XML이다

이 글의 초점은 "엑셀 파일을 어떻게 읽느냐"가 아니다. 진짜 메시지는 이거다:

> **SAX는 단순한 XML 파싱 기술이 아니다. 대용량 스트리밍 처리를 위한 패턴이다.**

XSSF와 SAX의 조합은 엑셀이라는 특정 도구를 넘어서, 우리가 대용량 데이터를 다룰 때 어떤 방식으로 접근해야 하는지를 보여준다. 필요한 부분만 읽고, 메모리에 머무르는 시간은 최소화하며, 처리 후 바로 버리는 방식. 이건 엑셀뿐만 아니라 JSON, 로그, CSV 등 어떤 스트리밍 대상에도 적용 가능한 일반적인 전략이다.

또한 SAX의 구조는 도메인 설계 관점에서도 흥미롭다. `startElement()`, `characters()`, `endElement()`는 명확한 순서를 따르는 상태 기계처럼 동작하고, 각 콜백은 특정 역할만 담당한다. 이건 우리가 흔히 말하는 "단일 책임 원칙"을 자연스럽게 따르고 있다. 객체지향을 코드로 배우는 게 아니라, XML 파서 구조 안에서 몸으로 느끼는 순간이다.

### 실무에서 기억해야 할 포인트

* `.xlsx`는 압축된 XML 구조이며, `sheet1.xml`이 실제 데이터 본문이다.
* `XSSFReader`와 `XMLReader`를 활용해 스트리밍 방식으로 데이터를 읽을 수 있다.
* SAX 방식은 메모리 사용량을 획기적으로 줄일 수 있지만, 순차적으로만 접근할 수 있다는 단점도 있다.
* 구조적으로 SAX 핸들러는 상태 기계처럼 동작하며, 복잡한 Excel 구조도 계층적으로 해석할 수 있다.

### 마지막으로: 기술을 바라보는 관점

우리는 자주 도구 중심으로 문제를 본다. "엑셀 업로드 어떻게 최적화하지?" 같은 질문 말이다. 하지만 그 도구 뒤에 어떤 **표현 방식(XML)** 이 있고, 그 표현을 어떻게 **구조적으로 해석할 수 있을지** 고민한다면, 더 깊은 인사이트를 얻을 수 있다.

이 글을 통해 여러분이 XML과 스트리밍 파싱을 더 잘 이해하고, 앞으로 어떤 대용량 데이터 포맷을 만나더라도 겁먹지 않게 되길 바란다. 중요한 건 기술이 아니라 **그 기술이 문제를 푸는 방식**이다.

---

## Reference

- [Apache POI - XSSF SAX (Event API)](https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api)
- [Apache POI - Streaming Sample](https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/FromHowTo.java)
- [www.saxproject.org](http://www.saxproject.org/about.html)
- OOXML 공식 표준
    - [ECMA-376, ISO/IEC 29500](https://ecma-international.org/publications-and-standards/standards/ecma-376/)
    - [Microsoft - Welcome to the Open XML SDK for Office](https://learn.microsoft.com/en-us/office/open-xml/open-xml-sdk)
    - [Microsoft - Office(2007) Open XML 파일 형식 소개](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2007/aa338205(v=office.12)?redirectedfrom=MSDN)
        - [Microsoft - [MS-XLSX]: Excel (.xlsx) Extensions to the Office Open XML SpreadsheetML File Format](https://learn.microsoft.com/en-us/openspecs/office_standards/ms-xlsx/)
- Apache POI SAX 기반 처리 문서
    - [Apache POI - XSSF and SAX (Event API)](https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api)
    - [Apache POI GitHub - XSSF SAX Sample code](https://github.com/apache/poi/blob/trunk/poi-examples/src/main/java/org/apache/poi/examples/xssf/eventusermodel/FromHowTo.java)
- 그외
    - [Difference Between XLS and XLSX](https://www.differencebetween.net/technology/difference-between-xls-and-xlsx/)
    - [Microsoft - What is the real difference between a .xls vs .xlsx file](https://answers.microsoft.com/en-us/msoffice/forum/all/what-is-the-real-difference-between-a-xls-vs-xlsx/c7a9d641-d6cf-485d-81d8-1bee60bb17d0)
    - [ECMA-376 Standard](https://www.ecma-international.org/publications-and-standards/standards/ecma-376/)
    - [Open XML SDK docs by Microsoft](https://learn.microsoft.com/en-us/office/open-xml/)
    - https://learn.microsoft.com/ko-kr/dotnet/standard/linq/example-outputs-office-open-xml-document-parts
