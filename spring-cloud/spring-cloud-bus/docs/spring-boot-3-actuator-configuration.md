# Spring Boot 3.x Actuator 설정 변경사항

## 변경 이유

Spring Boot 3.x에서 `management.endpoint.{endpoint-name}.enabled` 설정이 더 이상 필요하지 않습니다.

### 제거된 설정

```yaml
# ❌ Spring Boot 3.x에서 불필요 (deprecated 경고)
management:
  endpoint:
    refresh:
      enabled: true
```

### Spring Boot 2.x vs 3.x 차이점

| 항목          | Spring Boot 2.x           | Spring Boot 3.x            |
|-------------|---------------------------|----------------------------|
| 엔드포인트 기본 상태 | 대부분 비활성화                  | **모두 활성화**                 |
| 설정 필요성      | `enabled: true` 필수        | 불필요 (기본값)                  |
| 제어 방식       | `endpoint.{name}.enabled` | `exposure.include/exclude` |

## 올바른 설정 방법

### 엔드포인트 노출 제어만 필요

```yaml
# ✅ Spring Boot 3.x 권장 설정
management:
  endpoints:
    web:
      exposure:
        # 필요한 엔드포인트만 노출
        include: refresh, health, info, busrefresh, busenv
```

### 특정 엔드포인트 비활성화 (필요한 경우만)

```yaml
# 특정 엔드포인트를 완전히 비활성화하려는 경우만 사용
management:
  endpoint:
    shutdown:
      enabled: false  # shutdown 엔드포인트는 보안상 명시적으로 비활성화 권장
```

## 핵심 개념

### 1. Enabled vs Exposed

```
┌─────────────────────────────────────────┐
│ Actuator 엔드포인트 상태                  │
├─────────────────────────────────────────┤
│                                         │
│  [Enabled]           [Exposed]          │
│  엔드포인트 활성화     → 외부 노출         │
│  (기본: true)         (기본: false)      │
│                                         │
│  ↓                    ↓                 │
│  스프링 컨텍스트에      HTTP/JMX를 통해    │
│  빈으로 등록됨         외부 접근 가능      │
│                                         │
└─────────────────────────────────────────┘
```

### 2. 동작 흐름

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh  # ← 노출 제어만 하면 됨
```

```
요청: POST /actuator/refresh
  ↓
Spring Boot가 'refresh' 엔드포인트 확인
  ↓
1. Enabled? → ✅ (기본적으로 true)
2. Exposed? → ✅ (exposure.include에 포함됨)
  ↓
엔드포인트 실행
```

## Spring Boot 3.x에서 기본 활성화된 주요 엔드포인트

| 엔드포인트        | 설명        | 노출 필요         | 보안 고려사항        |
|--------------|-----------|---------------|----------------|
| `health`     | 헬스 체크     | ✅ 권장          | 공개 가능          |
| `info`       | 애플리케이션 정보 | ✅ 권장          | 공개 가능          |
| `refresh`    | 설정 갱신     | ✅ Config 사용 시 | 🔒 인증 필요       |
| `busrefresh` | Bus 설정 갱신 | ✅ Bus 사용 시    | 🔒 인증 필요       |
| `env`        | 환경 변수 조회  | ⚠️ 개발 환경만     | 🔒 민감 정보 포함    |
| `metrics`    | 메트릭 조회    | ⚠️ 모니터링 시     | 성능 데이터 노출      |
| `shutdown`   | 종료        | ❌ 권장하지 않음     | ⛔ 위험 (기본 비활성화) |

## 프로젝트 적용 예시

### Config Server (spring-cloud-bus-server)

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - busrefresh  # 모든 클라이언트 설정 갱신
          - busenv      # 환경 변수 동적 업데이트
          - info        # 서버 정보
          - health      # 헬스 체크
          - refresh     # 개별 서버 갱신
  endpoint:
    health:
      show-details: always  # 헬스 체크 상세 정보 표시
```

### Config Client (spring-cloud-bus-client)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh  # 수동 인스턴스 갱신
```

## 보안 설정 권장사항

### 프로덕션 환경

```yaml
management:
  endpoints:
    web:
      # 기본 경로 변경으로 보안 강화
      base-path: /internal/actuator
      exposure:
        include: health, info  # 최소한의 엔드포인트만 노출

  endpoint:
    health:
      show-details: when-authorized  # 인증된 경우만 상세 정보 표시
```

### Spring Security 통합

```java

@Configuration
public class ActuatorSecurityConfig {

	@Bean
	public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
		return http
		  .securityMatcher(EndpointRequest.toAnyEndpoint())
		  .authorizeHttpRequests(authorize -> authorize
			.requestMatchers(EndpointRequest.to("health", "info")).permitAll()
			.requestMatchers(EndpointRequest.to("refresh", "busrefresh")).hasRole("ADMIN")
			.anyRequest().authenticated()
		  )
		  .build();
	}
}
```

## 마이그레이션 체크리스트

Spring Boot 2.x → 3.x 전환 시:

- [ ] `management.endpoint.{name}.enabled: true` 설정 제거
- [ ] `exposure.include` 설정만 유지
- [ ] 보안이 필요한 엔드포인트 확인 (refresh, busrefresh 등)
- [ ] `shutdown` 엔드포인트 명시적 비활성화 (`enabled: false`)
- [ ] 프로덕션 환경에서 `base-path` 변경 고려
- [ ] 헬스 체크 상세 정보 노출 수준 재검토

## 참고 자료

- [Spring Boot Actuator Endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Spring Cloud Bus Actuator](https://docs.spring.io/spring-cloud-bus/reference/actuator.html)
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

## 정리

**Spring Boot 3.x에서는**:

1. 모든 엔드포인트가 기본적으로 활성화됨
2. `management.endpoint.{name}.enabled` 설정 불필요
3. **노출 제어**만 `management.endpoints.web.exposure.include`로 관리
4. 보안이 필요한 엔드포인트는 Spring Security로 보호

**기억할 것**:

- Enabled (활성화) ≠ Exposed (노출)
- Spring Boot 3.x는 "활성화는 기본, 노출은 명시적"
- 불필요한 `enabled: true` 설정은 제거하여 설정 파일 단순화
