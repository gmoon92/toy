# Spring Filter vs Interceptor vs AOP

## 구현 방식 선택 (Filter, Interceptor, AOP)

클라이언트 요청 정보를 로깅하기 위해서는 Spring의 **Filter**, **Interceptor**, **AOP**를 활용할 수 있다.

- **Filter** → 서블릿 수준에서 요청/응답을 전역적으로 가로챔.
- **Interceptor** → Spring MVC 레벨에서 컨트롤러 실행 전후를 가로챔.
- **AOP** → 컨트롤러뿐만 아니라 서비스, 리포지토리 등 비즈니스 로직 계층에도 개입 가능.

### 사용 목적별 적합한 기술

| 사용 목적 | 적합한 기술 | 예시 |
|-----------|-----------|------|
| 전역 요청 처리 | `Filter` | CORS, 보안 필터, 로깅, 인코딩 처리 |
| 인증/권한 체크 | `Interceptor` | JWT 토큰 검증, 세션 체크 |
| 비즈니스 로직 전후 처리 | `AOP` | 트랜잭션 관리, 로깅, 성능 모니터링, 메서드 실행 시간 측정 |
| 컨트롤러 레이어 전후 처리 | `Interceptor` | API 요청 로깅, 사용자 역할 기반 접근 제한 |
| 특정 레이어 (서비스, DAO) 개입 | `AOP` | 서비스 레이어 트랜잭션 처리, 실행 시간 측정, 공통 예외 처리 |

### 비교표

| 구분       | Filter | Interceptor | AOP |
|----------|--------|-------------|-----|
| 실행 시점 | 가장 먼저 실행 (Servlet 수준) | DispatcherServlet 이후 실행 (HandlerAdapter 전후) | 메서드 실행 전후 (비즈니스 로직 포함) |
| Spring Context 접근 | ❌ 불가능 (직접 주입 어려움) | ✅ 가능 (Spring Bean 접근 가능) | ✅ 가능 (Bean, 트랜잭션 활용 가능) |
| 컨트롤러 정보 접근 | ❌ 불가능 | ✅ 가능 | ✅ 가능 (메서드 호출 단위) |
| 세밀한 제어 | ❌ 요청/응답 전체 대상으로 처리 | ✅ 컨트롤러 전후 처리 가능 | ✅ 특정 메서드, 클래스, 애노테이션 기준으로 적용 가능 |
| 비즈니스 로직 개입 | ❌ 불가능 | ❌ 불가능 (핸들러 레이어만 가능) | ✅ 가능 (컨트롤러, 서비스, 리포지토리 등 모든 레이어 개입 가능) |
| 비동기 지원 | ✅ 가능 | ❌ 기본적으로 불가능 | ✅ 가능 |
| 적합한 용도 | 인코딩, 보안, CORS 등 전역 처리 | 인증, 권한 체크, 로깅 | 트랜잭션, 로깅, 공통 로직 |
| 구현 방식 | `javax.servlet.Filter` 인터페이스 구현 | `HandlerInterceptor` 인터페이스 구현 | `@Aspect`와 `@Around`, `@Before`, `@After` 등 활용 |
| 설정 방식 | `@Component` + `@WebFilter` 또는 `FilterRegistrationBean` 사용 | `@Component` + `WebMvcConfigurer`에서 `addInterceptors`로 등록 | `@Aspect` + `@Component` + `@EnableAspectJAutoProxy` 설정 |
| 메서드 | `doFilter()` | `preHandle()`, `postHandle()`, `afterCompletion()` | `@Around`, `@Before`, `@AfterReturning`, `@AfterThrowing` |
| 동작 원리 | 서블릿 요청을 가로채서 체인 형태로 연결 | Spring MVC에서 요청을 가로채고 핸들러 실행 전후에 개입 | 프록시 기반으로 메서드 실행을 감싸서 실행 |

