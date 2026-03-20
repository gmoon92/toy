# Claude Code Agent Team

Claude Code의 멀티 에이전트 팀 기능으로 여러 전문 에이전트가 병렬로 작업을 처리합니다.

---

## 1. 사전 준비

### it2 설치 (iTerm2 Split Pane용)

```bash
# it2 설치
uv tool install it2

# PATH 설정 (zsh)
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 설치 확인
which it2
it2 vsplit
```

### iTerm2 Python API 활성화

1. iTerm2 → Settings → General → Magic → **Enable Python API**
2. iTerm2 재시작
3. 권한 요청 시 허용

### tmux 대안 (it2 미사용 시)

```bash
# tmux Control Mode로 실행
tmux -CC attach || tmux -CC new -A -s main
```

---

## 2. Agent Team 실행 명령어

tmux 로 분할 창 모드로 설정되어 있다면, 클로드 코드는 다음 명령어에 `--model` opus 모델을 강제하여 실행한다.
이 경우 생성된 서브에이전트가 띄워진 분할 창 cli 에서 `/model`를 통해 설정해야한다.

### Sub-Agent 실행 예시

```bash
cd /Users/moongyeom/IdeaProjects/squadliterv/be && \
env CLAUDECODE=1 \
    CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1 \
    ANTHROPIC_BASE_URL=https\://api.moonshot.ai/anthropic \
/Users/moongyeom/.local/share/claude/versions/2.1.71 \
    --agent-id critic-2@shiny-brewing-coral \
    --agent-name critic-2 \
    --team-name shiny-brewing-coral \
    --agent-color pink \
    --parent-session-id 1befa5ba-9625-4173-8108-a1b0d7dff4bd \
    --agent-type backend-critic \
    --permission-mode acceptEdits \
    --model claude-opus-4-6
```

### 환경변수

| 변수                                       | 설명                     |
|------------------------------------------|------------------------|
| `CLAUDECODE=1`                           | Claude Code 실행 모드      |
| `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` | Agent Team 기능 활성화      |
| `ANTHROPIC_BASE_URL`                     | API 게이트웨이 (Moonshot 등) |

### CLI 옵션

| 옵션                    | 설명                                     |
|-----------------------|----------------------------------------|
| `--agent-id`          | 고유 ID (이름@팀명)                          |
| `--agent-name`        | 에이전트 이름                                |
| `--team-name`         | 팀 이름                                   |
| `--agent-color`       | 터미널 표시 색상                              |
| `--parent-session-id` | 팀 리더 세션 ID                             |
| `--agent-type`        | 에이전트 유형 (backend-critic 등)             |
| `--permission-mode`   | `acceptEdits`, `plan`, `default`       |
| `--model`             | 사용 모델 (`inherit`, `claude-opus-4-6` 등) |

---

## 3. Span Mode (멀티 Pane)

여러 전문 에이전트를 동시에 실행:

| Pane | 역할                      |
|------|-------------------------|
| 1    | backend-critic (코드 리뷰어) |
| 2    | backend-qa (테스터)        |
| 3    | backend-architect (설계자) |

---

## 4. 참고 링크

- [it2 GitHub](https://github.com/mkusaka/it2)
- [iTerm2 Python API 인증](https://iterm2.com/python-api-auth.html)
- [tmux Control Mode](https://trzsz.github.io/tmuxcc.html)
- [LinkedIn 포스트 - Agent Team 활용법](https://kr.linkedin.com/posts/%ED%98%B8%EC%A4%80-%EC%9D%B4-57005b2a5_claude-code-agent-teams-iterm2-%EB%B6%84%ED%95%A0-%EC%B0%BD-%EB%AA%A8%EB%93%9C-activity-7425931851724386304-mdx_)
