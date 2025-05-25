# BeanIO

[BeanIO](http://beanio.org/)는 플랫 파일(XML, CSV 등), 구분 기호로 구분된 스트림, 또는 문자열 객체에서 Java Bean을 마샬링/언마샬링 하는 오픈소스 Java 프레임워크다.

이 프로젝트는 BeanIO를 학습하기 위해 다양한 전문(message) 포맷(고정길이, CSV, 구분자, 커스텀 바이너리 등)의 직렬화와 역직렬화(파싱) 기능이 어떻게 지원되는지 살펴본다.

## Features

- 다양한 메시지 전문 포맷(XML, CSV, 구분자, 고정길이, ISO8583 등) 지원
- BeanIO 기반 매핑 및 XML 기반 레이아웃 정의 지원
- DTO와 전문 메시지 간 자동 변환(직렬화/역직렬화) 가능
- 필드 매핑 방식(XML, 자바 어노테이션, Builder API 등) 다양
- 레코드 순서 및 그룹핑 설정, 객체 바인딩, 조건부 레코드 식별 가능
- 프로토콜별 메시지 타입 분기 및 식별 가능
- 필드 검증 규칙, 사용자 정의 에러 메시지, 커스텀 Validator·컨버터·이벤트 리스너 확장 지원
- 스트림 파싱 및 타입 핸들러 확장, 고성능 패킷 처리 지원
- [Spring Batch](https://docs.spring.io/spring-batch/) 통합 지원
- OSGi 환경 호환 가능

## Quick Start

```xml

<beanio xmlns="http://www.beanio.org/2012/03">

    <stream name="contacts" format="csv"> <!-- [1] -->

        <record name="header" class="map"> <!-- [2] -->
            <field name="recordType" rid="true" literal="H"/> <!-- [3] -->
            <field name="fileDate" type="date" format="yyyy-MM-dd"/> <!-- [4] -->
        </record>

        <record name="body" class="example.UserContactDto"> <!-- [5] -->
            <field name="recordType" rid="true" literal="D" ignore="true"/> <!-- [6] -->
            <field name="firstName"/> <!-- [7] -->
            <field name="lastName"/> <!-- [8] -->
            <field name="street"/> <!-- [9] -->
            <field name="city"/> <!-- [10] -->
            <field name="state"/> <!-- [11] -->
            <field name="zip"/> <!-- [12] -->
        </record>

        <record name="trailer" target="recordCount"> <!-- [13] -->
            <field name="recordType" rid="true" literal="T"/> <!-- [14] -->
            <field name="recordCount" type="int"/> <!-- [15] -->
        </record>
    </stream>
</beanio>
```

- [1] stream 요소로 매핑할 전체 데이터 스트림의 이름(contacts)과 포맷(csv) 지정
- [2] 헤더 레코드 정의
    - `class="map"`: `java.util.HashMap`에 바인딩
    - [3] 식별자 필드: recordType
        - `rid`로 레코드 식별자 필드 지정
        - `literal` 속성으로 헤더 타입(`H`)임을 명시
    - [4] 필드: fileDate
        - 날짜 타입, "yyyy-MM-dd" 포맷 적용
        - `type`은 `Date(date)`와 `Number(int)`을 지원.
- [5] 상세(body) 레코드 정의
    - 바인딩할 Java 클래스 지정
    - [6] 식별자 필드: recordType
        - `rid`로 레코드 식별자 필드 지정
        - `literal` 속성으로 데이터 타입(`D`)임을 명시
        - `ignore` 옵션이 활성화된 필드는 DTO에 바인딩하지 않음
    - [7]~[12] 실제 데이터 필드에 해당되는 각 속성 정의
- [13] 트레일러 레코드 정의
    - 전체 레코드 개수를 나타내는 `recordCount`에 바인딩
    - [14] 식별자 필드: recordType
        - `rid`로 레코드 식별자 필드 지정
        - `literal` 속성으로 데이터 타입(`T`)임을 명시
    - [15] 실제 레코드 개수 필드. 타입은 `int`

## Reference

- [BeanIO](http://beanio.org/)
- [BeanIO 2.0 reference](http://beanio.org/2.0/docs/reference/index.html)
- [kevinseim - beanio](https://github.com/kevinseim/beanio)
