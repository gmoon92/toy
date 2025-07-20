# Spring POI

## Components

- `ExcelSaxRowEventReader` : `XSSFReader`와 `XMLReader`를 연결하여 실제 XML 이벤트 흐름을 처리하는 중심 클래스
- `SaxRowHandler` : `<row>` 단위로 콜백을 받기 위한 인터페이스. 호출자는 이 인터페이스를 구현해 각 행을 어떻게 처리할지 정의한다.
- `ExcelStreamingReader` : 실제로 외부에서 사용하는 진입점 API로, 파일/스트림을 넘기면 행 단위 처리를 시작해준다.
- `ExcelSheet<T>` : 한 시트의 행들을 특정 VO로 매핑한 컬렉션으로 구성한 도메인 표현 객체
