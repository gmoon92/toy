# 프롬프트 관리 지침

이 디렉토리는 Claude Code 작업에 사용되는 프롬프트 템플릿을 관리합니다.

## 디렉토리 구조

```
.claude/docs/prompts/
├── README.md                    # 이 파일
├── system/                      # 시스템 프롬프트
│   └── {name}-system.md
├── tasks/                       # 작업별 프롬프트
│   └── {task-name}/             # 작업 단위 중간 디렉토리
│       └── {purpose}-{type}.md
└── contexts/                    # 컨텍스트 템플릿
    └── {name}-context.md
```

## 명명 규칙

- **디렉토리명**: 영문 소문자, 하이픈(-) 사용 (예: `code-review`, `refactor-api`)
- **파일명**: 영문 소문자, 하이픈(-) 구분 (예: `review-prompt.md`, `analysis-context.md`)
- **패턴**: `{목적}-{타입}.md` (예: `code-review-system.md`, `api-refactor-task.md`)

## 프롬프트 작성 규칙

1. **파일명**: 위 명명 규칙 준수
2. **버전 관리**: 프롬프트 변경 시 버전 히스토리를 주석으로 기록
3. **재사용성**: 공통 패턴은 템플릿 변수로 추출

## 사용 방법

프롬프트 파일은 작업 수행 시 참조되며, Skill이나 Agent 정의에서 경로를 통해 로드할 수 있습니다.

## 경로 참조

- 환경 변수: `${CLAUDE_PROJECT_ROOT}/.claude/docs/prompts/`
