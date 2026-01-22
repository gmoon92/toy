# JMH(Java Microbenchmark Harness) 벤치마크

기능 개선의 효과를 객관적으로 평가하려면, 이를 입증할 수 있는 **성능 지표**가 필요하다.

예를 들어, 기존 기능에 비해 얼마나 성능이 향상되었는지, 혹은 JVM 환경에서 메모리 문제가 발생하지 않는지 등을 확인할 수 있어야 한다.

이러한 경우, 기능 도입의 정당성을 뒷받침할 수 있는 **신뢰할 만한 벤치마크 결과가 필요하다.**
이를 위해 다양한 프로파일링 도구가 존재하지만, **JVM 환경에서는 JMH(Java Microbenchmark Harness)가** 가장 널리 사용되는 벤치마크 프레임워크다.

## JMH란 무엇인가?

JMH는 JVM(Java Virtual Machine) 기반 프로그램(자바, 코틀린 등)에서 **메서드 단위의 성능을 정밀하게 측정**할 수 있도록 설계된 벤치마크 프레임워크다.

자바는 JIT(Just-In-Time) 컴파일, 메모리 관리 등 다양한 최적화 기법이 적용되기 때문에 코드 실행 결과에 편차가 발생할 수 있다. JMH는 이러한 특성을 고려해 **신뢰성 있는 성능 지표를 제공**할
수 있도록 설계되었다.

어노테이션 기반으로 작성되며, 실행 시간, 처리량(Throughput), 분산 등 다양한 성능 지표를 측정할 수 있다또한, **JIT 최적화 이후의 실제 성능을 측정**하기 위한 워밍업 기능도 기본적으로 제공한다.

Maven/Gradle 플러그인, 또는 자바 코드의 `main()` / `run()` 메서드를 통해 실행할 수 있다.

## 주요 JMH 어노테이션

JMH에서 제공하는 어노테이션은 벤치마크의 **정확성과 신뢰성**을 보장하기 위해 필수적으로 사용된다.

아래는 자주 사용하는 어노테이션과 그 역할을 간단히 정리한 표다.

