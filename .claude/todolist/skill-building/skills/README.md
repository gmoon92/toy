# 스킬 개발 모범 사례 (Skills Development Best Practices)

효과적인 Claude Code 스킬 개발을 위한 핵심 규칙과 패턴입니다.

## 목적

효과적인 Claude Code 스킬을 구조화, 작성, 테스트하기 위한 지침입니다.

## 내용

### [principles/](principles/)
**핵심 스킬 개발 원칙**

효과적인 스킬 작성을 위한 기본 원칙입니다.

- [01-conciseness.md](principles/01-conciseness.md) - 컨텍스트 윈도우 효율성
- [02-appropriate-freedom.md](principles/02-appropriate-freedom.md) - 작업별 특이성 조정
- [README.md](principles/README.md) - 원칙 개요

### [guidelines/](guidelines/)
**특정 스킬 작성 지침**

스킬 문서 및 구현 패턴에 대한 표준입니다.

- [documentation-style.md](guidelines/documentation-style.md) - 스킬 문서 스타일
- [implementation-patterns.md](guidelines/implementation-patterns.md) - 10가지 구현 패턴

### [reference/](reference/)
**빠른 참조**

체크리스트와 빠른 시작 가이드입니다.

- [checklist.md](reference/checklist.md) - 스킬 품질 체크리스트
- [quick-guide.md](reference/quick-guide.md) - 빠른 시작 가이드

---

## 사용 시기

### 새 스킬 생성

새 스킬을 생성할 때 구조와 모범 사례 적용

**로드:**
- [reference/quick-guide.md](reference/quick-guide.md)
- [guidelines/documentation-style.md](guidelines/documentation-style.md)

### 스킬 리팩토링

기존 스킬을 개선할 때 간결성과 품질 검증에 집중

**로드:**
- [principles/01-conciseness.md](principles/01-conciseness.md)
- [reference/checklist.md](reference/checklist.md)

### 패턴 구현

특정 구현 패턴 적용

**로드:**
- [guidelines/implementation-patterns.md](guidelines/implementation-patterns.md)

### 품질 검증

스킬 품질 검증

**로드:**
- [reference/checklist.md](reference/checklist.md)

---

## 핵심 원칙

1. **간결성** - 컨텍스트 윈도우는 공유되므로 간결하게 유지
2. **적절한 자유** - 작업 취약성에 맞게 특이성 조정
3. **점진적 공개** - 필요할 때만 콘텐츠 로드
4. **모델로 테스트** - 대상 모델로 테스트
5. **강력한 지시문** - 명확한 실행 지시문
6. **토큰 효율성** - 스크립트 추출을 통한 0토큰 실행

---

## 디렉토리 구조

### 권장 스킬 구조

```
skill-name/
├── SKILL.md              # 핵심 워크플로우 (< 500줄)
├── references/           # 상세 문서
│   ├── guides/
│   ├── examples/
│   └── templates/
└── scripts/             # 실행 가능한 스크립트 (선택)
    ├── analysis/
    ├── validation/
    └── utils/
```

---

## 빠른 참조

### 명명
- **스킬:** 동명사 형태 (`processing-pdfs`) 또는 명사구 (`pdf-processing`)
- **스크립트:** 동사_명사 패턴 (`collect_changes.sh`, `validate_message.py`)

### 설명
**공식:** `[무엇을 하는지] + [언제 사용하는지]`

```yaml
description: PDF 파일에서 텍스트와 테이블 추출. PDF 작업 또는 사용자가 문서를 언급할 때 사용.
```

### 문서
- SKILL.md를 500줄 미만으로 유지
- 점진적 공개 사용
- 참조는 한 단계 깊이까지만
- 경로에는 슬래시 사용

---

## 관련 문서

- [../prompting/](../prompting/) - 프롬프팅 규칙
