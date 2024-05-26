# H2 2 버전 마이그레이션시 쿼리 예약어 이슈

DML 쿼리 수행시 테이블 또는 컬럼 예약어 이슈.

## double quotes

- globally_quoted_identifiers
    - 테이블명과 모든 컬럼에 대해 따옴표(double quotes)를 추가하는 설정
- auto_quote_keyword
    - 모든 예약어에 대해 따옴표(double quotes)를 추가하는 설정

```yaml
spring:
  jpa:
    properties:
      hibernate:
        globally_quoted_identifiers: true
        auto_quote_keyword: true
```
