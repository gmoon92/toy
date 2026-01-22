# Gradle 의존성 스코프 정리 및 Maven 비교

## Gradle 의존성 스코프 종류와 의미

| Gradle 스코프           | 의미 요약                                 | Maven 대응 스코프    |
|----------------------|---------------------------------------|-----------------|
| `implementation`     | 내 코드에서 사용하고, 이걸 상속한 모듈엔 노출되지 않음 (캡슐화) | `compile` (기본)  |
| `api`                | 내 코드에서 사용하고, 상속한 모듈에도 노출됨 (라이브러리 개발용) | `compile`       |
| `compileOnly`        | 컴파일 시만 필요, 런타임에는 포함 안 됨 (예: Lombok)   | `provided`      |
| `runtimeOnly`        | 컴파일 시엔 불필요하고, 런타임에만 필요 (예: DB 드라이버)   | `runtime`       |
| `testImplementation` | 테스트 코드에서만 사용하는 구현체                    | `test`          |
| `testCompileOnly`    | 테스트 시에만 컴파일용으로 필요한 것                  | `test/provided` |
| `testRuntimeOnly`    | 테스트 시에만 런타임에 필요한 것                    | `test/runtime`  |


## 자주 쓰는 Gradle 의존성 스코프 조합

```groovy
dependencies {
    // 일반 라이브러리 (애플리케이션 코드)
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // 다른 모듈과 공유해야 하면 api
    api 'org.apache.commons:commons-lang3:3.12.0'

    // 컴파일 전용, 런타임에 포함 안 됨
    compileOnly 'org.projectlombok:lombok'

    // 런타임에만 필요
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

    // 테스트 코드에서만 사용
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
    testCompileOnly 'org.projectlombok:lombok'
}
````

## Maven vs Gradle 스코프 비교표

| Maven `<scope>` | Gradle 스코프               | 설명                         |
|-----------------|--------------------------|----------------------------|
| `compile`       | `implementation` / `api` | 기본 컴파일 의존성, `api`는 노출 필요 시 |
| `provided`      | `compileOnly`            | 컴파일에만 필요, 런타임 제외           |
| `runtime`       | `runtimeOnly`            | 런타임에만 필요                   |
| `test`          | `testImplementation`     | 테스트 코드용 의존성                |
| 없음 (기본)         | `implementation`         | 기본 의존성 스코프                 |

## 정리

- 일반 애플리케이션 개발은 `implementation`이 대부분 적합
- 라이브러리 개발 혹은 공용 모듈은 `api`를 사용해 하위 모듈에 노출
- `compileOnly`는 Lombok 같은 컴파일 시점 전용 라이브러리에 활용
- 런타임에만 필요한 드라이버, 네이티브 라이브러리는 `runtimeOnly`로 관리
- 테스트 관련 의존성은 `testImplementation`, `testRuntimeOnly`, `testCompileOnly`로 구분
