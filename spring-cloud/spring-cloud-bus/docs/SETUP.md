# Spring Cloud Bus Setup Guide

이 가이드는 Spring Cloud Bus를 사용한 분산 설정 갱신 시스템을 설정하고 테스트하는 방법을 설명합니다.

## Prerequisites

1. **Docker** - RabbitMQ 실행용
2. **JDK 17+** - Spring Boot 애플리케이션 실행용
3. **Git** - Config Server의 설정 저장소용

## Step 1: RabbitMQ 시작

Spring Cloud Bus는 메시지 브로커로 RabbitMQ를 사용합니다.

```bash
# RabbitMQ 컨테이너 시작
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# RabbitMQ Management UI: http://localhost:15672
# Username: guest
# Password: guest
```

## Step 2: Config Server용 Git 저장소 생성

Config Server는 Git 저장소에서 설정 파일을 가져옵니다.

```bash
# 로컬 Git 저장소 생성
mkdir -p ~/config-repo
cd ~/config-repo
git init

# Config Client용 설정 파일 생성
cat > config-client.yml << 'EOF'
app:
  config:
    message: "Hello from Config Server!"
    refresh-count: 0
    feature-enabled: false
EOF

# 변경사항 커밋
git add .
git commit -m "Initial configuration"
```

## Step 3: Config Server 시작

```bash
# 프로젝트 루트에서 실행
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-server:bootRun

# 실행 확인
curl http://localhost:8888/actuator/health
```

## Step 4: Config Client 인스턴스들 시작

여러 인스턴스를 실행하여 Bus의 브로드캐스트 기능을 테스트합니다.

```bash
# Terminal 1: Client Instance 1 (port 8080)
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun

# Terminal 2: Client Instance 2 (port 8081)
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun \
  --args='--server.port=8081'

# Terminal 3: Client Instance 3 (port 8082)
./gradlew :spring-cloud:spring-cloud-bus:spring-cloud-bus-client:bootRun \
  --args='--server.port=8082'
```

## Step 5: 현재 설정 확인

각 인스턴스의 현재 설정을 확인합니다.

```bash
# Instance 1
curl http://localhost:8080/api/config

# Instance 2
curl http://localhost:8081/api/config

# Instance 3
curl http://localhost:8082/api/config
```

## Step 6: 설정 업데이트 및 Refresh

### 6.1 설정 파일 업데이트

```bash
# Config Server의 Git 저장소에서 설정 변경
cat > ~/config-repo/config-client.yml << 'EOF'
app:
  config:
    message: "Updated via Bus Refresh!"
    refresh-count: 1
    feature-enabled: true
EOF

# 변경사항 커밋
cd ~/config-repo
git add .
git commit -m "Update configuration"
```

### 6.2 Bus Refresh 트리거

**중요:** `/actuator/busrefresh`는 Config Server에서 호출해야 합니다!

```bash
# Config Server에 busrefresh 호출 (단 한 번의 호출로 모든 인스턴스 갱신!)
curl -X POST http://localhost:8888/actuator/busrefresh
```

### 6.3 갱신된 설정 확인

```bash
# 모든 인스턴스가 새로운 설정을 가져왔는지 확인
curl http://localhost:8080/api/config
curl http://localhost:8081/api/config
curl http://localhost:8082/api/config

# 모두 동일한 갱신된 설정을 반환해야 함!
```

## Step 7: 커스텀 이벤트 테스트

Spring Cloud Bus를 통해 커스텀 이벤트를 브로드캐스트할 수 있습니다.

### 7.1 UserLoginEvent 발행

```bash
# Config Server에서 커스텀 이벤트 발행
curl -X POST http://localhost:8888/api/events/user-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "ipAddress": "192.168.1.100"
  }'
```

### 7.2 모든 인스턴스에서 이벤트 확인

```bash
# 각 인스턴스의 로그인 히스토리 확인
curl http://localhost:8080/api/login-history
curl http://localhost:8081/api/login-history
curl http://localhost:8082/api/login-history

# 모든 인스턴스가 동일한 로그인 히스토리를 가짐!
```

### 7.3 추가 이벤트 발행

```bash
# 다른 사용자 로그인 이벤트
curl -X POST http://localhost:8888/api/events/user-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "ipAddress": "192.168.1.200"
  }'

# 로그인 통계 확인
curl http://localhost:8080/api/login-stats
```

## Troubleshooting

### RabbitMQ 연결 실패

```bash
# RabbitMQ 상태 확인
docker ps | grep rabbitmq
docker logs rabbitmq

# RabbitMQ 재시작
docker restart rabbitmq
```

### Config Server 연결 실패

```bash
# Config Server가 실행 중인지 확인
curl http://localhost:8888/actuator/health

# Config Server가 설정을 제공하는지 확인
curl http://localhost:8888/config-client/default
```

### Bus Refresh가 작동하지 않음

1. **RabbitMQ 연결 확인**: 모든 애플리케이션이 RabbitMQ에 연결되어 있는지 확인
2. **Bus ID 확인**: 각 인스턴스가 고유한 Bus ID를 가지는지 확인
3. **Actuator 엔드포인트 확인**: busrefresh 엔드포인트가 노출되어 있는지 확인

```bash
# Config Server의 노출된 엔드포인트 확인
curl http://localhost:8888/actuator | jq '.["_links"]'

# Client의 노출된 엔드포인트 확인
curl http://localhost:8080/actuator | jq '.["_links"]'
```

### 로그 레벨 증가

문제 해결을 위해 로그 레벨을 DEBUG로 변경:

```yaml
# application.yml
logging:
  level:
    org.springframework.cloud.bus: DEBUG
    org.springframework.cloud.config: DEBUG
    org.springframework.amqp: DEBUG
```

## Advanced Topics

### Addressing Specific Instances

특정 인스턴스만 갱신:

```bash
# config-client:8080 인스턴스만 갱신
curl -X POST http://localhost:8888/actuator/busrefresh/config-client:8080:**
```

### Bus Env Endpoint

런타임에 환경 변수 업데이트:

```bash
# 모든 인스턴스의 로그 레벨을 DEBUG로 변경
curl -X POST http://localhost:8888/actuator/busenv \
  -H "Content-Type: application/json" \
  -d '{
    "name": "logging.level.root",
    "value": "DEBUG"
  }'

# 변경사항 적용 (refresh 필요)
curl -X POST http://localhost:8888/actuator/busrefresh
```

## Production Considerations

### Security

1. **Actuator 엔드포인트 보호**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,busrefresh
  endpoint:
    busrefresh:
      enabled: true

spring:
  security:
    user:
      name: admin
      password: ${ACTUATOR_PASSWORD}
```

2. **RabbitMQ 인증**
```yaml
spring:
  rabbitmq:
    host: rabbitmq.production.com
    port: 5672
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl:
      enabled: true
```

### High Availability

1. **RabbitMQ 클러스터 구성**
2. **Config Server 다중 인스턴스**
3. **Health Check 및 Monitoring 설정**

### Monitoring

```yaml
# Micrometer를 사용한 메트릭 수집
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

## References

- [Spring Cloud Bus Documentation](https://docs.spring.io/spring-cloud-bus/reference/)
- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/reference/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
