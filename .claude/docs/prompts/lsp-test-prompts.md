# LSP 테스트 프롬프트

Java 기반 프로젝트에서 LSP 도구 동작을 검증하기 위한 샘플 프롬프트 모음입니다.

각 프롬프트는 `grep` 으로 해결하기 어려운 시나리오를 대상으로 합니다.

---

## 1. 인터페이스 구현체 찾기 (Find Implementations)

`grep`은 인터페이스 선언과 텍스트만 찾지만, LSP는 실제 구현체(`CorsOriginRepositoryCustomImpl`)와 바인딩된 호출을 정확히 추적합니다.

```
CorsOriginRepositoryCustom 인터페이스의 모든 구현체를 찾아서,
각 구현 메서드가 어디서 호출되는지 추적해줘.
```

---

## 2. 메서드 오버로딩 + 호출 계층 추적 (Call Hierarchy)

동일 이름 메서드가 오버로딩된 경우 `grep`은 구분 불가, LSP는 타입 시그니처 기반으로 정확히 분리합니다.

```
UserService의 모든 메서드를 나열하고,
각 메서드가 어떤 컨트롤러/필터에서 호출되는지 호출 계층을 분석해줘.
```

---

## 3. 타입 계층 분석 (Type Hierarchy)

Repository 상속 체인(`JpaRepository` → `CrudRepository` → ...)을 LSP 심볼 해석으로 정확히 분석합니다.

```
CorsOriginRepository가 어떤 Spring Data 인터페이스를 상속받는지
타입 계층 전체를 추적하고, JPA 쿼리 메서드가 어떤 타입으로 동작하는지 설명해줘.
```

---

## 4. 참조 찾기 (Find References) — 변수 Shadowing 포함

`grep`으로는 스코프 구분 불가, LSP는 AST 기반 정확한 스코프 분석이 가능합니다.

```
JwtAuthenticationFilter에서 사용되는 변수들 중
같은 이름이 다른 스코프에서 재사용되는 케이스가 있는지 분석해줘.
```

---

## 5. 심볼 목록 + 타입 정보 (Document Symbol + Hover)

```
SecurityConfig 클래스의 모든 메서드와 그 반환 타입을 나열하고,
각 Bean이 어디에 주입되어 사용되는지 찾아줘.
```

---

## 참고

- LSP 플러그인 설치 필요: `jdtls-lsp@claude-plugins-official`
- 관련 문서: `ai/docs/claude-code-lsp.md`
- 1번(인터페이스 구현체 찾기)이 LSP 효과가 가장 명확하게 나타나므로 첫 번째로 시도 권장
