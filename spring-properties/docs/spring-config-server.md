## Spring Cloud Config Server

- Git 저장소나 파일시스템을 기반으로 분산 설정을 중앙에서 관리하는 서버.
- 클라이언트는 HTTP 요청으로 설정을 동적으로 조회 가능.
- Git 브랜치 또는 파일시스템 디렉토리 구분을 위해 **`label`*- 개념을 사용.

---

## `label` (라벨) 개념

- Config Server에서 버전이나 환경별 설정 구분을 위한 **Git 브랜치명*- 혹은 **디렉토리명*- 역할.
- 예: `spring.cloud.config.label=8.2.0.3`
- native (파일시스템) backend에서는 `label`이 `searchLocations` 하위 디렉토리로 매핑됨.

---

## Config Server native backend와 `searchLocations`

- Config Server가 Git 대신 로컬 파일시스템(native) backend 사용 시
  `spring.cloud.config.server.native.searchLocations`에 탐색할 기본 경로를 지정.
- `label` 값은 이 경로 하위의 하위 디렉토리명으로 동작함.

```properties
spring.cloud.config.server.native.searchLocations=file:/DATA/WEB/rv5x/config-server/config-repo
```

`label=8.2.0.3` → `/DATA/WEB/rv5x/config-server/config-repo/8.2.0.3` 하위 설정 조회

- 만약 설정하지 않으면 기본값은 실행 디렉토리 하위 `./config` 또는 `classpath:/config` 등.

---

## Config Server HTTP 요청 패턴

GET http://{config-server-host}:{port}/{application}/{profile}/{label}

- `{application}` : 서비스명 (클라이언트 `spring.application.name` 과 매칭)
- `{profile}` : 활성 프로파일 (`prod`, `dev` 등)
- `{label}` : Git 브랜치명 또는 디렉토리명

---

## Config Server 설정 병합 및 우선순위

1. 클라이언트 로컬 설정 (`application.properties` 등)
2. Config Server에서 받은 설정 (Git/파일시스템 기반)
3. 환경변수, JVM 옵션, 커맨드라인 인자

- 우선순위는 3 > 2 > 1 (나중에 오는 설정이 덮어씀)
- Config Server 설정은 로컬 설정의 키를 덮어쓰는 구조

## 운영 환경 JVM 옵션 예시

```bash
-Dspring.config.import=optional:configserver:http://172.0.0.0:9000
-Dspring.cloud.config.label=1.0.0
-Dspring.profiles.active=prod
-Dspring.application.name=gmoon
```

- spring.config.import로 Config Server 연동
- label로 배포 버전 구분
- profiles.active로 프로파일 구분
- application.name으로 조회 서비스명 지정

### 설정 확인 방법

```bash
curl http://172.0.0.0:9000/gmoon/prod/1.0.0
```

- JSON 형식으로 설정 정보 반환
- 잘못된 경우 404 또는 에러 발생
- 운영 환경에서 설정이 제대로 조회되는지 검증하는 데 사용 가능

## Reference

- https://docs.spring.io/spring-cloud-config/docs/current/reference/html/
