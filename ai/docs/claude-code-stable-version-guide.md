# Claude Code Stable 버전 가이드

이 글은 Claude Code를 `stable` 버전으로 고정하는 방법을 소개합니다.

Claude Code CLI 최신 버전를 통해 개발 중 [API 400 (tool_reference)](issues/claude-code-api-400-tool-reference-troubleshooting.md) 에러를 겪으면서, 
최신 버전보다 `stable` 고정 전략이 더 현실적이라는 결론을 얻었습니다.

---

## 최신 버전에서 stable 버전으로 마이그레이션

Claude Code 버전은 `claude install <stable|latest|version>` 명령으로 지정할 수 있습니다.

```bash
claude install stable
# 또는
claude update
```

Claude Code의 latest/stable 버전은 아래 링크를 통해 확인할 수 있습니다.

- [npm 패키지 버전 확인](https://www.npmjs.com/package/@anthropic-ai/claude-code?activeTab=versions)
- [GitHub Releases](https://github.com/anthropics/claude-code/releases)

> 기존 Claude Code가 설치되어 있어도 `install` 명령을 실행하면 해당 버전으로 다시 설치됩니다.

### 업데이트 채널 stable 설정

마지막으로 CLI 설정에서 `/config` 명령어를 입력하여 업데이트 채널을 `stable`로 지정해야 합니다.

```bash
# Settings 화면에서 Auto-update channel을 stable로 변경
claude
❯ /config

 ▐▛███▜▌   Claude Code v2.1.58
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
  ▘▘ ▝▝    ~/IdeaProjects/private/toy

  /model to try Opus 4.6

❯ /config
─────────────────────────────────────────────────────────────────────────────────────────
 ───────────────────────────────────────────────────────────────────────────────────────
  Settings:  Status   Config   Usage  (←/→ or tab to cycle)


  Configure Claude Code preferences

  ╭────────────────────────────────────────────────────────────────────────────────────╮
  │ ⌕ Search settings...                                                               │
  ╰────────────────────────────────────────────────────────────────────────────────────╯

    Auto-compact                              true
    Show tips                                 true
    Reduce motion                             false
    Thinking mode                             true
    Prompt suggestions                        true
    Rewind code (checkpoints)                 true
    Verbose output                            false
    Terminal progress bar                     true
    Default permission mode                   Default
    Respect .gitignore in file picker         true
  ❯ Auto-update channel                       stable
    Theme                                     Dark mode
    Notifications                             Auto
    Output style                              default
    Language                                  korean
    Editor mode                               normal
    Show PR status footer                     true
    Model                                     kimi-k2.5
    Auto-connect to IDE (external terminal)   false
    Claude in Chrome enabled by default       true
    Teammate mode                             auto

  Space to change · / to search · Esc to cancel
```

- `Auto-update channel` = `stable`

## 마무리

AI 제품은 매우 빠른 속도로 발전하고 있고, Claude Code도 GitHub 활동(PR/릴리즈)만 봐도 업데이트 주기가 상당히 빠릅니다.  
이런 제품 특성상 최신 버전에는 베타 성격의 기능이 먼저 들어오고, 버전에 따라 예상하지 못한 사이드 이슈가 발생할 수 있습니다.

그래서 실무 환경에서는 최신 기능 추종보다 운영 안정성을 우선해 `stable` 채널을 기본값으로 유지하는 전략이 더 현실적입니다.  
문제가 발생하면 `원인 확인 -> stable 복구 -> 재검증` 순서로 대응하는 것을 권장합니다.

1. `claude --version`으로 현재 버전 확인
2. [공식 anthropics 깃헙 페이지](https://github.com/anthropics/claude-code/issues)에서 `release/issue` 탭에서 동일 증상 확인
3. `claude install stable`로 안정 버전 복구
4. 동일 프롬프트 재검증
5. 당시 버전/모델/로그/참고 링크 문서화
