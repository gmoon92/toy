# Spring bean profiler

`@SpringBootTest` 혹은 전체 `ApplicationContext`를 로드하는 테스트 시 **테스트 수행 시간이 과도하게 길어지는 문제가 반복됩니다.**

실제 실무에서는 여러 버전 브랜치(또는 피처 브랜치)가 병행적으로 개발되는 상황에서, 최종 병합(merge)된 브랜치의 테스트 코드가 느려지면 어떤 Bean/Component가 실제로 느리게 초기화되는지 직관적으로
파악할 수 있는 도구가 없어 병목 분석과 성능 개선에 어려움을 겪게 됩니다.

이 프로젝트는 테스트 수행 시 무거운 빈의 로드 현황을 추적하고, 위험을 사전에 알리며, 개발자에게 로컬 테스트 환경에서 좀 더 라이트(light)한 경험을 제공하는 것을 목적으로 합니다.

## 요구 사항

- Spring 내장 또는 최소한의 추가 라이브러리로 구현할 것
- 프로덕션 코드와 완전히 분리, 테스트(`src/test`)에서만 동작할 것
- 멀티 모듈 프로젝트 어디에서나 적용 가능할 것
- 테스트 실행 시점에 모든 Bean의 초기화(Init) 소요 시간을 자동, 반복적으로 측정할 수 있을 것
- 외부망, DB 등 무거운 빈도 빠르게 분석 가능할 것

## 주요 기능

- `BeanPostProcessor`를 활용해 모든 싱글톤 빈에 대한 초기화 시점을 후킹(hooking)
  - **Bean 이름별 초기화 시간(ms)을** 콘솔/로깅으로 출력
- 로그 분석만으로 주요 병목 빈을 신속 파악
- Spring Boot 2.4+ 사용 시,
    - `management.endpoints.web.exposure.include=startup` 설정을 통한 **actuator startup endpoint**와 병행 활용 가능

> Note:<br/>
> Lazy Initialization(지연초기화)은 현재 프로젝트 구조상(@SpringBootTest, Full Context 로드 기반 테스트) 실제 테스트 체감 효과에 한계가 있기 때문에 다루지 않음 (slice 테스트 적용 시 고려)

## Alternative Approaches

프로젝트 설계 시 아래와 같은 대안도 함께 검토했습니다.

1. Spring Lazy Initialization
    - 모든 빈을 필요 시점에만 생성하는 방식
    - 장점: slice 테스트에 효과적인 방식임
    - 단점: `@SpringBootTest`와 같이 Full Context 부팅 시에는 실질적 효과가 미미함.
2. Spring Boot Actuator Startup Endpoint
    - `Spring Boot 2.4+`에서 제공하는 앱 부팅 시점 엔드포인트 활용
    - 장점: 전체 앱 부트 타임라인을 직관적으로 분석 가능
    - 단점: 외부 보안 이슈, 추가 엔드포인트 오픈 관리 필요
3. JVM 프로파일링 도구(VisualVM, JFR 등)
    - 애플리케이션 구동 전체의 JVM 레벨 성능 분석에 유용
    - 장점: JVM 레벨에서 전체 부트 타임/스레드 병목 등 잡아낼 수 있음
    - 단점: 환경 세팅/분석 비용, CI 내 테스트 자동화와 직접 연동 어려움, 실용성 낮음

결론적으로, **BeanPostProcessor 기반 빈별 초기화 시간 측정**이

- 테스트 환경 전용
- 프로덕션 완전 분리
- 코드 한 줄로 원인 추적

등 실전/협업/운영분리 관점에서 가장 적합해 본 모듈에서 채택하였습니다.