| 어노테이션                                                                 | 설명                                                     |
|-----------------------------------------------------------------------|--------------------------------------------------------|
| [@Benchmark](#benchmark-내가-측정-대상이야)                                   | 벤치마크 대상 메서드임을 표시한다.                                    |
| [@BenchmarkMode](#benchmarkmode-어떤-성능-지표를-원해)                         | 평균 시간, 처리량(Throughput) 등 여러 지표를 동시에 측정할 수 있다.          |
| [@Warmup](#warmup-정확한-성능-측정을-위한-jvm-예열)                               | JIT 컴파일러가 최적화할 시간을 주기 위해, 예열 단계(예: 2초 × 3회)를 수행한다.     |
| [@Measurement](#measurement-실제-성능-측정-구간-정의)                           | 본 측정 단계. 예: 2초 동안 5회 반복 측정하여 총 10초간 데이터를 수집한다.         |
| [@Fork](#fork-벤치마크-환경을-초기화해서-신뢰도를-높이자)                                | 독립된 JVM 프로세스를 실행하여 외부 요인을 제거하고 신뢰도를 높인다. (1회 이상 설정 가능) |
| [@OutputTimeUnit](#outputtimeunit-ms-초-등-뭐로-결과-보여줄래)                  | 결과 출력 단위를 지정한다. 예: 밀리초(ms), 마이크로초(μs) 등                |
| [@State](#state-벤치마크-변수-라이프사이클)                                       | 벤치마크 클래스의 멤버 변수 공유 범위를 지정한다.                           |
| [@Param](#param-파라미터별로-어떤-값으로-실험해볼까)                                  | 다양한 값으로 반복 측정할 수 있도록 파라미터를 정의한다. 예: 파일 크기, 행 수 등       |
| [@Setup / @TearDown](#setup-teardown-벤치마크-반복실행-전에-할-일-끝난-뒤-정리할-일을-정의) | 벤치마크 실행 전후에 초기화 및 정리 작업을 수행한다.                         |

### @Benchmark: 내가 측정 대상이야

`@Benchmark`는 JMH에서 **성능 측정 대상 메서드**임을 나타내는 어노테이션이다.  
측정 대상은 주로 실행 시간이나 처리량(Throughput)과 같은 지표로 평가된다.

```java

@Benchmark
public void newFunction() {
	// TODO 성능 측정할 기능 구현 
}
```

이 어노테이션이 붙은 메서드는 JMH가 자동으로 여러 번 반복 실행하며,  
성능 측정 결과는 설정된 모드(`@BenchmarkMode`)와 단위(`@OutputTimeUnit`)에 따라 기록된다.

`@Benchmark`는 런타임 시점에 인식되며, 메서드에만 적용할 수 있도록 설계되어 있다.

```java

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Benchmark {
}
```

### @BenchmarkMode: 어떤 성능 지표를 원해?

`@BenchmarkMode`는 벤치마크를 어떤 방식으로 측정할지 정하는 어노테이션이다.

평균 시간, 처리량(Throughput) 등 여러 측정 모드를 선택할 수 있으며, 클래스나 메서드에 선언할 수 있다.Mode 배열 속성으로 원하는 측정 방식을 지정한다.

`Mode` 배열 속성으로 원하는 측정 방식을 지정한다.

```java

@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BenchmarkMode {
	Mode[] value();
}
```

아래는 주요 측정 모드와 간단한 설명이다.

| Mode           | 설명                                  | 예시                                              |
|----------------|-------------------------------------|-------------------------------------------------|
| Throughput     | 초당 실행 횟수, 처리량(Throughput)을 중시       | 초당 API 응답 수, DB 요청 수와 같은 초당 처리량 (TPS, QPS) 측정 등 |
| AverageTime    | 한 번 실행하는 데 걸린 평균 시간                 | 요청 1건당 평균 응답 시간                                 |
| SampleTime     | 실행 시간의 표본(sample)을 수집하여 분포나 분산을 분석  | 요청별 응답 시간 샘플링 -> 지연 시간 히스토그램 생성                 |
| SingleShotTime | 1회 실행 시간 측정, 주로 `cold start`상황에서 사용 | 람다 `cold start` 시간, 캐시 초기화 전 초기 DB 연결 시간        |
| All            | 위 모든 모드를 측정                         | 4가지 모든 모드를 한 번에 측정 (종합적인 성능 분석 목적)              |

> "콜드 스타트(`cold start`)"는 시스템이 처음 시작되거나 오랫동안 사용하지 않다가 다시 시작될 때 발생하는 초기화 및 준비 과정을 의미한다.

### @Warmup: 정확한 성능 측정을 위한 JVM 예열

`@Warmup`은 성능 측정을 시작하기 전, JVM이 충분히 "달궈질" 수 있도록 미리 코드를 반복 실행해주는 예열 설정이다.

JVM은 코드가 처음 실행될 때는 최적화가 덜 되어 있어 성능이 낮게 나오기 때문에, 실제 측정 전에 사전 준비가 꼭 필요하다.

#### 왜 워밍업이 필요할까?

Java는 처음엔 바이트코드를 인터프리트 방식으로 실행하다가,
자주 호출되는 코드(핫스팟)는 실행 도중에 JIT(Just-In-Time) 컴파일러가 감지해서 기계어로 변환하고 캐싱한다.

이 과정을 통해 시간이 지날수록 성능이 점점 개선되는데, 이 “점점 빨라지는” 구간을 측정에 포함하면 정확하지 않은 결과가 나온다.

> JIT 컴파일: JVM은 처음엔 코드를 천천히 해석하지만, 반복 호출되는 코드를 기계어로 바꿔 빠르게 실행할 수 있도록 최적화한다. 이 덕분에 "한 번 실행할 때보다, 여러 번 실행할수록 더 빨라지는" 성질이
> 생긴다.

#### 워밍업 결과는 측정값 통계에 포함되지 않는다

`@Warmup`으로 실행된 반복 횟수 동안의 결과는 공식적인 벤치마크 통계에 포함되지 않는다.

즉, JMH는 예열 단계와 본 측정 단계를 명확히 구분하며, 진짜 성능은 워밍업 이후부터 측정된다.

다음 예시는 총 3번, 매번 2초씩 워밍업을 수행하고, 그 후 본 측정을 시작하는 설정이다.

```java
@Warmup(iterations = 3, time = 2) // 2초씩 3번 반복
```

```java

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Warmup {
	int iterations() default -1;

	int time() default -1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	int batchSize() default -1;
}
```

### @Measurement: 실제 성능 측정 구간 정의

`@Measurement`는 `@Warmup` 이후, 벤치마크를 **얼마나 오래**, **몇 번 반복해서** 측정할지를 정의한다.

- `time`: 한 번 측정 구간의 지속 시간 (초, 밀리초 등 단위 지정 가능)
- `iterations`: 해당 측정 구간을 몇 번 반복할지 지정

#### 예시: 2초 x 5회 측정

```java

@Measurement(time = 2, iterations = 5) // 2초간 실행 x 5회 반복 측정
public class ExcelSAXBenchmark {

	@Benchmark
	public void readSax() throws IOException {
		ExcelUtil.readSAX(
			 Files.newInputStream(Paths.get(getFilePath())),
			 ctx,
			 ExcelUserVO.class,
			 list -> {/* noop */}
		);
	}
}
```

위 설정은 `@Benchmark` 메서드를 2초(`time`) 동안 가능한 한 여러 번 반복 호출하는 과정을 1회(`iteration`)로 간주하고, 이 과정을 다섯 번(`iterations=5`) 반복한다.
'

- 1회 반복당 2초간 측정 실행, 이 과정을 5번 반복(총 10초)
- 각 반복 종료 시마다 실행 횟수, 평균 소요시간 등 결과 데이터 산출
- 각 세트는 독립적인 측정값으로 기록되어, 마지막에 평균, 분산, 처리량(QPS) 등 지표들을 계산한다.

> 즉, "2초 동안 최대한 반복 실행"하는 과정을 5회 시행한다는 의미다. 실제 반복 실행 횟수는 대상 메서드의 처리 속도에 따라 수 천, 수 만 번도 될 수 있다.

#### Throughput(ops/sec) 계산 방식

JMH는 `@Measurement` 결과를 바탕으로 `Throughput` 모드에서 **ops/sec(작업 횟수/초)을** 자동 계산한다.

_공식: QPS or TPS = (각 반복에서 실행된 전체 횟수) / (반복당 측정 시간(초))_

예를 들어 1회 반복 2초(`time`) 동안 `@Benchmark` 메서드가 20,000번 호출됐다면

- QPS(Throughput) = 전체 실행 횟수 / 반복 측정 시간(초)
- QPS = 20,000 / 2초 = 10,000 ops/sec

세트에서 각각 다음과 같은 호출 횟수를 기록했다면

- `[20,000, 19,800, 20,500, 19,600, 20,100]` (각각 2초 동안)
- 각 세트의 처리량 = 호출 수 ÷ 2초
- 최종 QPS 평균 = (10,000 + 9,900 + 10,250 + 9,800 + 10,050) / 5번(`iterations`)
- QPS 평균 ≒ 10,000 ops/sec

> QPS (Queries Per Second): 초당 처리된 쿼리 수<br/>
> TPS (Transactions Per Second): 초당 처리된 트랜잭션 수<br/>
> **참고:** JMH의 ops/sec(Throughput)은 해당 `@Benchmark`가 "어떤 작업 단위"를 반복하는지"에 따라 QPS, TPS로 해석할 수 있다.

#### QPS/TPS 의미, 어떻게 해석해야 할까?

`@Benchmark` 메서드가 수행하는 **작업 단위**가 무엇인지에 따라, JMH의 `Throughput(ops/sec)` 수치를 QPS 또는 TPS로 해석할 수 있다.

- **QPS (Queries Per Second)**: 단순한 쿼리 실행 또는 작업 단위일 경우
- **TPS (Transactions Per Second)**: 여러 작업이 묶인 트랜잭션성 기능일 경우

> 핵심은 `@Benchmark` 메서드 **"한 번 호출이 의미하는 작업의 범위이다."** 즉, "이 함수 한 번 돌면 어떤 일이 벌어지는가?"를 기준으로 해석한다.

여기서 말하는 쿼리(Query), 트랜잭션(Transaction)은 DB의 기술 용어와 1:1로 일치하지 않는다. 벤치마크 대상의 논리적 단위를 의미하는 것이다.

예를 들어,

- 문자열 파싱, 리스트 정렬, JSON 직렬화처럼 하나의 경량 연산 작업이 대상이라면 ops/sec는 특별한 구분 없이 처리량으로 해석된다.
- `SELECT * FROM user`와 같은 단일 쿼리 실행이 벤치마킹 대상이면 QPS로 볼 수 있다.
- 여러 단계의 작업(예: 사용자 생성 → 이메일 발송 → 알림 저장)이 하나의 단위로 수행된다면, 이를 TPS로 해석하는 것이 적절하다.

요약하자면,

- 작업 단위가 작고 독립적이면 → QPS
- 여러 로직이 묶여 하나의 기능을 구성하면 → TPS
- 단순 반복 연산이면 → 구분 없이 `ops/sec(작업 횟수/초)`

> **Note:** JMH의 `Throughput`(ops/sec) 값은 **"1회 반복이 어떤 논리적 작업인가"**에 따라 QPS, TPS, 또는 단순 처리량(ops/sec)으로 해석됩니다. 실제 DB
> 트랜잭션/쿼리와 항상 일치하는 것은 아니므로, 해석할 때 반복 단위를 늘 명확히 인식해야 한다.

#### 예시로 보는 QPS / TPS 차이

```java

@Benchmark
public void runQuery() {
	// QPS 예시: 쿼리 1건 수행
	jdbcTemplate.queryForObject("SELECT ...");
}
// -> `Throughput: 5000 ops/sec`이라면 QPS = 5000

@Benchmark
public void processTransaction() {
	// TPS 예시: 트랜잭션 1건 처리
	userService.createUserAndSendEmail(); // 트랜잭션 하나
}
// -> `Throughput: 5000 ops/sec`이라면 TPS = 5000
```

### @Fork: 벤치마크 환경을 초기화해서 신뢰도를 높이자

JMH는 JVM의 내부 상태(GC, 캐시, JIT 등)로 인해 측정 결과가 왜곡되지 않도록, **아예 다른 JVM 프로세스를 새로 시작해서 벤치마크를 반복 실행한다.** 이를 위한 설정이 `@Fork`다.

- `value`: 측정용으로 띄울 JVM 프로세스 수
- `warmups`: 본격 측정 전, **워밍업(예열) 전용 Fork(JVM 프로세스) 수**
    - 즉, 각 `@Fork`마다 측정 전에 워밍업만 실행하는 '준비 단계'용 프로세스가 별도 N번 실행됨.
    - `@Warmup`과는 다름!!!..
- `jvmArgs`: 필요에 따라 JVM 인자를 직접 지정하거나, 특정 버전의 Java로 벤치마크를 수행할 수도 있다.

하나의 JVM 안에서만 반복 측정하면, 다음과 같은 문제가 발생할 수 있다:

- GC 발생 시점에 따라 처리 시간이 불규칙하게 측정됨
- 힙/스레드 풀/정적 필드 등 내부 상태가 반복 실행에 영향을 줌
- 첫 번째 실행과 마지막 실행 간의 JIT 최적화 정도 차이 등 편차 발생

따라서 `@Fork`를 사용하면, JVM 전체 상태를 리셋해서 "1회성 환경 요인"의 영향을 최소화할 수 있다. 이는 벤치마크 결과의 재현성과 정확성을 높이는 데 중요한 역할을 한다.

> "Fork 수만큼 JVM을 완전히 새로 실행해서 벤치마크를 반복 수행"<br/>
> JVM 프로세스 자체를 완전히 종료하고 새로 시작하므로, GC 상태, 클래스 로딩, JIT 캐시 등 모든 환경이 초기화된다.

```java

@Fork(value = 2, warmups = 2)
public class ExcelSAXBenchmark {

}
```

위 설정의 의미는 다음과 같다.

- `value = 2`: **서로 다른 JVM 프로세스에서 벤치마크를 총 2회 수행**
- `warmups = 2`: 벤치마크 측정 전에 JVM을 따로 2회 실행해 워밍업만 수행 (측정 결과에는 포함되지 않음)

JMH는 `value` 속성 값에 따라 JVM 프로세스 수를 결정한다.<br/>
**동시에 실행되지 않고**, 각 JVM 프로세스는 **순차적으로 JVM을 띄워서 각각 독립적으로 벤치마크를 수행하는 방식이다.**

즉, JVM 프로세스를 2번 새로 띄워서 다음 과정을 순차적으로 반복한다.

- JVM #1 (워밍업 전용)
    - @Warmup(`iterations = N`)만 반복 N 실행
    - @Measurement 없음 → 결과 집계 제외
    - JVM #1 종료
- JVM #2 (워밍업 전용)
    - 동일하게 @Warmup(`iterations = N`)만 반복 N 실행
    - @Measurement 없음 → 결과 집계 제외
    - JVM #2 종료
- JVM #3 (측정 1회차)
    - 워밍업(`@Warmup`) 반복 N회
    - 측정(`@Measurement`) 반복 M회
    - JVM #3 종료
- JVM #4  (측정 2회차)
    - 워밍업(`@Warmup`) 반복 N회
    - 측정(`@Measurement`) 반복 M회
    - JVM #4 종료

JMH는 이처럼 완전히 **독립된 환경에서 벤치마크를 수행한 뒤, 결과를 평균 내거나 합산해서 최종 처리량(ops/sec)을 계산한다.**

### @OutputTimeUnit: ms, 초 등 뭐로 결과 보여줄래?

JMH 벤치마크 결과(처리 시간 등)를 **어떤 시간 단위(ms, s, ns 등)로 출력할지 지정한다.**

```java
@OutputTimeUnit(TimeUnit.MILLISECONDS)
```

```java

@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OutputTimeUnit {
	TimeUnit value();
}
```

> 단지 결과 포맷을 바꾸는 역할이며, 측정 방식이나 내부 처리 시간에는 영향을 주지 않는다.

### @Param: 파라미터별로 어떤 값으로 실험해볼까?

실험 조건(파라미터)별로 벤치마크를 반복 실행하고 싶을 때 사용한다.

JMH는 지정된 **각 파라미터 값마다 벤치마크를 자동 반복 수행**하며 결과를 비교한다.

```java

@Param({"1", "100"})
public int dataSize;
```

위 설정은 `dataSize`가 1, 100일 때 각각 벤치마크를 수행하여 결과를 따로 출력한다. 이를 통해 다음과 같은 조건별 성능 비교가 가능하다.

- 입력 데이터 크기 변화에 따른 처리량 비교
- GC 시간 변화 분석
- 병목 지점 구간별 검증 등

> 하나의 코드로 여러 조건을 반복 실험할 수 있어, 실험 효율성과 재현성이 크게 향상된다.

### @State 벤치마크 변수 라이프사이클

`@State`는 벤치마크 수행 중 사용할 **객체 인스턴스의 생성 주기(Lifecycle)** 를 설정하는 어노테이션이다.

```java

@State(Scope.Benchmark)
public class ExcelSAXBenchmark {
	private ApplicationContext ctx;
	@Param
	public int excelDataRowSize;
}
```

위와 같이 `@State(Scope.Benchmark)`가 지정된 클래스는,<br/>
**"JMH 벤치마크 실행 1회 전체 동안 단 하나의 인스턴스" 로 공유된다.**

즉, 클래스의 필드/멤버변수는 **워밍업(`@Warmup`)과 측정(`@Measurement`) 단계에서 모두 공유**되며,<br/>
한 번 생성된 인스턴스를 여러 번 사용하는 구조다.

| Scope             | 설명                                                           |
|-------------------|--------------------------------------------------------------|
| `Scope.Benchmark` | **벤치마크 전체 실행 동안 단 하나의 인스턴스 생성**<br/>→ 대부분의 상황에서 사용           |
| `Scope.Thread`    | **벤치마크 스레드마다 별도 인스턴스 생성**<br/>→ 스레드 간 상태 공유 X, 멀티스레드 환경에서 안전 |
| `Scope.Group`     | **스레드 그룹 단위로 인스턴스 생성**<br/>→ 특정 그룹 단위 상태 격리에 활용              |

JMH는 내부적으로 벤치마크 클래스를 생성해서 매 측정마다 인스턴스를 주입한다. 이때 `@State`의 Scope 설정에 따라 다음이 달라진다.

- 측정값에 영향을 줄 수 있는 공유 상태 관리 가능 여부
- 인스턴스 재사용 여부
- 쓰레드 안전성, 상태 고립 여부

> `@State`는 단순히 "변수 생명주기"가 아니라, JMH가 벤치마크 인스턴스를 언제 만들고, 어디까지 공유할지를 결정하는 중요한 설정이다.

### @Setup/ @TearDown: 벤치마크 반복실행 전에 할 일, 끝난 뒤 정리할 일을 정의

`@Setup` 과 `@TearDown`은 벤치마크 클래스의 인스턴스 상태(State)를<br/>
**벤치마크 실행 전후에 준비(Setup)하고 해제(TearDown) 하는 데 사용된다.**

```java

@Setup(Level.Trial)
public void setUp() {
	// 벤치마크 전에 실행됨
}

@TearDown(Level.Trial)
public void tearDown() {
	// 벤치마크 끝나고 실행됨
}
```

#### Level: 실행 타이밍의 단위

```java

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Setup {
	Level value() default Level.Trial;
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TearDown {
	Level value() default Level.Trial;
}

```

| Level        | 의미                                                                   |
|--------------|----------------------------------------------------------------------|
| `Trial`      | **벤치마크 클래스 전체**의 실행 전/후 1회. 즉, `@Fork` 된 JVM 프로세스 시작과 종료마다 한 번씩 실행됨. |
| `Iteration`  | **각 반복 측정 단위** 전/후 실행. `@Measurement(iterations=5)`면 5번 각각 실행됨.      |
| `Invocation` | **각 `@Benchmark` 메서드 호출** 전/후 실행. 매우 자주 실행되므로 성능에 영향 줄 수 있음.         |

- `Iteration`이나 `Invocation`을 쓰면 매 측정마다 상태를 새로 초기화할 수 있음
- `Invocation`은 너무 자주 실행되므로, Heavy한 작업은 지양하는 것이 좋음

#### `@State`와 함께 쓰는 구조 예시

```java

@State(Scope.Benchmark)
public class MyBench {
	int counter;

	@Setup
	public void setUp() {
		counter = 0;
	}

	@TearDown
	public void tearDown() { /* 리소스 해제 등 */ }

	@Benchmark
	public void test() {
		counter++;
	}
}
```

- `@State(Scope.Benchmark)`이면, **벤치마크 1회 실행(JVM 1개)** 동안 이 클래스 인스턴스(즉, 멤버 변수들)는 단 하나만 생성된다.
- 따라서 `counter` 값은 여러 번의 워밍업 및 측정 반복 사이에서도 계속 유지된다.
- `@Setup`과 `@TearDown`은 보통 측정 전에 멤버 변수를 초기화하거나, 측정 후 리소스를 정리할 때 사용한다.

---

## 동작 순서와 동작 방식 설명

아래는 JMH의 반복, 워밍업, 포크 등 실행 흐름에 대한 이해를 돕는 설명이다.

```java

@Fork(
	 value = 1,
	 warmups = 1,
	 jvmArgs = {
		  "-Xmx1024m",
		  "-XX:+HeapDumpOnOutOfMemoryError",
		  "-XX:HeapDumpPath=./heapdump.hprof",
		  "-XX:+PrintGCDetails",
		  "-XX:+PrintGCTimeStamps"
	 }
)
@BenchmarkMode(Mode.All)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ExcelSAXBenchmark {
	private ApplicationContext ctx;

	@Param({
		 "1000",
		 "10000",
		 "100000",
		 "1000000"
	})
	public int excelDataRowSize;

	@Setup(Level.Iteration)
	public void setup() {
		if (ctx == null) {
			ctx = SpringApplication.run(
				 SpringPoiApplication.class,
				 "--server.port=9000"
			);
		}
	}

	@TearDown(Level.Iteration)
	public void tearDown() {
		if (ctx != null) {
			SpringApplication.exit(ctx);
			ctx = null;
		}
	}

	@Benchmark
	public void readSax() throws IOException {
		// TODO ...
	}

	@Benchmark
	public void readDom() throws IOException {
		// TODO ...
	}
}

```

### 1. `@Fork` JVM 프로세스 실행

1. `warmups = 1`: 이 JVM 실행 전에 별도의 워밍업용 JVM 1개가 먼저 실행
2. `value = 1`: 총 1개의 JVM 프로세스가 별도로 실행됨

### 2. `@Param`

위 예시는 excelDataRowSize 파라미터 값이 총 4개이므로 각 파라미터마다 벤치마크 전체를 반복 수행함 (즉, 4회 반복)

예를 들어 지정한 파라미터 `excelDataRowSize = 1000,10000,...` 별로 각각 독립 세트 벤치마크 실행

### 3. 각 파라미터값에 대해 다음 순서대로 진행

#### 3.1. 워밍업 단계 (`@Warmup`)

2초 × 3회 동안 측정대상 코드 반복 실행(JIT 등 환경 안정화)

_`@Warmup(time=2, iterations=3)`_

- 2초 동안 readSax(), readDom() 메서드를 각각 3번 반복 실행
- JIT 컴파일러 최적화 유도, 초기화된 GC 성능 확보 등 안정화 목적
- 이때 측정 결과는 통계에 포함되지 않음

#### 3.2. 측정 단계 (`@Measurement`)

2초 × 5회(총 10초) 동안 본 측정 및 결과 집계

_`@Measurement(iterations=5, time=2)`_

- 2초씩 5번 반복
- 각 반복에서 수행 순서:
- **반복마다**
    - `@Setup(Level.Iteration)`: (필요시) 인스턴스 초기화/Spring 올라감
    - `@Benchmark`: 성능 측정 대상 메서드 실행 (예: readSax, readDom)
    - `@TearDown(Level.Iteration)`: 인스턴스 정리 (Spring 내려감)
- 측정된 성능 수치는 이 단계의 결과만 통계로 집계됨

---

## JVM 힙 메모리 측정을 위한 JMH 설정 팁

JMH는 기본적으로 시간 기반(Throughput, AverageTime 등) 벤치마크에 최적화되어 있지만,
별도의 JVM 프로세스를 반복 실행하기 때문에, 실행 전후의 메모리 사용량 차이를 직접 측정할 수 있다.

```java

@State(Scope.Benchmark)
public class MemoryBenchmark {
	private long beforeUsedMem;
	private long afterUsedMem;

	@Setup(Level.Iteration)
	public void setup() {
		System.gc(); // GC 유도 (단, 명확하게 보장되진 않음)
		beforeUsedMem = getUsedMemory();
	}

	@Benchmark
	public void test() {
		// 측정 대상 로직
	}

	@TearDown(Level.Iteration)
	public void tearDown() {
		System.gc();
		afterUsedMem = getUsedMemory();

		long diff = afterUsedMem - beforeUsedMem;
		System.out.println("Used memory (bytes): " + diff);
	}

	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}
}
```

---

## 마무리

자바에서 무작정 "몇 번 실행해보겠다" 식으로 벤치마크를 하면, JVM 특성상 결과가 쉽게 오염되어 신뢰도가 떨어진다.

JMH는 워밍업(`@Warmup`), 측정(`@Measurement`) 단계뿐만 아니라, 다양한 환경 변수 설정, 멀티 파라미터(`@Param`) 반복 실험, 그리고 완전히 독립된 JVM 프로세스(`@Fork`)에서
다시 반복 측정함으로써, 환경 오염 없는 깨끗한 결과만 남기는 신뢰도 높은 성능 측정 구조를 제공한다. 필요에 따라 힙 메모리 사용량이나 GC 시간 등도 함께 기록할 수 있다.

- "코드 한 줄" 수준의 최적화, 알고리즘 튜닝, 성능 개선 포인트 발굴에 매우 효과적이다.
- 반면, 실제 서비스의 전체 응답 속도나 네트워크, DB 등 JVM 외부 의존성이 큰 구간은 별도의 시스템 벤치마크나 로깅, 관찰 지표를 함께 병행해 해석해야 한다.

결과적으로, 마이크로벤치마크(JMH)는 코드 내부 성능 문제를 과학적으로 파악하는 데 최고의 도구이며, 시스템 전체 성능 해석에는 별도의 관찰 지표와 병행하는 것이 필수적이다.

