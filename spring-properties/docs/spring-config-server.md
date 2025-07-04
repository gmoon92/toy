## Spring Cloud Config Server

- Git 저장소나 파일 시스템을 기반으로 여러 서비스를 위한 분산 설정을 중앙에서 관리할 수 있는 서버다.
- 클라이언트는 HTTP 요청을 통해 실시간으로 설정을 조회할 수 있다.
- Git 브랜치명이나 파일 시스템 디렉토리 구분을 위해 **`label`** 개념을 지원한다.

---

## `label`(라벨) 개념

- Config Server에서 버전, 환경, 배포 구분을 위해 **Git 브랜치명** 또는 **디렉토리명** 역할을 한다.
- 예시: `spring.cloud.config.label=v1.2.3`
- 파일 시스템(nativie backend)에서는 `label`이 설정 디렉토리 하위의 폴더명과 대응된다.

---

## Config Server native backend와 `searchLocations`

- Config Server가 Git 대신 로컬 파일시스템(native)을 backend로 사용하는 경우,
  `spring.cloud.config.server.native.searchLocations`로 탐색할 기본 파일 경로를 지정할 수 있다.
- `label` 값은 이 경로 아래의 하위 폴더명으로 사용된다.

```properties
spring.cloud.config.server.native.searchLocations=file:/opt/config-server/config-repo
```

`label=v1.2.3` → `/opt/config-server/config-repo/v1.2.3` 폴더 하위에서 설정을 조회

- 따로 지정하지 않으면 기본은 실행 디렉토리 하위의 `./config` 또는 `classpath:/config` 등이 된다.

---

## Config Server HTTP 요청 패턴

GET http://{config-server-host}:{port}/{application}/{profile}/{label}

- `{application}` : 서비스 이름(ex. `order-service`), 클라이언트의 `spring.application.name`과 일치
- `{profile}` : 활성 프로파일(`prod`, `dev` 등)
- `{label}` : Git 브랜치명 또는 디렉토리명

---

## Config Server 설정 병합과 우선순위

1. 클라이언트 자체 설정 파일 (ex. `application.properties`)
2. Config Server에서 내려받은 설정 (Git/파일 시스템)
3. 환경 변수, JVM 옵션, 커맨드라인 옵션

- 우선순위는 3 > 2 > 1 (숫자가 높을수록 우선)
- Config Server에서 내려받은 설정이 있으면 로컬 설정의 동일 키를 덮어쓴다

---

## 운영 환경 JVM 옵션 예시

```bash
-Dspring.config.import=optional:configserver:http://config.example.com:8888
-Dspring.cloud.config.label=release-202406
-Dspring.profiles.active=prod
-Dspring.application.name=myapp
```

- config.import로 Config Server 연동
- label로 버전 지정
- profiles.active로 프로파일 구분
- application.name으로 해당 서비스의 설정만 조회

### 설정 확인 방법

```bash
curl http://config.example.com:8888/myapp/prod/release-202406
```

- 설정 값을 JSON 형태로 받을 수 있다.
- 정보가 없을 경우 404나 에러 반환
- 배포 전후에 해당 명령어로 설정 정상 조회를 검증할 수 있다.

## Reference

- [Spring Cloud Config Server 공식문서](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
