# Gradle의 Task 구조와 Maven Phase 차이

## Maven vs Gradle

| Maven Phase    | Gradle Task                                |
|----------------|--------------------------------------------|
| validate       | (별도 task 없음. 필요 시 custom validation 구현 가능) |
| compile        | `compileJava`, `classes`                   |
| test           | `test`, `jacoco*`, `check` (검증 task 집합)    |
| package        | `jar`, `bootJar`, `assemble`               |
| verify         | `check`, `jacoco*`                         |
| install/deploy | `install` task는 존재하지만 Maven과는 다소 다름        |
| clean          | `clean`                                    |
| build          | `build` (실제로는 `assemble` + `check` 조합 실행)  |

- Maven은 **순차적인 phase 중심**
- Gradle은 **Task 단위의 의존성 그래프 기반(DAG)** 으로 실행된다.
- Gradle은 Phase/Goal 개념이 없으며, **필요한 Task만 조합하여 실행**한다.

## Gradle의 build / verification 그룹

Gradle은 Task를 기능별로 그룹화한다. 이 그룹은 실행 순서와는 무관하며 단순한 분류 목적이다.

### build group

**아티팩트 생성 및 컴파일 관련 Task**

- `build`: 전체 빌드 과정을 실행하는 대표 Task (`assemble` + `check`)
- `assemble`: 결과물(jar/war 등)만 생성
- `classes`, `testClasses`, `jar`, `bootJar` 등 포함
- Spring Boot 플러그인을 적용하면 `bootJar`, `bootBuildImage` 등도 여기에 포함된다

### verification group

**테스트와 코드 검증 관련 Task**

- `test`: JUnit 테스트 실행 (Maven의 `test`와 유사)
- `check`: 모든 verification 관련 Task를 실행 (test, lint, static analysis 등 포함)
- `jacocoTestReport`, `jacocoTestCoverageVerification`: JaCoCo 코드 커버리지 측정 관련

## 대표 Task 실행 예시

각 Task의 역할은 IntelliJ Gradle 패널에서 실행해보며 확인하는 것이 가장 빠르다.

| 목적            | 명령어 또는 IntelliJ 실행 위치                              |
|---------------|----------------------------------------------------|
| 테스트만 실행       | `./gradlew test` or Gradle 패널의 verification > test |
| 전체 빌드(테스트 포함) | `./gradlew build` or Gradle 패널의 build > build      |
| 아티팩트만 생성      | `./gradlew assemble`                               |

## Maven → Gradle 전환 시 주의점

- **Maven은 phase/goal 중심**, Gradle은 **task 의존성 그래프 기반**
- Gradle에서 `build`는 phase가 아니라 단일 Task이며, `test`, `assemble` 등도 각각의 Task로 존재한다
- Gradle의 `build`는 `assemble`과 `check`에 의존하므로 **테스트까지 포함한 최종 아티팩트 생성** 역할을 한다
- Gradle의 group은 단지 Task 분류일 뿐, 실행 흐름이나 순서를 강제하지 않는다

## Spring Boot 사용 시 추가되는 Task

Spring Boot 플러그인을 적용하면 다음과 같은 특화 Task가 자동 생성된다:

- `bootJar`: 실행 가능한 Spring Boot JAR 생성
- `bootBuildImage`: Docker 이미지 생성
- `bootBuildInfo`: 빌드 메타 정보 생성
- 이들 Task는 필요 시 `build` Task에 포함되어 함께 실행된다

## 요약

- Gradle은 **task 중심의 빌드 시스템**이다
- `build` Task는 **테스트까지 수행한 최종 결과물 생성**의 의미이며 Maven의 `build` phase와 유사하다
- `assemble`은 테스트 없이 아티팩트만 생성
- `check`는 테스트 + 검증을 포함한 통합 Task
- `test`는 단독 테스트 실행 Task

## 핵심 비교

| 개념             | Maven                 | Gradle                                  |
|----------------|-----------------------|-----------------------------------------|
| 실행 구조          | phase/goal 기반 순차 실행   | task 의존성 그래프 기반 비순차 실행 (DAG)            |
| 빌드 대표 명령       | `mvn build`           | `./gradlew build` (assemble + check 포함) |
| 테스트만 실행        | `mvn test`            | `./gradlew test`                        |
| 결과물만 생성        | `mvn package`         | `./gradlew assemble`                    |
| 전체 검증 Task     | `mvn verify`          | `./gradlew check`                       |
| 실행 단위          | Goal (phase 흐름 내에 존재) | Task (독립적이며 조합 가능)                      |
| Spring Boot 지원 | 별도 설정 필요              | 플러그인 적용 시 다양한 boot\* Task 자동 생성         |

## 결론

- Gradle은 **유연하고 조합 가능한 Task 중심**의 빌드 도구다.
- 처음에는 Maven의 익숙한 phase 개념이 없어 헷갈릴 수 있지만,
- 구조를 이해하고 나면 **더 세밀하고 유연하게 빌드를 제어**할 수 있다.
- `build`, `test`, `assemble`, `check` 등 각 Task의 역할과 관계를 명확히 파악해두는 것이 핵심이다.
