# Spring scheduling quartz

`Quartz`는 Java 및 J2SE 및 J2EE 환경에서 동작되도록 설계된 대표적인 **오픈소스 스케쥴러 프레임워크다.**

복잡한 일정 주기마다 수행하는 작업을 관리할 수 있을 뿐만 아니라, 간단하고 유연하며 확장 가능한 스케쥴러 기능을 제공한다.

## Interface keywords

`Quartz`는 애플리케이션의 런타임 환경 관리를 담당한다.

`Quartz`는 멀티 스레드 아키텍처를 기반으로 동작되며 확장성을 보장한다. 
Quartz 프레임워크가 시작되면 `Scheduler`가 `Job`을 실행하는 데 사용하는 작업자 스레드 세트를 초기화한다.

이 방법을 통해 `Quartz`의 `Scheduler`가 동시에 많은 작업을 실행할 수 있다. 
또한, 스레드 환경을 관리하기 위해 느슨하게 결합된 ThreadPool 관리 구성 요소 집합에 의존한다.

- Scheduler: 프레임워크의 스케줄러와 상호 작용하기 위한 기본 API
- Job: 실행되하고자 하는 컴포넌트를 구현하기 위한 인터페이스
- JobDetail: Job을 정의하는데 사용될 인터페이스
- Trigger: Job이 수행될 일정을 결정하는 컴포넌트
- JobBuilder: JobDetail 인스턴스를 생성하는데 사용된다. Job 인스턴스들을 정의할 수 있다.
- TriggerBuilder: Trigger 인스턴스를 생성하는데 사용된다.

## Quartz Scheduler

`Scheduler` 인터페이스는 Quartz의 메인 인터페이스다.

- Scheduler 는 JobDetail 및 Trigger 레지스트리를 유지 관리한다. 
- Scheduler에 등록되면 Scheduler 는 연결된 Trigger 실행될 때(예약된 시간에 도달할 때) Job 실행을 담당한다.

Scheduler 인스턴스는 `SchedulerFactory`에 의해 생성된다. 

- 이미 생성/초기화 된 스케줄러는 Scheduler를 생산한 동일한 `SchedulerFactory` 를 통해 찾아 사용할 수 있다. 
- Scheduler 가 생성된 후에는 "대기" 모드에 있으며 Job 하기 전에 start() 메서드가 호출한다.

Job 은 Job 인터페이스를 구현하는 클래스를 정의하고 JobDetail에 정의한다.

- 그런 다음 JobDetail 인스턴스는 Scheduler 등록한다.
  - scheduler.scheduleJob(JobDetail, Trigger)
  - scheduler.addJob(JobDetail, boolean)

## 참고

- [Spring integration - scheduling-quartz](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-quartz)
- [Baeldung - Introduction to Quartz](https://www.baeldung.com/quartz)
- [Baeldung - Scheduling in Spring with Quartz](https://www.baeldung.com/spring-quartz-schedule)
- [Quartz - GitHub](https://github.com/quartz-scheduler)
  - [Quartz jdbc job store - sql server](https://github.com/quartz-scheduler/quartz/blob/master/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_sqlServer.sql)
  - [Quartz jdbc job store - mysql](https://github.com/quartz-scheduler/quartz/blob/master/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_mysql.sql)
  - [Quartz - GitHub Best practices](https://github.com/quartz-scheduler/quartz/blob/master/docs/best-practices.adoc)
- [Quartz - Examples index](http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/examples/index.html)
  - [Quartz - tutorials](http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/tutorials/index.html)
  - [Quartz - Database Clustering](http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html#configuration-of-database-clustering-achieve-fail-over-and-load-balancing-with-jdbc-jobstore)
- ETC
  - [Quartz + Spring Batch 조합하기](https://kingbbode.tistory.com/38)
  - [Spring Boot Quartz Scheduler Example: Building an Email Scheduling app](https://www.callicoder.com/spring-boot-quartz-scheduler-email-scheduling-example/)
  - [Using Quartz for Scheduling With MongoDB](https://dzone.com/articles/using-quartz-for-scheduling-with-mongodb)
  - [https://github.com/jlinn/quartz-redis-jobstore](https://github.com/jlinn/quartz-redis-jobstore)
  - [Quartz-3 Multi WAS 환경을 위한 Cluster 환경의 Quartz Job Scheduler 구현](https://blog.advenoh.pe.kr/spring/Multi-WAS-%ED%99%98%EA%B2%BD%EC%9D%84-%EC%9C%84%ED%95%9C-Cluster-%ED%99%98%EA%B2%BD%EC%9D%98-Quartz-Job-Scheduler-%EA%B5%AC%ED%98%84/)
  - [Spring Boot using Quartz in mode Cluster](https://medium.com/javarevisited/spring-boot-using-quartz-in-mode-cluster-e1d71e4af4b9)
  - [Spring Boot - 스프링 부트 Quartz!](https://kouzie.github.io/spring/Spring-Boot-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8-Quartz/#%EC%BD%94%EB%93%9C)