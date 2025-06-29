### 로컬 & 젠킨스 ST 빌드시 구성은 다음과 같다.

```
src/
 ㄴ main/
     ㄴ resources/
         ㄴ application.properties   <-- spring.profiles.active=prod
         ㄴ application-prod.properties  
                 ㄴ-- [공통] 모든 기존 프로퍼티 포함
                e.g) spring.config.import=classpath:application-core.properties
 ㄴ test/
     ㄴ resources/
         ㄴ application.properties <-- main prod 참조
         ㄴ application-local.properties
         ㄴ application-st-biuld.properties <-- test local 참조
```

### 기본: test/resource/application.properties

```
spring.config.import=classpath:application-prod.properties
#---
spring.profiles.active=@test.spring.profiles.active@ # Maven filtering
```

### 로컬: test/resource/application-local.properties

```
spring.config.import=classpath:application-prod.properties
#---
.... # 테스트에서 사용할 오버라이딩 설정

```

### ST Build: test/resource/application-st-buld.properties

```
spring.config.import=classpath:application-local.properties
# ^-- 테스트를 위해 정의한 프로퍼티를 그대로 상속받기 위함
#---
# ST 서버 빌드시 외부 서버와 의존된 프로퍼티 비활성화
redis.enabled=0
amqp.enabled=0
firebase.enabled=0
dynamodb.enabled=0
```

### 마무리

- Spring Boot 2.4+부터 spring.config.import 사용 시 기본 로딩 체계가 바뀌므로 주의
- **테스트와 메인 리소스**
    - **test/resources와 main/resources 동명의 파일은 "병합"이 아님*- → test/resources가 main/resources를 완전히 덮어씀(무시)
    - 테스트 프로퍼티가 main 설정을 덮는 문제는 클래스패스 우선순위 때문에 발생
- **spring.config.import**
    - "import"에 명시한 설정만 불러올 수 있으나  
      정확히는, 기존 기본 경로는 "import" 사용법/위치에 따라 "병합"될 수도 있음
      (import 자체가 "자동 병합을 끄는 것" 은 아님)
- **spring.config.location**
    - spring.config.location은 외부 설정 파일 위치 지정용, 남용 시 병합 로직 꼬일 수 있음
    - 지정을 하면 "기본 로딩 경로" (= classpath:/application.properties 등)을 더 이상 자동 탐색하지 않으므로, 기존 설정을 완전히 대체함
- **설정 우선순위**
    - Spring Cloud Config는
        - 커맨드라인/시스템 프로퍼티/환경변수 → (외부 config 파일, classpath config) → Config Server 순
        - 즉, config server property는 일반적으로 로컬파일보다 **높은 우선순위**로 적용
- **실행 시 / 병합/덮어쓰기 구분부터 명확히 할 것**
- Config Server는 label을 통해 버전 및 환경별 설정을 깔끔하게 분리 관리 가능
- Config Server가 native backend이면 searchLocations 설정값 확인 필수
- 운영 환경에서 JVM 옵션을 잘 설정해야 config 병합이 제대로 이루어짐

## Reference

- https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config
