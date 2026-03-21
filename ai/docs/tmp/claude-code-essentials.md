# Claude Code 핵심 가이드

Claude Code의 기본 개념과 효과적인 활용 방법을 설명합니다.

---

## Skills

스킬(Skill)은 재사용 가능한 작업 레시피를 정의한 문서입니다. Claude Code 문서는 프론트매터와 본문으로 구성됩니다.

### 스킬의 등장 배경

스킬 문서는 기존 서브 에이전트와 커맨드 문서의 정적 로딩 문제를 해결하기 위해 도입되었습니다. 스킬은 프론트매터의 메타데이터만 로드하고, 본문은 필요할 때만 로드합니다. 이는 Claude Code가 컨텍스트 윈도우 메모리 관리와 토큰 사용량 최적화에 중점을 두고 있음을 보여줍니다.

### 스킬 vs 도구

| 구분 | 도구(Tool) | 스킬(Skill) |
|:---|:---|:---|
| **정의** | 특정 함수를 직접 실행하기 위한 인터페이스 | 파일과 지침을 포함한 모듈형 실행 계약 |
| **적합한 작업** | 단일 작업, API 호출 | 복잡한 워크플로우, 도메인 지식이 필요한 작업 |
| **예시** | 파일 읽기, 검색, API 호출 | Git 커밋 메시지 작성, 코드 리뷰, 문서 생성 |

### 스킬의 장점과 단점

**장점:**
- 컨텍스트 윈도우의 auto compact 현상 완화
- 불필요한 토큰 사용 감소
- 재사용 가능한 레시피 제공

**단점:**
- 모델이 프론트매터의 `description`을 기준으로 스킬을 판별하여 호출하기 때문에, 동일한 프롬프트라도 호출 여부가 확률적으로 결정됩니다
- 문서를 위에서부터 순차적으로 읽으므로 섹션 위치가 중요합니다

### 디렉토리 구조

```
.claude/
└── skills/
    └── pdf-processing/
        ├── SKILL.md        # 실행 계약 (필수)
        ├── resources/      # 참조 자료 (선택)
        │     └── forms.md
        └── scripts/        # 실행 스크립트 (선택)
              └── analyze.py
```

### SKILL.md 작성 규약

**프론트매터 (필수):**

```yaml
---
name: git-commit-message
description: git diff를 분석하여 컨벤션에 맞는 커밋 메시지를 생성합니다. 사용자가 커밋 메시지 작성 또는 변경사항 검토를 요청할 때 사용합니다.
---
```

**필수 포함 요소:**
1. **무엇을 수행하는지** - 기능 설명
2. **언제 사용하는지** - 사용 시점/조건

**자동 호출(Invocation) 원리:**

Claude는 description을 의미 기반 매칭의 주요 신호로 활용하여 사용자 요청과 가장 관련성이 높은 스킬을 선택합니다. 이는 단순한 키워드 검색이 아닌 **의미 기반 매칭(semantic matching)**을 사용합니다.

| 문제 유형 | 원인 | 결과 |
|---------|------|------|
| **오탐** | description이 너무 모호함 | 관련 없는 스킬이 호출됨 |
| **미탐** | description이 너무 좁음 | 필요할 때 호출되지 않음 |
| **충돌** | 여러 스킬이 유사한 description | 예측 불가능한 선택 |

**description 작성 전략:**
- **구체적이면서도 넓게**: 특정 기능은 명확히, 사용 시점은 다양한 표현을 고려
- **중복 최소화**: 조직 내 다른 스킬과 중복되지 않도록 유니크한 키워드 사용
- **키워드 최적화**: 사용자가 실제로 사용할 검색어 포함

**스킬 충돌 관리:**

| 전략 | 설명 |
|------|------|
| **네이밍 컨벤션** | 조직/도메인 접두사 사용 (finance-report, hr-onboarding) |
| **키워드 분리** | 유사한 스킬 간 description 키워드 중복 최소화 |
| **범위 명확화** | 각 스킬의 고유 사용 시점을 명확히 구분 |

### 파일 크기 기준

| 기준 | 라인 수 | 조치 |
|-----|--------|------|
| 권장 | 500줄 이하 | 유지 |
| 경고 | 500~800줄 | 분리 검토 |
| 필수 | 800줄 초과 | 즉시 분리 (운영 상한) |

---

## Hooks

