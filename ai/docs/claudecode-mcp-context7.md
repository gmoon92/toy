# Context7 MCP 연동 가이드

Claude Code에서 Context7을 연동하는 방법을 정리했습니다.

## Context7이란?

Context7는 LLM과 AI 코드 에디터를 위한 **최신 코드 문서 플랫폼**입니다.

- 소스에서 직접 가져온 최신 문서와 코드 예제 제공
- 특정 라이브러리 버전의 문서 자동 매칭

## 사전 준비

시작하기 전에 API 키가 필요합니다.

### API 키 발급

1. https://context7.com/dashboard 에 접속
2. API 키 발급

요금제별로 월 API 호출 횟수가 정해져 있습니다. 자세한 요금제는 [Context7 Plans](https://context7.com/plans)에서 확인하세요.

### 환경변수 설정

발급받은 API 키는 `${CONTEXT7_API_KEY}` 환경변수로 설정합니다. `settings.local.json`에 넣거나 `export` 명령어로 설정하는 게 좋습니다. 실수로 코드나 히스토리에 키가 남지 않도록 주의하세요.

```bash
# STDIO 방식
claude mcp add --scope project context7 -- npx -y @upstash/context7-mcp --api-key ${CONTEXT7_API_KEY}

# HTTP 방식
claude mcp add --scope project --header "CONTEXT7_API_KEY: ${CONTEXT7_API_KEY}" --transport http context7 https://mcp.context7.com/mcp
```

> 참고: 프록시 설정 방법도 지원합니다.
>
> ```bash
> claude mcp add --scope user context7 \
>   -- npx -y @upstash/context7-mcp \
>   --api-key ${HTTPS_PROXY} \
>   --https-proxy http://proxy:port
> ```
>
> If you are behind an HTTP proxy, Context7 uses the standard https_proxy / HTTPS_PROXY environment variables.
> - https://github.com/upstash/context7/blob/master/packages/mcp/README.md#-tips

---

## 사용 방법

Context7은 LLM의 지식 한계를 보완해주는 도구예요. 

LLM은 학습했던 시점의 데이터로 응답하니까, 예를 들어 스프링 3으로 학습된 모델이 스프링 4 프로젝트에서 일할 때 최신 문서가 필요하겠죠.

그래서 프롬프트에 'use context7'이라고 명확히 말해줘야 해요. 그러면 Claude가 Context7을 활용해서 최신 정보를 찾아 작업을 수행하죠.

- `use context7` 또는 `context7로 확인해줘`
- `Context7에서 {라이브러리명} 최신 문서 찾아줘`

### 매번 입력하기 귀찮다면

`CLAUDE.md` 파일에 다음을 추가하면 매번 입력하지 않아도 되게끔 유도할 수 있습니다:

```
Always use context7 when I need code generation, setup or configuration steps,
or library/API documentation. This means you should automatically use the Context7
MCP tools to resolve library id and get library docs without me having to explicitly ask.
```

**참고**: CLAUDE.md에 있어도 Claude가 항상 이 지침을 따르지는 않습니다. 필요할 때 직접 `use context7`을 입력해주세요.

> Always use context7 when I need code generation, setup or configuration steps, or library/API documentation.
> - https://github.com/upstash/context7/blob/master/packages/mcp/README.md

---

## 참고 자료

- GitHub: https://github.com/upstash/context7
- MCP README: https://github.com/upstash/context7/blob/master/packages/mcp/README.md
- 문서: https://context7.com/docs
- 요금제: https://context7.com/plans
- MCP 클라이언트 목록: https://context7.com/docs/resources/all-clients
