# Claude Code에서 Kimi 모델 사용하기

Claude Code에서 Anthropic 모델 대신 Kimi 모델을 사용하는 방법을 설명합니다.

전제 조건
- Claude Code CLI가 설치되어 있어야 합니다.
- Moonshot AI API 키 발급 필요: https://platform.moonshot.ai/console/api-keys

---

## 설정 방법

### Step 0: 기존 ANTHROPIC_API_KEY 환경 변수 제거

기존에 Anthropic API 키가 설정되어 있다면 제거합니다.

```bash
# 환경 변수에서 API 키 제거 (임시)
unset ANTHROPIC_API_KEY

# 영구 제거는 ~/.zshrc, ~/.bashrc 등에서 해당 라인 삭제

# 환경 변수 제거 확인
echo $ANTHROPIC_API_KEY
```

> **출력 결과가 비어 있어야 정상입니다.**<br/>
> 값이 출력되면(예: `sk-ant-...`)  `~/.zshrc`, `~/.bashrc` 등에서 `ANTHROPIC_API_KEY` 설정을 찾아 삭제합니다.

---

### Step 1: Claude Code의 글로벌 설정

`~/.claude/settings.json`은 **Claude Code의 글로벌 설정 파일**입니다.

`env` 키를 통해 **환경 변수를 설정**하는 방식입니다. 
이 파일에 설정된 값은 Claude Code 실행 시 자동으로 환경 변수로 로드됩니다.

```bash
vim ~/.claude/settings.json
```

```json
{
  "env": {
    "ANTHROPIC_BASE_URL": "https://api.moonshot.ai/anthropic",
    "ANTHROPIC_AUTH_TOKEN": $YOUR_MOONSHOT_API_KEY,
    "ANTHROPIC_MODEL": "kimi-k2.5",
    
    "ANTHROPIC_DEFAULT_OPUS_MODEL": "kimi-k2.5",
    "ANTHROPIC_DEFAULT_SONNET_MODEL": "kimi-k2.5",
    "ANTHROPIC_DEFAULT_HAIKU_MODEL": "kimi-k2.5",
    "CLAUDE_CODE_SUBAGENT_MODEL": "kimi-k2.5"
  }
}
```

- `ANTHROPIC_BASE_URL`: Moonshot의 Anthropic 호환 엔드포인트
- `ANTHROPIC_AUTH_TOKEN`: Moonshot API 키 (⚠️ `ANTHROPIC_API_KEY` 아님!)
- `ANTHROPIC_MODEL`: 기본 사용 모델

