# Agent Team Practice

Claude Code Agent Team의 실전 활용 방법을 설명합니다.

---

## 1. Agent Team 개요

Agent Team은 여러 전문 Agent가 협업하여 작업을 수행하는 구조입니다. 단일 AI의 한계를 극복하고 더 높은 품질의 결과를 얻을 수 있습니다.

### 단일 AI의 한계

단일 AI는 다음과 같은 한계가 있습니다:

- **Agreeable Bias**: 쉽게 동의하는 성향으로 비판적 검증 부재
- **Confirmation Bias**: 초기 설계 방향 고수로 대안적 관점 배제
- **Hallucination**: 존재하지 않는 패턴 제시
- **Self-Critique Limit**: 스스로를 비판하지 못함

> 자세한 내용은 [claude-code-core-concepts.md의 Agent 시스템 개요](./claude-code-core-concepts.md#21-단일-agent-vs-agent-team)를 참조하세요.
>
> 일부 벤치마크에서만 정확도 향상이 보고되었으며, 모든 문제에서 항상 개선되는 것은 아닙니다.

---

## 2. Blue Team vs Red Team

### 2.1 Blue Team 역할 (설계 및 구현)

Blue Team은 설계와 구현을 담당합니다.

| 역할 | 책임 |
|------|------|
| **Architect** | 시스템 아키텍처 설계, 기술 스택 선정 |
| **Developer** | 코드 작성, 기능 구현 |
| **Security** | 보안 요구사항 정의, 보안 아키텍처 설계 |
| **Performance** | 성능 요구사항 정의, 성능 최적화 설계 |

### 2.2 Red Team 역할 (검증 및 공격)

Red Team은 검증과 공격을 담당합니다.

| 역할 | 책임 |
|------|------|
| **Critic** | 설계/코드 비판, 문제점 발굴 |
| **Devil's Advocate** | 반대 의견 제시, 대안적 관점 제공 |

### 다중 관점의 힘

```
역할 A (Architect): "Microservices가 적합합니다"
역할 B (Performance): "그러나 Latency가 3배 증가합니다"
역할 C (Security): "서비스 간 통신에서 인증 문제가 있습니다"

→ 다양한 관점의 충돌로 진실에 근접
```

---

## 3. 실행 프로세스

### 3.1 실행 흐름

```
1. 사용자 → Moderator에게 작업 요청
2. Moderator → Blue Team에 설계/구현 요청
3. Blue Team → 설계/구현 결과 생성
4. Moderator → Red Team에 검증 요청
5. Red Team → 피드백 제공
6. Blue Team → 피드백 반영하여 개선
7. 반복 → Synthesizer가 최종 정리
```

### 3.2 시간 관리

Agent Team은 동시에 실행되므로 시간 관리가 중요합니다.

| 단계 | 예상 시간 | 제한 |
|------|----------|------|
| 설계 (Blue) | 5-10분 | 15분 |
| 검증 (Red) | 3-5분 | 10분 |
| 개선 (Blue) | 5-10분 | 15분 |
| **총계** | **15-25분** | **40분** |

> 시간 초과 시 Agent가 자동으로 종료됩니다.

---

## 4. Span Mode 설정

### 4.1 it2 설치 및 설정

Agent Team을 효과적으로 사용하려면 터미널 멀티플렉서가 필요합니다.

```bash
# it2 설치 (macOS)
brew install --cask iterm2

# 또는 tmux
brew install tmux
```

### 4.2 CLI 옵션

```bash
# Agent Team 실행
claude --team --agents architect,developer,reviewer

# Span Mode 활성화
claude --span --team

# 시간 제한 설정
claude --team --timeout 30m
```

---

## 5. 고급 활용

### 5.1 Git Worktree 연계

Git Worktree와 Agent Team을 함께 사용하면 병렬 작업이 가능합니다.

```bash
# Worktree 생성
git worktree add ../feature-a feature-a
git worktree add ../feature-b feature-b

# 각 worktree에서 독립적 Agent 실행
cd ../feature-a && claude --team
cd ../feature-b && claude --team
```

**장점:**
- 완전히 격리된 환경
- 병렬 처리 가능
- 충돌 방지

### 5.2 오케스트레이션

복잡한 워크플로우는 오케스트레이션으로 관리합니다.

```yaml
# workflow.yaml
workflow:
  name: code-review
  steps:
    - agent: developer
      task: implement-feature
      output: pr-branch

    - parallel:
        - agent: reviewer
          task: code-review
        - agent: security
          task: security-review

    - agent: architect
      task: final-approval
      input: review-results
```

---

## 6. 비용/한계/주의사항

그러나 이러한 기능을 사용할 때 다음과 같은 한계와 비용을 반드시 인지해야 합니다.

### ⚠️ 주의사항

Agent Team은 강력하지만 비용과 한계가 있습니다. 신중하게 적용하세요.

### 6.1 Context Explosion

Agent Team은 Context Window를 빠르게 소모합니다.

| 구성 | Context 사용량 | 비용 |
|------|---------------|------|
| 단일 Agent | 1x | 기준 |
| 3인 Team | 3-5x | 3-5배 |
| 5인 Team | 5-10x | 5-10배 |

> 각 Agent가 독립적인 Context를 유지하므로 병렬 실행 시 비용이 선형 증가합니다.

### 6.2 비용 분석

> 아래 비용은 예시이며, 실제 비용은 모델 선택, 작업 복잡도, 턴 수에 따라 달라질 수 있습니다.

```
단일 Agent 작업: $0.10 (예시)
3인 Team 작업: $0.30-0.50 (예시)
5인 Team 작업: $0.50-1.00 (예시)
```

**비용 효율적 사용:**
- 중요한 설계 검토에만 사용
- 단순 작업에는 단일 Agent 사용
- 시간 제한을 명확히 설정

### 6.3 적용 체크리스트

Agent Team을 사용하기 전에 확인하세요:

- [ ] 작업의 복잡도가 높은가? (단순 작업은 단일 Agent로 충분)
- [ ] 다양한 관점이 필요한가? (보안, 성능, 유지보수 등)
- [ ] 충분한 시간이 있는가? (최소 15-20분 소요)
- [ ] 비용을 감수할 수 있는가? (단일 Agent의 3-5배)
- [ ] 검증이 필요한 설계/코드인가?
- [ ] Git Worktree 설정이 되어 있는가?

---

## 참고 문서

- [agent-fundamentals.md](./agent-fundamentals.md) - Agent 설계 기초
- [claude-code-agent-skills.md](./claude-code-agent-skills.md) - Skills 상세 작성법

---

*생성일: 2026-03-23*
*통합 문서: agent-debate-pattern.md + claude-code-agent-team.md + claude-code-advanced.md 기반*
