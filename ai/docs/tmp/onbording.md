## skills

기본적으로 클로드 코드의 문서들은 프론트메터 + 본문으로 구성한다.
CLAUDE.md 와 서브 에이전트 문서 전체를 로드한다.
이는 `/agents`, `memory` 에 등록된 CLAUDE.md, 서브 에이전트 문서의 전체 텍스트가 그대로 로드된걸 확인할 수 있다.

클로드 코드 skill 문서는 기존 서브 에이전트와 커멘드 문서의 컨텍스트 윈도우의 정적 로드하는 문제를 해결하기 위해 나온 기능이다.

스킬은 프론트 메터만 클로드 코드 로드시에 적제한다. 문서 전체를 그대로 적재하지 않는다. 클로드 코드는 컨텍스트 뷰 메모리 관리(토큰 사용량)에 진심이라는걸 엿볼수 있다.

장점만 본다면 auto compact 현상을 해결되는거 아닌가? 기존 에이전트 문서들을 스킬로 대체해야될까?

단점도 존재한다.
사용자의 프롬프트에 의해 내장된 모델에 의해 등록된 스킬의 프론트 메터 중 `description`을 보고 적절한 스킬을 판별하여 호출하게 된다.

탑다운 방식으로 위에서 부터 읽고 점진적으로 로드하게 됨으로 본문에 섹션 위치도 굉장히 중요하다.

무엇보다 모델에 의해 트리거된다는 단점이 존재한다. 즉 같은 프롬프트라도 실제로 호출이 안된다. 확률 싸움.

## hooks

훅은 특정 이벤트 시점에 강제로 명령어나 promt 를 호출한다는 장점이 존재한다.

## hooks + skills

이런 불안정한 단점을 극복하고자 hooks 와 엮어 사용한다.

즉 모델에 의한 판단이 아닌 훅을 통해 강제로 특정 시점에 스킬을 문서를 무조건 강제로 실행시킬 수 있다.

# Agent native

- 랄프 루프(Ralph Wiggum Loop) 구성: 실패필도 계속해서 완벽을 만들어내

도록 (반복)

- 연구
- 계획
- 개발
- 수정
- QA
- 결과

- 사용자 개입 시점

-> https://wikidocs.net/blog/@jaehong/8227/

메인 컨텍스트 윈도우 또는 서브 에이전트에서 일련의 작업을 지시하다본 컨텍스트 오염에 빠지기 쉽다.
또한 금방 메모리가 가득차 auto compact 현상으로 인해 의도하지 않는 결과물을 산출하기도 하다.

## 리서치 or plan : Agent Team

agent team 을 활용해서 경쟁 토론후 고도화된 문서를 산출 받을 수 있다.

- Agent debate pattern (red vs blue)
- 피드백 루프 구성 (산출 -> 검토)

## 개발: 병령 서브 에이전트 + git worktree

분리된 작업 공간에서 각 에이전트가 작업하고 작업된 결과를 병합

- 내 정보 관리/기본 정보:
  @.claude/agents/workflows/frontend-design-orchestrator.md https://www.figma.com/design/7TZxXYOnX3eY3N7OFtEMFt/-RV--Squad-1?node-id=5369-3098&m=dev
- 내 정보 관리/비밀번호 변경:
  @.claude/agents/workflows/frontend-design-orchestrator.md https://www.figma.com/design/7TZxXYOnX3eY3N7OFtEMFt/-RV--Squad-1?node-id=5372-2124&m=dev

Git Worktree + sub agent isolation: worktree

```text
claude --worktree feature-auth   # 터미널 1: 새 기능 개발
claude --worktree bugfix-123     # 터미널 2: 버그 수정
```

워크트리에서 작업해줘" 또는 "새 worktree를 시작해줘"라고 하면

Claude가 자동으로 `.claude/worktree` 워크트리 디렉토리를 생성하고 해당 디렉토리에서 작업한다.

```text
// 워크트리 목록 확인
git worktree list

// 특정 워크트리 수동 삭제
git worktree remove .claude/worktrees/feature-auth

// 이미 삭제된 디렉토리의 워크트리 레코드 정리
git worktree prune
```

서브 에이전트 프론트 메터에 지정도 가능하다.

```text
---
isolation: worktree
---
```

---

# 오해와 진실

## 1. 서브에이전트 또는 스킬이 동작이 잘 안돼요.

많은 사람들이 착각하고 있는게 있는데 지침에 적으면 무조건 호출되는건 아닌가?

클로드 코드의 CLAUDE.md 관련 문서에 따륜 지침은 가이드라인이지 반드시 지침을 지켜 실행하지 않는다 라고 명시한다.

