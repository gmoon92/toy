# Configuring and Running a Job

## TOC

- [Configuring a Job](#configuring-a-job)

# Configuring a Job

`Job` 인터페이스의 다양한 구현체들이 존재하며, 이러한 구현체들은 제공된 빌더(Java 설정) 또는 XML 네임스페이스(XML 기반 설정) 뒤에 추상화되어 있다.

```java
@Bean
public Job footballJob(JobRepository jobRepository) {
    return new JobBuilder("footballJob", jobRepository)
                     .start(playerLoad()) // 첫 번째 Step
                     .next(gameLoad())    // 두 번째 Step
                     .next(playerSummarization()) // 세 번째 Step
                     .build();
}
```

이 예시는 3개의 `Step` 인스턴스로 구성된 `Job`을 보여준다. `Job` 관련 빌더는 병렬 처리(Split), 선언적 흐름 제어(Decision), 또는 외부 흐름 정의(Flow)를 지원하는 다양한 요소들을 포함할 수 있다.

> 모든 `Job`과 `Step`은 `JobRepository`를 필요로 하며, `JobRepository`는 [Java Configuration](#https://docs.spring.io/spring-batch/reference/job/java-config.html)을 통해 설정된다.

## Restartability
### Job 재시작 가능성

배치 작업을 실행할 때 중요한 문제 중 하나는 `Job`이 재시작될 때의 동작이다.

특정 `JobInstance`에 대해 이미 `JobExecution`이 존재하면, 그 `Job`을 실행하는 것은 "재시작"으로 간주된다.

모든 작업이 중단된 지점부터 재시작할 수 있어야 하지만, 특정 시나리오에서는 불가능할 수 있다. 이 경우, 새로운 `JobInstance`를 생성해야 한다. 스프링 배치에서는 이러한 상황을 돕기 위한 설정이 제공된다. 만약 `Job`이 절대 재시작되지 않아야 한다면, `restartable` 속성을 `false`로 설정할 수 있다.

```java
// Java 설정에서 재시작 불가 예시
@Bean
public Job footballJob(JobRepository jobRepository) {
    return new JobBuilder("footballJob", jobRepository)
                     .preventRestart() // 재시작 불가 설정
                     .build();
}
```

preventRestart()를 설정하면 작업이 다시 시작되지 않으며, 재시작 시도 시 `JobRestartException`이 발생한다.


```java
Job job = new SimpleJob();
job.setRestartable(false);

JobParameters jobParameters = new JobParameters();

JobExecution firstExecution = jobRepository.createJobExecution(job, jobParameters);
jobRepository.saveOrUpdate(firstExecution);

try {
    jobRepository.createJobExecution(job, jobParameters);
    fail();
} catch (JobRestartException e) {
    // expected
}
```

- **재시작 불가능 설정**: 만약 재시작이 불가능한 작업으로 설정하려면 `restartable` 속성을 `false`로 설정한다.
- 재시작 불가로 설정된 작업을 재실행하면 `JobRestartException`이 발생한다.


## JobExecutionListener
### Job 실행 중 이벤트 처리

`JobExecutionListener`는 `Job` 실행 중 발생하는 다양한 이벤트에 대해 알림을 받을 수 있는 기능을 제공한다. `SimpleJob`은 적절한 시점에 `JobListener`를 호출하여 이 기능을 구현한다.

```java
public interface JobExecutionListener {
    void beforeJob(JobExecution jobExecution);
    void afterJob(JobExecution jobExecution);
}
```

```java
@Bean
public Job footballJob(JobRepository jobRepository) {
    return new JobBuilder("footballJob", jobRepository)
                     .listener(sampleListener()) // Listener 설정
                     .build();
}
```

- **afterJob 메서드**는 성공 여부와 상관없이 호출된다. 성공 여부는 `JobExecution`의 상태에서 확인할 수 있다.

---

## **상속을 통한 Job 구성**

여러 `Job`이 비슷한 구성을 공유하는 경우, 부모 `Job`을 정의하고 자식 `Job`이 이를 상속받아 사용할 수 있다. 자식 `Job`은 부모 `Job`의 속성을 상속받아 이를 결합할 수 있다.

### **XML 설정에서 부모-자식 Job 상속 예시**
```xml
<job id="baseJob" abstract="true">
    <listeners>
        <listener ref="listenerOne"/>
    <listeners>
</job>

<job id="job1" parent="baseJob">
    <step id="step1" parent="standaloneStep"/>
    <listeners merge="true">
        <listener ref="listenerTwo"/>
    <listeners>
</job>
```

## JobParametersValidator

`Job`은 필수 파라미터를 포함한 유효성 검사를 위해 `JobParametersValidator`를 선언할 수 있다. 기본 제공되는 `DefaultJobParametersValidator`를 사용하거나 더 복잡한 제약 조건이 필요하면 직접 구현할 수 있다.

```java
@Bean
public Job job1(JobRepository jobRepository) {
    return new JobBuilder("job1", jobRepository)
                     .validator(parametersValidator()) // Validator 설정
                     .build();
}
```

---

이와 같이 `Job` 구성, 재시작 가능성, 이벤트 처리, 상속을 통한 구성, 그리고 파라미터 유효성 검사를 포함한 다양한 설정을 통해 배치 작업을 관리할 수 있다.

## Reference

- https://docs.spring.io/spring-batch/reference/job.html
- https://docs.spring.io/spring-batch/reference/job/configuring.html
