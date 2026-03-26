# 프롬프팅 모범 사례 (Prompting Best Practices)

Claude 4.x 프롬프트 작성을 위한 핵심 원칙과 지침입니다.

## 목적

Claude와 효과적으로 소통하기 위한 프롬프트 작성 규칙입니다. 시스템 프롬프트, 대화, 지시문에 적용됩니다.

## 내용

### [principles/](principles/)
**핵심 프롬프팅 원칙**

- [01-general-principles.md](principles/01-general-principles.md) - 명확한 지시문, 컨텍스트, 예시
- [02-long-term-reasoning.md](principles/02-long-term-reasoning.md) - 상태 추적, 컨텍스트 관리
- [03-communication-style.md](principles/03-communication-style.md) - 작성 스타일 가이드
- [README.md](principles/README.md) - 원칙 개요

### [guidelines/](guidelines/)
**특정 상황별 지침**

- [01-tool-usage.md](guidelines/01-tool-usage.md) - 도구 사용 패턴
- [02-format-control.md](guidelines/02-format-control.md) - 응답 형식 제어
- [03-avoid-overengineering.md](guidelines/03-avoid-overengineering.md) - 과잉 엔지니어링 방지
- [04-code-exploration.md](guidelines/04-code-exploration.md) - 코드 탐색 가이드
- [README.md](guidelines/README.md) - 지침 개요

### [reference/](reference/)
**빠른 참조**

- [strong-directives.md](reference/strong-directives.md) - 강력한 지시문 키워드
- [quick-patterns.md](reference/quick-patterns.md) - 일반적인 패턴

---

## 사용 시기

### 일반 프롬프팅

프롬프트를 작성할 때 기본 원칙 적용

**로드:** [principles/README.md](principles/README.md)

### 특정 시나리오

특정 상황에 대한 안내가 필요할 때

**로드:** [guidelines/README.md](guidelines/README.md)

### 빠른 참조

빠른 참조나 복사-붙여넣기 패턴이 필요할 때

**로드:** [reference/](reference/)

---

## 핵심 원칙

1. **명확성** - 명확하고 구체적인 지시문
2. **컨텍스트 추가** - 왜(why)인지 설명
3. **예시 보여주기** - 완전하고 현실적인 예시
4. **강력한 지시문 사용** - 필수, 스크립트 실행, 하지 마세요
5. **출력 구조화** - XML 태그로 명확한 경계
6. **기본적으로 실행** - 제안보다 실행
7. **먼저 조사** - 추측 대신 검증

---

## 관련 문서

- [../skills/](../skills/) - 스킬 개발 규칙
