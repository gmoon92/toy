# QueryDSL SQL

QueryDSL SQL은 database schema driven codegen(코드 생성기)다.

실제 DB에 직접 접속(JDBC 연결)해서, 테이블 메타정보를 기반으로 **메타클래스(Codegen)** 를 생성한다. 이 메타클래스를 활용하여 **Native SQL** 기반의 **type-safe**하고 **유지보수가 쉬운(maintainable)** 쿼리 코드를 작성할 수 있다.

## 주요 특징

- Type-safe: 컴파일 시점에 쿼리 오류를 감지하여 안전한 코딩 가능
- 코드 자동 생성: 메타클래스 생성으로 보일러플레이트 코드 제거
- Native SQL 최적화: `multi-insert`, 복잡 쿼리, DB 고유 함수 사용 등 최적화 지원
- 프레임워크 독립적: JPA/Hibernate와 무관하게 JDBC, Spring JDBC 등 다양한 환경에서 사용 가능
- 유지보수성 향상: 테이블 구조 변경 시 코드 제너레이터 재실행만으로 메타클래스 최신화

## 기대 효과

메타클래스를 활용하면 JPA의 한계를 극복하여, DB 전용 함수 사용, 복잡한 조회 쿼리를 type-safe 하게 작성, multi-insert 등 퍼포먼스가 중요한 영역에서의 최적화를 기대할 수 있다.

## QueryDSL JPA와 SQL 모듈의 차이

- **QueryDSL JPA**: ORM(Entity) 기반의 type-safe한 쿼리 생성을 위한 QDomain 클래스 자동 생성
- **QueryDSL SQL**: 실제 DB 스키마/테이블 메타 기반으로 Native SQL 쿼리를 빌더 형태로 type-safe하게 캡슐화 (복잡 쿼리, 성능 중심의 데이터 처리에 유리)

> 흔히 알고 있는 `QDomain`은 QueryDSL JPA의 codegen 이다. <br/>
> QueryDSL SQL은 Native SQL을 type-safe하게 지원하기 위해 QueryDSL 프로젝트의 별도 하위 모듈이다..

## 주요 활용 사례

- 대용량 bulk insert/update, 고성능 write-intensive 시스템
- 레거시 JDBC 기반 프로젝트에서 type-safe 쿼리 적용
- ORM(JPA) 일부 또는 전체 대체가 필요한 경우

## Dependencies

```text
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-sql</artifactId>
    <version>${querydsl.version}</version>
</dependency>

<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-sql-codegen</artifactId>
    <version>${querydsl.version}</version>
    <scope>provided</scope>
</dependency>
```

## Code generation via Maven

```xml

<project>
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.querydsl</groupId>
                <artifactId>querydsl-maven-plugin</artifactId>
                <version>${querydsl.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>export</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <jdbcDriver>org.apache.derby.jdbc.EmbeddedDriver</jdbcDriver>
                    <jdbcUrl>jdbc:derby:target/demoDB;create=true</jdbcUrl>
                    <packageName>com.myproject.domain</packageName>
                    <targetFolder>${project.basedir}/target/generated-sources/java</targetFolder>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.derby</groupId>
                        <artifactId>derby</artifactId>
                        <version>${derby.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
            ...
        </plugins>
    </build>
</project>
```

## Reference

- [QueryDSL 공식 문서 - SQL 모듈](http://querydsl.com/static/querydsl/latest/reference/html_single/#sql_integration)
