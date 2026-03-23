# Reviewer Agent Resources

이 디렉토리는 통합 검증 에이전트(reviewer)가 사용하는 참조 문서들을 포함합니다.

## 디렉토리 구조

```
resources/
├── README.md          # 이 파일
└── checklist/         # 검증 체크리스트 문서
    ├── standard.md
    ├── technical.md
    ├── critical.md
    ├── reader.md
    └── structure.md
```

## 체크리스트 목록

| 파일                       | 검증 모드     | 설명                                     |
|--------------------------|-----------|----------------------------------------|
| `checklist/standard.md`  | standard  | 기본 품질 검증 (명확성, 완전성, 정확성, 일관성, 가독성, 구조) |
| `checklist/technical.md` | technical | 기술 정확성 검증 (코드, 용어, 버전, 환경 설정)          |
| `checklist/critical.md`  | critical  | 비판적 검증 (가정, 실패 시나리오, 논리 일관성)           |
| `checklist/reader.md`    | reader    | 독자 관점 검증 (이핏도, 실용성, 예시)                |
| `checklist/structure.md` | structure | 구조 분석 (중복, 흐름, 균형, 네비게이션)              |

## 사용 방법

Reviewer 에이전트는 문서 검증 시 다음 순서로 5가지 체크리스트를 참조합니다:

1. **standard** → 기본 품질 기준 검증
2. **technical** → 기술 정확성 검증
3. **critical** → 비판적 관점 검증
4. **reader** → 독자 관점 검증
5. **structure** → 문서 구조 분석

각 체크리스트는 검증 항목, 심각도 분류, 출력 형식을 포함합니다.

---

*이 리소스는 `.claude/agents/docs/reviewer.md` 에이전트에 의해 참조됩니다.*