훅(Hook)은 Claude Code 라이프사이클의 특정 시점에서 실행되는 **사용자 정의 셸 명령어**입니다. 코드 포맷팅, 알림 전송, 명령어 검증, 프로젝트 규칙 강제 등의 작업을 자동화할 수 있습니다.

> Hook은 셸 명령어를 직접 호출하여 결정론적인 응답을 산출하므로, LLM 추론 비용이 발생하지 않습니다.

### 주요 Hook 이벤트

| 이벤트 | 설명 | 차단 가능 |
|--------|------|----------|
| `SessionStart` | 세션 시작/재개 시 | ❌ |
| `UserPromptSubmit` | 프롬프트 제출 전 | ✅ |
| `PreToolUse` | 도구 호출 실행 전 | ✅ |
| `PostToolUse` | 도구 성공 후 | ❌ |
| `PostToolUseFailure` | 도구 실패 후 | ❌ |
| `PermissionRequest` | 권한 대화 상자 표시 시 | ✅ |
| `Stop` | Claude 응답 완료 시 | ✅ |
| `PreCompact` | 컨텍스트 압축 전 | ❌ |
| `SubagentStart` | 서브에이전트 시작 시 | ❌ |
| `SubagentStop` | 서브에이전트 종료 시 | ✅ |
| `Notification` | 알림 발생 시 | ❌ |
| `ConfigChange` | 설정 파일 변경 시 | ✅ |
| `WorktreeCreate` | 워크트리 생성 시 | ✅ |
| `WorktreeRemove` | 워크트리 제거 시 | ❌ |

### 설정 예시