> - Moonshot API 키는 [Moonshot AI Console](https://platform.moonshot.ai/console/api-keys)에서 확인합니다.
> - [code.claude.com - Claude Code settings](https://code.claude.com/docs/en/settings)
> - [platform.moonshot.ai - Console API Key Management](https://platform.moonshot.ai/console/api-keys)
> - [platform.moonshot.ai - Claude Code Configure Environment](https://platform.moonshot.ai/docs/guide/agent-support#configure-environment-variables)

---

### Step 2: Claude Code 인증 절차 우회

Kimi는 Claude Code의 공식 인증 방식이 아니므로, 
기존 Console 인증 상태를 해제하고 인증 절차를 우회해야 합니다.

#### 2.1. 기존 Console 인증 상태 해제

기존 Console 인증 상태를 해제하기 위해 로그아웃합니다.

```bash
claude /logout
```

#### 2.2. 인증 절차 확인 및 종료

`claude` 실행 후 아래와 같은 "로그인 방법 선택" 메시지가 나오면 **옵션을 선택하지 말고 종료합니다**.

```bash
➜  ~  claude
Welcome to Claude Code v2.1.39
…………………………………………………………………………………………………………………………………………………………

     *                                       █████▓▓░
                                 *         ███▓░     ░░
            ░░░░░░                        ███▓░
    ░░░   ░░░░░░░░░░                      ███▓░
   ░░░░░░░░░░░░░░░░░░░    *                ██▓░░      ▓
                                             ░▓▓███▓▓░
 *                                 ░░░░
                                 ░░░░░░░░
                               ░░░░░░░░░░░░░░░░
       █████████                                        *
      ██▄█████▄██                        *
       █████████      *
…………………█ █   █ █………………………………………………………………………………………………………………


 Claude Code can be used with your Claude subscription or billed based on API usage through
 your Console account.

 Select login method:
 ❯ 1. Claude account with subscription · Pro, Max, Team, or Enterprise
   2. Anthropic Console account · API usage billing
   3. 3rd-party platform · Amazon Bedrock, Microsoft Foundry, or Vertex AI
```

#### 2.3. hasCompletedOnboarding 설정

인증 절차를 무시하도록 설정합니다.

```bash
vim ~/.claude.json
```

```json
{
  "hasCompletedOnboarding": true
}
```

---

### Step 3: Claude Code 실행 및 확인

#### 3.1. 무인증 Claude 실행 확인

기존 인증 과정 없이 바로 Kimi API로 연결됩니다.

```bash
➜  ~  claude

 ▐▛███▜▌   Claude Code v2.1.39
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
  ▘▘ ▝▝    /Users/moongyeom

  /model to try Opus 4.6

──────────────────────────────────────────────────────────────────────────────────────────────
❯ 
──────────────────────────────────────────────────────────────────────────────────────────────
  [kimi-k2.5] ██░░░░░░░░ 23% Claude Code has switched from npm to native installer. Run
  moongyeom                  `claude install` or see
  1 MCPs                     https://docs.anthropic.com/en/docs/claude-code/getting-started
                             for more options.
```

- 에러 로그 메시지 없이 정상 `kimi-k2.5` 모델 연동 확인
- 만약 인증 방식 충돌 및 key 충돌 메시지가 표시된다면, [트러블슈팅](#트러블슈팅)을 참고하시면 됩니다.

#### 3.2. 실행 후 `/status` 명령으로 설정을 확인합니다.

```bash
➜  ~  claude

 ▐▛███▜▌   Claude Code v2.1.39
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
  ▘▘ ▝▝    /Users/moongyeom

  /model to try Opus 4.6

❯ /status
──────────────────────────────────────────────────────────────────────────────────────────────
 ────────────────────────────────────────────────────────────────────────────────────────────
  Settings:  Status   Config   Usage  (←/→ or tab to cycle)


  Version: 2.1.39
  Session name: /rename to add a name
  Session ID: 7c56dfed-cd83-4a01-8479-3fd4d87f5000
  cwd: /Users/moongyeom
  Auth token: ANTHROPIC_AUTH_TOKEN
  Anthropic base URL: https://api.moonshot.ai/anthropic

  Model: kimi-k2.5
  MCP servers: plugin:context7:context7 ✔, serena ✔
  Memory:
  Setting sources: User settings, Shared project settings
  Esc to cancel
```

확인 항목:
- `Auth token`: ANTHROPIC_AUTH_TOKEN
- `Anthropic base URL`: https://api.moonshot.ai/anthropic
- `Model`: kimi-k2.5

이전 작업들은 `/resume` 명령어를 통해 이어가면 됩니다.

enjoy!

---

## 트러블슈팅

<details>
<summary>Auth conflict: Using ANTHROPIC_API_KEY instead of Anthropic Console key.</summary>

**에러 메시지:**

```bash
➜  ~  claude

 ▐▛███▜▌   Claude Code v2.1.39
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
  ▘▘ ▝▝    /Users/moongyeom

  /model to try Opus 4.6

──────────────────────────────────────────────────────────────────────────────────────────────
Auth conflict: Using ANTHROPIC_API_KEY instead of Anthropic Console key.
Either unset ANTHROPIC_API_KEY, or run `claude /logout`.

>
```

**원인:**

- `ANTHROPIC_API_KEY` 환경 변수와 Claude Console 로그인이 동시에 설정됨

**해결 방법:**

```bash
# 환경 변수에서 API 키 제거 (임시)
unset ANTHROPIC_API_KEY

# 영구 제거는 ~/.zshrc, ~/.bashrc 등에서 해당 라인 삭제
```

자세한 내용은 [Step 0: 기존 ANTHROPIC_API_KEY 환경 변수 제거](#step-0-기존-anthropic_api_key-환경-변수-제거) 섹션을 참고하시면 됩니다.

</details>

<details>
<summary>Auth conflict: Both a token (ANTHROPIC_AUTH_TOKEN) and an API key</summary>

**에러 메시지:**

```bash
➜  ~  claude

 ▐▛███▜▌   Claude Code v2.1.39
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
  ▘▘ ▝▝    /Users/moongyeom

  /model to try Opus 4.6

──────────────────────────────────────────────────────────────────────────────────────────────
Auth conflict: Both a token (ANTHROPIC_AUTH_TOKEN) and an API key
(/login managed key) are set. This may lead to unexpected behavior.
  • Trying to use ANTHROPIC_AUTH_TOKEN? claude /logout
  • Trying to use /login managed key? Unset the ANTHROPIC_AUTH_TOKEN environment variable
  
> 
```

**원인:**

- Kimi API용 `ANTHROPIC_AUTH_TOKEN`이 설정됨
- 동시에 Claude Console 로그인 상태도 유지됨

**해결 방법:**

```bash
# Claude Console 로그아웃
claude /logout
```

자세한 내용은 [Step 2: Claude Code 인증 절차 우회](#step-2-claude-code-인증-절차-우회) 섹션을 참고하시면 됩니다.
</details>

---

## Reference

- [platform.moonshot.ai - Use kimi k2 Model in ClaudeCode/Cline/RooCode](https://platform.moonshot.ai/docs/guide/agent-support)
- [platform.moonshot.ai - Claude Code Configure Environment](https://platform.moonshot.ai/docs/guide/agent-support#configure-environment-variables)
- [code.claude.com - Claude Code settings](https://code.claude.com/docs/en/settings)
