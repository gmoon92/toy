# 특정 상황별 지침 (Specific Situation Guidelines)

스킬 개발의 특정 시나리오를 위한 실용적인 지침입니다.

## 문서

### [01-tool-usage.md](01-tool-usage.md)
**Claude의 도구 사용 동작 제어**

주제:
- 기본적으로 실행 vs 보수적 접근
- 병렬 도구 실행
- 답변 전 조사
- 과도한 트리거 방지

**읽어야 할 때:** Claude가 도구를 어떻게 사용할지 제어해야 할 때

---

### [02-format-control.md](02-format-control.md)
**응답 형식 및 구조 제어**

주제:
- 긍정적 지시문(무엇을 할지)
- XML 형식 표시자
- 프롬프트 스타일과 출력 일치
- 과도한 마크다운 방지
- 복잡한 출력 구조화

**읽어야 할 때:** 일관된 출력 형식이 필요할 때

---

### [03-avoid-overengineering.md](03-avoid-overengineering.md)
**스킬을 단순하고 집중적으로 유지**

주제:
- 불필요한 추상화 방지
- 범위 정의
- 과적합 없이 테스트 집중
- 하위 호환성 해킹 방지

**읽어야 할 때:** 범위 확장과 복잡성을 방지하고 싶을 때

---

### [04-code-exploration.md](04-code-exploration.md)
**철저한 코드 조사 장려**

주제:
- 탐색 요구사항
- 조사 워크플로우
- Grep을 사용한 검색 패턴
- 다중 파일 분석
- 코드 위치 인용

**읽어야 할 때:** 철저한 코드베이스 이해가 필요할 때

---

## 빠른 참조

### 도구 사용

```markdown
<default_to_action>
변경사항을 제안하는 대신 구현하세요.
</default_to_action>

<use_parallel_tool_calls>
독립적인 도구를 병렬로 실행하세요.
</use_parallel_tool_calls>

<investigate_before_answering>
주장하기 전에 코드를 읽으세요.
</investigate_before_answering>
```

### 형식 제어

```markdown
<section_name>
XML 태그로 출력을 구조화하세요.
</section_name>

무엇을 해야 하는지 말하세요, 무엇을 하지 말아야 하는지가 아니라.
프롬프트 스타일을 원하는 출력과 일치시키세요.
```

### 과잉 엔지니어링 방지

```markdown
<avoid_overengineering>
직접 요청된 변경사항만 수행하세요.
해결책을 단순하고 집중적으로 유지하세요.
</avoid_overengineering>
```

### 코드 탐색

```markdown
<explore_code_thoroughly>
변경사항을 제안하기 전에 모든 관련 파일을 읽으세요.
핵심 사실을 검색하는 데 철저하세요.
</explore_code_thoroughly>
```

---

## 지침 결합

효과적인 스킬은 종종 여러 지침을 결합합니다:

```markdown
## 예시: 완전한 단계 정의

<default_to_action>
자동으로 구현을 진행하세요.
</default_to_action>

<investigate_before_answering>
권장사항을 제기하기 전에 모든 파일을 읽으세요.
</investigate_before_answering>

<avoid_overengineering>
필요한 변경사항만 수행하세요.
</avoid_overengineering>

**출력 구조:**

<analysis>
[구조화된 발견사항]
</analysis>

<implementation>
[수행된 변경사항]
</implementation>
```

---

## 함께 보기

- [../principles/](../principles/) - 핵심 원칙
- [../reference/](../reference/) - 빠른 참조