`.claude/settings.json` 파일에 Hook을 설정합니다:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./scripts/security-check.sh",
            "async": false
          }
        ]
      }
    ]
  }
}
```

### Hook 타입

| 타입 | 설명 | 사용 예시 |
|------|------|----------|
| `command` | 셸 명령어 실행 | 코드 검증, 포맷팅 |
| `http` | HTTP POST 요청 | 외부 알림, 웹훅 |
| `prompt` | 단일 턴 프롬프트 | 간단한 검증 |
| `agent` | 다중 턴 에이전트 | 복잡한 검증 작업 |

### Matcher 패턴

이벤트 타입별 매칭 대상:

| 이벤트 | 매처 필터 대상 | 예시 값 |
|--------|---------------|---------|
| `PreToolUse`, `PostToolUse`, `PostToolUseFailure`, `PermissionRequest` | 도구 이름 | `Bash`, `Edit\|Write`, `mcp__.*` |
| `SessionStart` | 세션 시작 방식 | `startup`, `resume`, `clear`, `compact` |
| `SessionEnd` | 세션 종료 이유 | `clear`, `logout`, `prompt_input_exit` |
| `Notification` | 알림 유형 | `permission_prompt`, `idle_prompt`, `auth_success` |
| `SubagentStart`, `SubagentStop` | 에이전트 타입 | `Bash`, `Explore`, `Plan` |
| `ConfigChange` | 설정 소스 | `user_settings`, `project_settings`, `local_settings` |
| `PreCompact` | 압축 트리거 | `manual`, `auto` |

MCP 도구 매칭: `mcp__<server>__<tool>` 패턴 사용
- 예시: `mcp__memory__create_entities`, `mcp__github__search_repositories`

### Exit Code 동작

| Exit Code | 동작 |
|-----------|------|
| **0** | 작업 진행 (stdout은 Claude 컨텍스트에 추가) |
| **2** | 작업 차단 (stderr는 Claude에게 피드백) |
| **기타** | 작업 진행 (stderr는 로그에만 기록) |

### 설정 위치

| 위치 | 범위 | 공유 가능 |
|------|------|----------|
| `~/.claude/settings.json` | 모든 프로젝트 | ❌ (로컬 머신) |
| `.claude/settings.json` | 단일 프로젝트 | ✅ (repo에 커밋 가능) |
| `.claude/settings.local.json` | 단일 프로젝트 | ❌ (gitignore) |
| 관리 정책 설정 | 조직 전체 | ✅ (관리자 제어) |
| Skill 또는 Agent frontmatter | 활성화 동안 | ✅ (컴포넌트 파일에 정의) |

### 모든 Hook 비활성화

`/hooks` 메뉴 하단 토글 또는 설정 파일에 `"disableAllHooks": true` 추가

### Hooks + Skills

스킬의 확률적 호출 문제를 해결하기 위해 훅과 함께 사용할 수 있습니다. 모델의 판단이 아닌 훅을 통해 특정 시점에 스킬 문서를 강제로 실행할 수 있습니다.

```yaml
---
name: secure-operations
description: Perform operations with security checks
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "./scripts/security-check.sh"
---
```

---

## Context View

Claude Code는 사용자와의 상호작용을 **Context View**라는 메모리 공간에서 관리합니다.

### 토큰 누적 메커니즘

```
[Turn 1] User Input (50 tokens) → AI Output (100 tokens) = 150 tokens
[Turn 2] Previous Context (150) + New Input (30) → Output (80) = 260 tokens
[Turn 3] Previous Context (260) + New Input (40) → Output (120) = 420 tokens
...
```

### 한정된 메모리와 Compact 현상

컨텍스트 뷰는 무한하지 않습니다. Claude Code는 **200K 토큰**(표준), 최대 **1M 토큰**(베타)을 지원합니다. 일정 토큰 한도에 도달하면 **Compact** 현상이 발생합니다.

**Compact의 영향:**

- 오래된 대화 내용을 요약하여 공간 확보
- 요약 과정에서 세부 지침이나 중요 문맥이 사라질 수 있음
- 워크플로우나 작업 단위가 명시된 문서도 의도한 대로 동작하지 않을 수 있음

> **주의**: `.md` 파일에 작성된 워크플로우나 꼭 지켜야 할 지침들도 compact되면서 핵심 내용이 유실될 수 있습니다.

### 컨텍스트 윈도우 구성 요소

| 구성 요소 | 설명 |
|----------|------|
| **System Prompt** | 모델 역할 및 동작 방식 정의 (CLAUDE.md, Rules 포함) |
| **Messages** | 이전 턴의 대화 내용 (사용자 메시지 + 어시스턴트 응답) |
| **Tool Results** | 도구 호출 결과 (파일 읽기/쓰기, API 응답 등) |

### 컨텍스트 편집 (Context Editing)

토큰 사용량이 증가함에 따라 자동으로 컨텍스트를 관리하는 기능입니다.

| 전략 | 설명 |
|------|------|
| **도구 결과 지우기** | 임계값 초과 시 오래된 도구 결과 자동 제거 |
| **Thinking 블록 지우기** | Extended thinking 활성화 시 thinking 블록 관리 |
| **클리언트 측 압축** | SDK에서 요약 생성 후 전체 기록 대체 |

### 모델별 컨텍스트 윈도우 크기

| 모델 | 컨텍스트 윈도우 | 특징 |
|-----|---------------|------|
| Claude Opus 4.6 | 200K | 최고 성능, 컨텍스트 인식 |
| Claude Sonnet 4.6 | 200K / 1M (베타) | 컨텍스트 인식, 장문 컨텍스트 지원 |
| Claude Haiku 4.5 | 200K | 경량, 컨텍스트 인식 |

---

## Ralph Wiggum Loop

랄프 위컴 루프(Ralph Wiggum Loop)는 반복적인 개선 사이클을 의미합니다. 실패하더라도 계속해서 완벽한 결과를 만들어내는 반복 구조입니다.

**워크플로우 단계:**
- 연구 (Research)
- 계획 (Plan)
- 개발 (Develop)
- 수정 (Refine)
- QA (Quality Assurance)
- 결과 (Deliver)

**사용자 개입 시점**

메인 컨텍스트 윈도우나 서브 에이전트에서 일련의 작업을 지시하다 볼 컨텍스트 오염에 빠지기 쉽습니다. 또한 메모리가 빠르게 가득 차 auto compact 현상이 발생하면 의도하지 않은 결과물이 나올 수도 있습니다.

참고: https://wikidocs.net/blog/@jaehong/8227/

---

## 문서별 작성 가이드

Claude Code 프로젝트에서 관리하는 문서 디렉토리 구조는 다음과 같습니다.

```
project
└── .claude/
    ├── agents/          # 에이전트 정의
    │   └── <name>.md
    ├── skills/          # 스킬 정의
    │   └── <name>/
    │       └── SKILL.md
    ├── commands/        # 커스텀 명령어
    │   └── <command>.md
    └── rules/           # 규칙 정의
        └── <name>.md