예를 들어 스킬 또는 에이전트를 사용하여 작업 지시를 하면,
클로드 코드 자체가 프롬프트 자체를 한번 더 가공해서 요청하는걸 디버깅 모드에서 확인할 수 있는데,
종종 문서 지침을 제대로 요청하는게 아닌, 자기가 해석하고 가공하는 과정에서 지침을 무시하거나 추가해서 요청한다.

실제 아래와 같이 오케스트레이션 스킬엔 다음과 같이 지정한다.

```bash
Figma 디자인 링크를 확인했습니다. frontend-design-orchestrator 에이전트를 실행하여 디자인 기반 프론트엔드 개발
  워크플로우를 시작하겠습니다.

  입력 정보:
  - Figma URL: https://www.figma.com/design/7TZxXYOnX3eY3N7OFtEMFt/-RV--Squad-1?node-id=5369-3098
  - 화면: 내 정보 관리/기본 정보

  frontend-design-orchestrator(Figma 디자인 기반 FE 개발 오케스트레이션)
  ⎿ Prompt:
       Figma 디자인을 기반으로 한 프론트엔드 개발 자동화 워크플로우를 실행해주세요.

       입력 정보

       - Figma URL: https://www.figma.com/design/7TZxXYOnX3eY3N7OFtEMFt/-RV--Squad-1?node-id=5369-3098
       - 화면 설명: 내 정보 관리/기본 정보 페이지

       워크플로우 단계

       Phase 1: 디자인 계획 생성

       1. design-planner 스킬을 사용하여 Figma 디자인 분석 및 계획 문서 생성
       2. 생성된 계획 문서 경로 보고

       Phase 2: 프론트엔드 개발

       1. 생성된 디자인 계획을 기반으로 frontend-developer 에이전트 실행
       2. 개발 완료 보고 수신

       Phase 3: UI 검증 (반복 루프, 최대 3회)

       1. frontend-ui-verifier 에이전트로 디자인-구현 일치성 검증
       2. 검증 결과:
         - 통과 → Phase 4로 이동
         - 실패 & iteration < 3 → 체크리스트 추출 후 재개발
         - 실패 & iteration >= 3 → Phase 5로 이동

       Phase 4: 완료 처리 (성공 시)

       1. Plan 문서 누락 분석
       2. 최종 완료 보고서(final-report.md) 생성
       3. 사용자 보고

       Phase 5: 실패 처리 (3회 반복 실패 시)

       1. 실패 보고서(failure-report.md) 생성
       2. 사용자 보고

       모든 산출물은 .claude/plans/ 디렉토리에 저장해주세요.

```

이는 `Lost in the Middle` 현상과 관련 있다. 문서의 앞, 뒤 문단 만 기억하고 중간 문단(섹션)은 요약하는 현상을 의미하는데,

- 클로드 공식 문서엔 claude.md 200 line 권장하지만, 경험상 60 라인 이 적절하다.
- agent, skill 문서는 100 line 권장한다.

적은 문서 라인수로 또 하나의 문제점이 있다.

서브 에이전트의 로컬 메모리 영역이라는게 존재하는데

CLAUDE.md 파일은 사용자가 직접 작성하고 관리하는 반면, 서브 에이전트 메모리는 클로드가 직접 관리한다.
지시할 때 반복적인 부분과 사용자의 패턴을 분석해 클로드가 메모리에 추가 메모하고, 작업 수행시 해당 메모리를 참고한다.

따라서 같은 지침 문서더라도 사용자마다 결과물이 달라진다.

> 만약 중요한 지침으로 인해 전체 문서 라인 수가 길어졌다면,
> 참조 문서(reference)를 별도로 구성해 연결 링크로 참조하는 방법을 구성하라.

## 2. 프롬프트를 잘 작성하면 되잖아. 지시를 너가 못한거야

AI 작업을 하다본, 지시에 따라 제대로 동작되지 않는 결과물을 보고 답답함을 토로하다본 누구나 한번 쯤은 들어봤을 말이다.

같은 프롬프트라도 모델에 따라 다르다.

스킬 또는 에이전트를 사용하여 작업 지시를 하면, 클로드 코드 자체가 프롬프트 자체를 한번 더 가공해서 요청하는걸 확인할 수 있다.
모델에 응답 값에 따라 도구(tool), 스킬을 호출하기 때문이다.
따라서 앤트로픽에서도 이를 모델을 출시할 때마다, 스킬을 검증하고 제대로 동작되는지 확인하는 시스템을 구축해야된다고 명시한다.
공식 플러그인에서 검증에 대한 많인 기능을 포함한 업데이트가 이뤄졌는데, 이를 커뮤니티에선 agent skill2.0이라 부른다.

클로드 코드는 시스템 프롬프트 -> 클로드 md -> 사용자 프롬프트 형태로 동작한다.

