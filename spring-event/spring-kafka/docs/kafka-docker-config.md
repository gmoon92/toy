# [Kafka Docker Config - Environment Variables 사용 가이드](https://github.com/apache/kafka/blob/trunk/docker/examples/README.md#using-environment-variables)

# Environment

- Kafka 4.0
- KRaft

## KRaft 모드에서 환경 변수 사용 시 주의사항

KRaft 노드를 실행할 때 **환경 변수로 설정하는 경우**, 노드 실행에 필요한 **모든 필수 속성(properties)** 을 반드시 지정해야 한다.  
이러한 설정 방식은 주로 **Docker Compose** 환경에서 사용하는 것이 권장된다.

> **💡 Docker Compose 사용 권장 이유**  
> Compose를 사용하면 환경 변수 관리가 용이하고, KRaft 클러스터의 스케일 아웃, 서비스 간 의존성 관리에도 유리하다.

또한, **속성 파일과 환경 변수를 함께 사용하는 것도 가능**하다.  
이 경우, 공통 설정은 **속성 파일**에 두고, 특정 노드마다 다른 값은 **환경 변수**로 오버라이드하는 방식으로 운영할 수 있다.

### 1. 환경 변수 적용 우선순위

- 환경 변수로 지정한 속성은 **속성 파일에서 정의된 값을 덮어쓴다.**
- 환경 변수만 사용하는 경우, **모든 필수 속성을 빠짐없이 지정해야 한다.**
    - 기본 속성 파일과 병행 사용 시, **환경 변수에 지정된 값이 항상 우선 적용**된다.  
      (예: Docker 이미지 내 기본 설정보다 환경 변수 값이 먼저 적용됨)

> 필수 속성 목록은 [Apache Kafka - Broker Config](https://kafka.apache.org/documentation/#brokerconfigs) 에서 확인 가능.

### 2. 환경 변수 키 작성 규칙

환경 변수 키를 작성할 때는 다음 규칙을 따른다.

|    변환 대상    |         변환 방식         |
|:-----------:|:---------------------:|
|   `.` (점)   |    `_` (언더스코어)로 변환    |
| `_` (언더스코어) |  `__` (언더스코어 2개)로 변환  |
|  `-` (하이픈)  | `___` (언더스코어 3개)로 변환  |
|   접두사 추가    | 항상 `KAFKA_` 를 접두사로 붙임 |

#### example

|               원본 속성 키               |                   환경 변수 키                   |
|:-----------------------------------:|:-------------------------------------------:|
|               node.id               |               `KAFKA_NODE_ID`               |
|              log.dirs               |              `KAFKA_LOG_DIRS`               |
|            process.roles            |            `KAFKA_PROCESS_ROLES`            |
| controller.quorum.bootstrap.servers | `KAFKA_CONTROLLER_QUORUM_BOOTSTRAP_SERVERS` |

## [## 3. Broker Config](https://kafka.apache.org/documentation/#brokerconfigs)

다음 변수는 필수 값이다.

- node.id
- log.dirs
- process.roles
- controller.quorum.bootstrap.servers

Topic-level configurations and defaults are discussed in more detail below.

| name                                         | 필수 | type   | default | valid value         | importance | update mode | description                                                                                                                                                                                                                                                                                |
|----------------------------------------------|----|--------|---------|---------------------|------------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `node.id`                                    | O  | int    |         | [0,...]             | high       | read-only   | KRaft 모드에서는 클러스터 내에서 각 브로커를 구분하는 식별자<br/>`process.roles`가 broker,controller인 경우, 해당 역할을 수행할                                                                                                                                                                                                |
| `log.dirs`                                   | O  | string | null    |                     | high       | read-only   | 카프카 브로커가 로그 데이터를 저장할 디렉터리 경로                                                                                                                                                                                                                                                               |
| `process.roles`                              | O  | list   |         | [broker,controller] | high       | read-only   | 브로커의 역할<br/>- broker → 메시지를 저장하고, 프로듀서 & 컨슈머와 직접 통신하는 역할<br/>- controller → 클러스터 메타데이터를 관리하고, 브로커를 조율하는 역할                                                                                                                                                                                 |
| `controller.quorum.bootstrap.servers`        | O  | list   |""         | non-empty-list      | high       | read-only   | Kafka KRaft 모드(Kafka Raft)에서 사용되는 설정으로, 주키퍼 없이 카프카 자체적으로 클러스터를 관리할 때 필요하다.<br/>- 해당 설정은 클러스터 컨트롤러의 초기화에 필요한 서버 목록을 지정한다.<br/>- {host}:{port} 형식으로 쉼표( , )로 구분된 여러 개의 엔드포인트를 입력할 수 있다.                                                                                                                                                            |
|                                              |    |        |         |                     |            |             |
| `add.partitions.to.txn.retry.backoff.max.ms` |    | int    | 20      | [0,...]             | high       | read-only   | Kafka 트랜잭션에서 파티션을 추가할 때 허용되는 최대 타임아웃 값<br/>-검증 작업에는 적용되지 않고, 실제 추가 작업에만 적용<br/>- 이 값이 `request.timeout.ms`보다 크면 의미 없음 (즉, `request.timeout.ms` 값 이하로 설정해야 함)                                                                                                                               |
| `add.partitions.to.txn.retry.backoff.ms`     |    | int    | 20      | [1,...]             | high       | read-only   | 서버가 트랜잭션에 파티션을 추가하려고 시도할 때 적용되는 서버 측 재시도(backoff) 간격                                                                                                                                                                                                                                       |
| `advertised.listeners`|    | String | null    |                     | high       | read-only   | 브로커가 클라이언트 및 다른 브로커에게 자신의 접속 가능한 네트워크 주소를 알리는 설정<br/>- 클라우드(IaaS) 환경에서는 내부 IP와 외부 IP가 다를 수 있어서 클라이언트가 연결할 올바른 주소를 설정하는 데 유용<br/>- 0.0.0.0을 사용할 수 없음 (반드시 특정 IP 또는 도메인을 사용해야 함)<br/> - 동일한 포트를 여러 번 사용할 수 있으며, 로드밸런서를 고려한 설정이 가능<br/>- `advertised.listeners` 값이 없으면 `listeners` 값을 기본으로 사용 |

## Reference

- [Apache Kafka - Release Note](https://kafka.apache.org/downloads)
- [Apache Kafka - Broker Config](https://kafka.apache.org/documentation/#brokerconfigs)
- [Docker Hub - Apache Kafka](https://hub.docker.com/r/apache/kafka)
- [github Apache Kafka - Example](https://github.com/apache/kafka/blob/trunk/docker/examples/README.md)
- [github Apache Kafka - Using Environment Variables](https://github.com/apache/kafka/blob/trunk/docker/examples/README.md#using-environment-variables)
