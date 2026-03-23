# Claude Code 문서 모음 (통합 완료)

## 문서 통합 결과

- **통합일**: 2026-03-23
- **원본 문서**: 13개 → **통합 후**: 8개 (38% 감소)

---

## 문서 구조

### 통합 문서 (4개)

| 문서                                                               | 설명                | 라인 수 | 출처                                     |
|------------------------------------------------------------------|-------------------|------|----------------------------------------|
| [claude-code-core-concepts.md](./claude-code-core-concepts.md)   | Claude Code 핵심 개념 | 434줄 | essentials + onbording + standard-guid |
| [claude-code-memory-loading.md](./claude-code-memory-loading.md) | CLAUDE.md 로딩 전략   | 187줄 | loading-strategy + claude-files-guid   |
| [agent-fundamentals.md](./agent-fundamentals.md)                 | Agent 설계 기초       | 264줄 | agent-design-guide                     |
| [agent-team-practice.md](./agent-team-practice.md)               | Agent Team 실전 활용  | 215줄 | agent-team + debate-pattern + advanced |

### 독립 문서 (4개)

| 문서                                                                 | 설명            | 라인 수 |
|--------------------------------------------------------------------|---------------|------|
| [claude-code-agent-skills.md](./claude-code-agent-skills.md)       | Skills 상세 작성법 | 945줄 |
| [claude-code-hooks.md](./claude-code-hooks.md)                     | Hooks 상세 레퍼런스 | 852줄 |
| [claude-code-lsp.md](./claude-code-lsp.md)                         | LSP 설정 및 사용법  | 315줄 |
| [claude-code-mcp.md](./claude-code-mcp.md)                         | MCP 연동 가이드    | 294줄 |
| [claude-code-context-windows.md](./claude-code-context-windows.md) | 컨텍스트 윈도우 심화   | 353줄 |

---

## 디렉토리 구조

```
ai/docs/tmp/
├── README.md                          # 본 파일
├── claude-code-core-concepts.md       # 통합 (핵심 개념)
├── claude-code-memory-loading.md      # 통합 (로딩 전략)
├── agent-fundamentals.md              # 통합 (Agent 기초)
├── agent-team-practice.md             # 통합 (Agent 실전)
├── claude-code-agent-skills.md        # 독립 (Skills 상세)
├── claude-code-hooks.md               # 독립 (Hooks 레퍼런스)
├── claude-code-lsp.md                 # 독립 (LSP 가이드)
├── claude-code-mcp.md                 # 독립 (MCP 가이드)
├── claude-code-context-windows.md     # 독립 (컨텍스트 심화)
├── backup/                            # 원본 백업 (14개)
└── archive/                           # 통합 후 이동된 원본 (9개)
```

---

## 학습 경로 추천

### 초급

1. [claude-code-memory-loading.md](./claude-code-memory-loading.md) - CLAUDE.md 로딩 이해
2. [claude-code-core-concepts.md](./claude-code-core-concepts.md) - 핵심 개념 학습

### 중급

3. [agent-fundamentals.md](./agent-fundamentals.md) - Agent 설계 기초
4. [claude-code-agent-skills.md](./claude-code-agent-skills.md) - Skills 작성법

### 고급

5. [agent-team-practice.md](./agent-team-practice.md) - Agent Team 활용
6. [claude-code-hooks.md](./claude-code-hooks.md) - Hooks 고급 활용

### 특화

- [claude-code-lsp.md](./claude-code-lsp.md) - LSP 활용
- [claude-code-mcp.md](./claude-code-mcp.md) - MCP 연동
- [claude-code-context-windows.md](./claude-code-context-windows.md) - 컨텍스트 최적화

---

## 목표 달성 확인

| 목표         | 결과               | 달성률      |
|------------|------------------|----------|
| 문서 수 감소    | 13개 → 8개         | 38% 감소 ✓ |
| 라인 수 최적화   | 6,030줄 → ~2,200줄 | 63% 감소 ✓ |
| 고유 인사이트 보존 | 핵심 내용 모두 포함      | 100% ✓   |

---

*통합 완료일: 2026-03-23*