따라서 발전되는 모델에 따라 프롬프트도 다르게 작성해야되고, 문서들도 지속적으로 유지보수 해야한다.

예를들어 예전엔 초기 프롬프트엔 페르소나를 주입하고, 상세하고 구체적으로 작성해야 좀 더 잘 동작했었다.
하지만 최근 클로드 코드 스킬 문서엔 페르소나를 작성하지 않고 필요한 작업 지시만 명시하는게 좀 더 정확하게 동작된다고 가이드하고 있다.

---

# 문서 관리는 어떻게 하는데?

결과적으로 사용자는 AI의 행위를 감시하는 감독의 역할로써 AI 를 활용해야한다.

핵심은 문서 관리다.
바이브 코딩은 단순히 개인 프로젝트에서 진행해야지, 실무에선 유지보수가 중요하다.

기능 결과만 되는 핵심은 지시에 따른 결과물이 달라진다면

어떻게 그러면 문서를 관리해야되는데? 라고 질문한다면 다음 사례를 보자.

## 사례: 디자인 작업을 수행하지 않음

피그마를 통해 프론트 엔드 작업 지시중 디자인을 자기 멋대로 생각하고 구현했던 사례다.

![implementation-screenshot.png](implementation-screenshot.png)

AI는 작업 과정을 통해 결과를 도출하는 점은 매번 달라질 수 있지만,
결과를 가지고 문제를 해석하는건 되게 잘한다. 진짜 잘한다.

이를 이용해서 작업된 메인 컨텍스트 윈도우에서 다음 작업 과정을 검토도록 요청한다.

```
디자인 작업중 로고 이미지 파일이 피그마 문서랑 달라
figma mcp 를 활용해서 asset 파일 목록 확인해줘
```

결과물이 잘못된 부분을 알려주고,
작업 진행 흐름을 거꾸로 재검토하며 왜 그런 결정을 했는지 근본적인 원인을 파악하도록하여,
문서에 지침을 추가하여 보안하는 방식을 진행한다.

- 이미지 파일이 없다고 나옴.
- 피그마에 분명히 로고가 존재하니 다시 검토 요청
    - 확인 결과
        - get_metadata: 구조, 레이아웃 사이즈 정보
        - get_design_context: asset 정보를 가지고 있음
- 현재 작업 과정을 분석후 잘못된 부분을 검토 요청
- 작성된 스킬에서 어떤 부분이 누락됐는지 확인.
- 프롬프팅을 개선

# 문서마다 어떻게 작성해야되는데?

프로젝트를 진행하다 본 클로드 코드에서 관리할 문서의 전체적인 디렉토리 구조는 다음과 같다.

````text
project
ㄴ.claude
    ㄴ agents/
        ㄴ <name>.md
    ㄴ skills/
        ㄴ <name>/
            ㄴ SKILL.md
    ㄴ commands/
        ㄴ <command>.md
    ㄴ rules/
        ㄴ <name>.md
ㄴ CLAUDE.md
````

간혹 CLAUDE.md 파일과 rule.md 파일의 차이는 뭔지. 에이전트와 스킬 문서는 어떤걸 작성해야되는지 궁금 어떤걸 구성해야될지

| 기능             | 유형 | 로드 시점                | 적용 범위        | 역할                             | 사용 시기                           | 컨텍스트 비용                  | 통신 방식           | 작업 조율             | 토큰 비용                    |
|----------------|----|----------------------|--------------|--------------------------------|---------------------------------|--------------------------|-----------------|-------------------|--------------------------|
| **CLAUDE.md**  | 문서 | 매 세션 시작 시            | 프로젝트 전체      | 모든 대화에서 지속적으로 참조되는 컨텍스트        | 프로젝트 규칙, "항상 X를 수행" 규칙          | 매 요청마다 로드                | 해당 없음           | 해당 없음             | 해당 없음                    |
| **Rules**      | 문서 | 매 세션 또는 파일 매칭 시      | 파일 경로별 지정 가능 | `paths` 프론트매터로 범위를 지정한 가이드라인   | 언어별 또는 디렉토리별 규칙이 필요할 때          | 로드 시 매 요청마다              | 해당 없음           | 해당 없음             | 해당 없음                    |
| **Skills**     | 문서 | 필요시 (호출 또는 관련성 판단 시) | 작업 단위별       | Claude가 사용할 수 있는 지침, 지식, 워크플로우 | 재사용 가능한 콘텐츠, 참조 문서, 반복 가능한 작업   | 낮음 (설명만 매 요청마다 로드)*      | 해당 없음           | 해당 없음             | 해당 없음                    |
| **Subagent**   | 실행 | 생성 시                 | 메인 세션에서 격리됨  | 요약된 결과만 반환하는 격리된 실행 컨텍스트       | 컨텍스트 분리, 병렬 작업, 전문 작업자가 필요할 때   | 메인 세션에서 격리됨              | 메인 에이전트에만 결과 보고 | 메인 에이전트가 모든 작업 관리 | 낮음: 결과만 메인 컨텍스트에 요약됨     |
| **Agent Team** | 실행 | 다중 독립 세션 병렬 실행       | 완전히 독립적      | 다중 독립 Claude Code 세션 조율        | 병렬 연구, 신규 기능 개발, 상충 가설을 활용한 디버깅 | 높음: 각 팀원이 별도 Claude 인스턴스 | 팀원 간 직접 메시지 교환  | 공유 태스크 목록으로 자체 조율 | 높음: 각 팀원이 별도 Claude 인스턴스 |