└── CLAUDE.md            # 프로젝트 컨텍스트
```

**문서 유형별 차이점:**
- CLAUDE.md vs Rules: 적용 범위와 로딩 시점의 차이
- Agent vs Skill: 실행 vs 참조의 차이
- 각 문서 유형별 목적과 사용 시기를 이해하는 것이 중요합니다

| 기능 | 유형 | 로드 시점 | 적용 범위 | 역할 | 사용 시기 | 컨텍스트 비용 | 통신 방식 | 작업 조율 | 토큰 비용 |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| **CLAUDE.md** | 문서 | 매 세션 시작 시 | 프로젝트 전체 | 모든 대화에서 지속적으로 참조되는 컨텍스트 | 프로젝트 규칙, "항상 X를 수행" 규칙 | 매 요청마다 로드 | 해당 없음 | 해당 없음 | 해당 없음 |
| **Rules** | 문서 | 매 세션 또는 파일 매칭 시 | 파일 경로별 지정 가능 | `paths` 프론트매터로 범위를 지정한 가이드라인 | 언어별 또는 디렉토리별 규칙이 필요할 때 | 로드 시 매 요청마다 | 해당 없음 | 해당 없음 | 해당 없음 |
| **Skills** | 문서 | 필요시 (호출 또는 관련성 판단 시) | 작업 단위별 | Claude가 사용할 수 있는 지침, 지식, 워크플로우 | 재사용 가능한 콘텐츠, 참조 문서, 반복 가능한 작업 | 낮음 (설명만 매 요청마다 로드)* | 해당 없음 | 해당 없음 | 해당 없음 |
| **Subagent** | 실행 | 생성 시 | 메인 세션에서 격리됨 | 요약된 결과만 반환하는 격리된 실행 컨텍스트 | 컨텍스트 분리, 병렬 작업, 전문 작업자가 필요할 때 | 메인 세션에서 격리됨 | 메인 에이전트에만 결과 보고 | 메인 에이전트가 모든 작업 관리 | 낮음: 결과만 메인 컨텍스트에 요약됨 |
| **Agent Team** | 실행 | 다중 독립 세션 병렬 실행 | 완전히 독립적 | 다중 독립 Claude Code 세션 조율 | 병렬 연구, 신규 기능 개발, 상충 가설을 활용한 디버깅 | 높음: 각 팀원이 별도 Claude 인스턴스 | 팀원 간 직접 메시지 교환 | 공유 태스크 목록으로 자체 조율 | 높음: 각 팀원이 별도 Claude 인스턴스 |

* Skill의 프론트매터에 `disable-model-invocation: true`를 설정하면 호출될 때까지 컨텍스트 비용을 제로로 줄일 수 있음

### 주요 조합 패턴

| 조합 패턴 | 작동 방식 | 예시 |
|:---|:---|:---|
| **Skill + MCP** | MCP는 연결을 제공하고, Skill은 Claude가 이를 잘 활용하도록 가르침 | MCP로 데이터베이스에 연결 → Skill에 스키마와 쿼리 패턴 문서화 |
| **Skill + Subagent** | Skill이 병렬 작업을 위해 Subagent를 생성 | `/audit` Skill이 보안, 성능, 스타일 Subagent를 동시에 실행하여 격리된 환경에서 작업 |
| **CLAUDE.md + Skills** | CLAUDE.md는 항상 적용되는 규칙을, Skills는 필요시 로드되는 참조 자료를 보관 | CLAUDE.md: "API 컨벤션을 따를 것" → Skill: 전체 API 스타일 가이드 포함 |

---

## 오해와 진실

### 서브에이전트 또는 스킬이 동작이 잘 안돼요

많은 사람들이 착각하고 있는 것이 있습니다. 지침에 적으면 무조건 호출되는 것은 아닙니다.

클로드 코드의 CLAUDE.md 관련 문서에 따른 지침은 가이드라인일 뿐, 반드시 지침을 지켜 실행하지는 않습니다.

예를 들어 스킬 또는 에이전트를 사용하여 작업 지시를 하면, 클로드 코드 자체가 프롬프트 자체를 한번 더 가공해서 요청하는 것을 디버깅 모드에서 확인할 수 있는데, 경우에 따라 문서의 지침이 아닌 자체 해석을 통해 요청을 가공하여 지침에서 벗어나는 경우가 있습니다.

이는 `Lost in the Middle` 현상과 관련이 있습니다. 문서의 앞뒤 문단만 기억하고 중간 문단(섹션)은 요약하는 현상입니다.

**문서 길이 권장사항:**
- CLAUDE.md: 공식 문서에서는 200라인을 권장하지만, 실제 경험상 60라인이 적절합니다.
- Agent, Skill 문서: 100라인 권장 (500줄 이하 유지, 800줄 초과 시 분리 필수)

**서브 에이전트 메모리 특성**

서브 에이전트는 독립적인 로컬 메모리 영역을 가지고 있습니다. CLAUDE.md는 사용자가 직접 작성·관리하는 반면, 서브 에이전트 메모리는 Claude가 자동으로 관리합니다. 반복적인 지시와 사용자 패턴을 분석하여 메모리에 저장하고, 작업 수행 시 해당 메모리를 참조합니다.

따라서 동일한 지침 문서를 사용하더라도 사용자별로 결과물이 달라질 수 있습니다.

> **참고**: 중요한 지침으로 인해 문서가 길어진다면, 참조 문서(reference)를 별도로 구성하여 연결 링크로 참조하도록 구성하세요.

### 프롬프트만 잘 작성하면 되지 않을까?

AI 작업을 하다 본 지시에 따라 제대로 동작하지 않는 결과물을 보고 답답함을 느낀 적이 있을 것입니다. "프롬프트만 잘 작성하면 되는 거 아니야?"라는 말을 들어본 적이 있을 겁니다.

같은 프롬프트라도 모델에 따라 결과가 다릅니다.

스킬이나 에이전트를 사용하여 작업을 지시하면, Claude Code가 프롬프트를 추가로 가공하여 요청합니다. 모델의 응답에 따라 도구(tool)나 스킬이 호출되기 때문입니다. Anthropic에서도 모델 출시 시마다 스킬이 제대로 동작하는지 검증하는 시스템을 구축해야 한다고 명시하고 있습니다.

Claude Code는 `시스템 프롬프트 → CLAUDE.md → 사용자 프롬프트` 순서로 동작합니다. 따라서 모델이 발전함에 따라 프롬프트 작성 방식도 달라지며, 문서도 지속적으로 유지보수해야 합니다.

**프롬프트 작성 방식의 변화:**
- 과거: 페르소나 주입, 상세하고 구체적인 작성
- 현재: 페르소나 없이 필요한 작업 지시만 명시 (Claude Code 스킬 문서 권장사항)

---

## 문서 관리 방법

결론적으로 사용자는 AI 행위를 감시하는 감독자 역할로 AI를 활용해야 합니다.

핵심은 문서 관리입니다. 바이브 코딩은 개인 프로젝트에 적합하지만, 실무에서는 유지보수가 중요합니다. 지시에 따른 결과물이 달라질 수 있으므로 체계적인 문서 관리가 필요합니다.

### 사례: 디자인 작업을 수행하지 않음

Figma 디자인을 기반으로 프론트엔드 작업을 지시했을 때, AI가 디자인을 임의로 해석하여 구현한 사례입니다.

AI는 작업 과정마다 결과가 달라질 수 있지만, 생성된 결과물의 문제를 분석하고 해석하는 능력은 매우 뛰어납니다. 이 특성을 활용하여 메인 컨텍스트에서 작업 결과를 검토하도록 요청할 수 있습니다.

```
디자인 작업중 로고 이미지 파일이 피그마 문서랑 달라
figma mcp 를 활용해서 asset 파일 목록 확인해줘
```

결과물의 잘못된 부분을 파악하고, 작업 진행 흐름을 재검토하며 잘못된 결정의 근본 원인을 분석한 후 문서에 지침을 추가하여 개선합니다.

**디버깅 프로세스:**
1. Figma MCP로 asset 파일 목록 확인
2. 로고 이미지 누락 확인
3. Figma 메타데이터 재검토
   - `get_metadata`: 구조, 레이아웃 사이즈 정보
   - `get_design_context`: asset 정보 확인
4. 작업 과정 분석 및 누락 부분 파악
5. 스킬 문서 개선 및 프롬프팅 수정

---

## Reference

- [Best Practices for Claude Code](https://code.claude.com/docs/en/best-practices)
- [skill vs subagent](https://code.claude.com/docs/en/features-overview#skill-vs-subagent)
- [claude.md vs skill](https://code.claude.com/docs/en/features-overview#claude-md-vs-skill)
- [claude.md vs rules vs skills](https://code.claude.com/docs/en/features-overview#claude-md-vs-rules-vs-skills)
- [subagent vs agent team](https://code.claude.com/docs/en/features-overview#subagent-vs-agent-team)
- [Claude Code - CLAUDE.md files](https://code.claude.com/docs/en/memory#claude-md-files)
- [Claude Code - How Claude remembers your project](https://code.claude.com/docs/en/memory#how-claudemd-files-load)