\* Skill의 프론트매터에 `disable-model-invocation: true`를 설정하면 호출될 때까지 컨텍스트 비용을 제로로 줄일 수 있음

### Type 설명

- **Document**: 지시문/규칙 문서 (CLAUDE.md, Rules, Skills)
- **Execution**: 실행 방식/병렬 처리 메커니즘 (Subagent, Agent Team)

### 주요 조합 패턴 (공식 문서)

| 조합 패턴                  | 작동 방식                                              | 예시                                                         |
|------------------------|----------------------------------------------------|------------------------------------------------------------|
| **Skill + MCP**        | MCP는 연결을 제공하고, Skill은 Claude가 이를 잘 활용하도록 가르침       | MCP로 데이터베이스에 연결 → Skill에 스키마와 쿼리 패턴 문서화                    |
| **Skill + Subagent**   | Skill이 병렬 작업을 위해 Subagent를 생성                      | `/audit` Skill이 보안, 성능, 스타일 Subagent를 동시에 실행하여 격리된 환경에서 작업 |
| **CLAUDE.md + Skills** | CLAUDE.md는 항상 적용되는 규칙을, Skills는 필요시 로드되는 참조 자료를 보관 | CLAUDE.md: "API 컨벤션을 따를 것" → Skill: 전체 API 스타일 가이드 포함      |

- [skill vs subagent](https://code.claude.com/docs/en/features-overview#skill-vs-subagent)
- [claude.md vs skill](https://code.claude.com/docs/en/features-overview#claude-md-vs-skill)
- [claude.md vs rules vs skill](https://code.claude.com/docs/en/features-overview#claude-md-vs-rules-vs-skills)
- [subagent vs agent team](https://code.claude.com/docs/en/features-overview#subagent-vs-agent-team)
- [Claude Code - CLAUDE.md files](https://code.claude.com/docs/en/memory#claude-md-files)
- [Claude Code - How Claude remembers your project](https://code.claude.com/docs/en/memory#how-claudemd-files-load)

## 1. 오케스트레이션

스킬 문서는 프로세스를 지정하거나 행위의 단계를 담고 있다.

스킬 문서를 호출하게 되면 자체적으로 서브에이전트나 mcp 를 호출해서 사용하진 않는다.

오케스트레이션은 상시 로드되는 에이전트 보다 스킬 문서로 관리하는 편이 좋다.

```text
---
name: workflows:frontend-design-orchestrator
description: |
   Figma 기반 프론트엔드 개발 워크플로우를 오케스트레이션합니다.
   디자인 계획 → 개발 → UI 검증 → 피드백 반영 사이클을 최대 3회 자동 반복하며, 각 단계 산출물을 문서화하여 작업 연속성을 보장합니다.
argument-hint: figma-urls
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Write, Edit, Bash, Agent
hooks:
    - lint
    - test
    - doc-verifier
    - doc-rewriter
agent:
    - design-planner
    - frontend-developer
    - frontend-ui-verifier
---
```

- argument-hint
- disable-model-invocation
- user-invocable
- agent

일부로 모델이 판단해서 호출되지 않도록 disable-model-invocation 를 활성화하였고 `agent` 필드를 통해 에이전트를 통해 지정했다.

---

# Reference

- [Best Practices for Claude Code](https://code.claude.com/docs/en/best-practices)
- [skill vs subagent](https://code.claude.com/docs/en/features-overview#skill-vs-subagent)
- [claude.md vs skill](https://code.claude.com/docs/en/features-overview#claude-md-vs-skill)
- [claude.md vs rules vs skills](https://code.claude.com/docs/en/features-overview#claude-md-vs-rules-vs-skills)
- [subagent vs agent team](https://code.claude.com/docs/en/features-overview#subagent-vs-agent-team)
- [Claude Code - CLAUDE.md files](https://code.claude.com/docs/en/memory#claude-md-files)
- [Claude Code - How Claude remembers your project](https://code.claude.com/docs/en/memory#how-claudemd-files-load)
