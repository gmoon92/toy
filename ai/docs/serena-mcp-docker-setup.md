# Serena MCP Docker 설정 가이드

## 개요

**Serena**는 semantic code retrieval과 editing 기능을 제공하는 코딩 에이전트 툴킷입니다. MCP(Model Context Protocol) 서버 형태로 동작하며, Claude Code와 연동하여 IDE 수준의 코드 분석 및 편집 기능을 제공합니다.

### 주요 특징

- **Semantic Retrieval**: 코드의 의미를 이해하고 관련 심볼 검색
- **Code Editing**: 코드 수정 제안 및 자동화
- **Multi-language 지원**: Python, JavaScript/TypeScript, Java, C/C++ 등
- **Docker 지원**: 컨테이너 기반으로 쉽게 배포 가능 (실험적 기능)

---

## 사전 요구사항

- **Docker Engine**: 20.10 이상 권장
- **Docker Compose**: v2 이상
- **GitHub 계정**: GitHub Container Registry 접근용 (Public 이미지라 인증 불필요)

---

## 설치 방법

### 1. Docker Compose 설정

`.claude/docker/docker-compose.yml` 파일에 Serena 서비스를 추가합니다:

```yaml
name: claude-code
version: 'v1'

services:
  # https://github.com/oraios/serena/blob/main/DOCKER.md
  # https://github.com/oraios/serena/pkgs/container/serena
  serena:
    image: ghcr.io/oraios/serena:latest
    container_name: mcp-serena
    stdin_open: true
    tty: true
    volumes:
      # 현재 프로젝트를 워크스페이스로 마운트
      - ../../:/workspace/toy
    environment:
      - SERENA_DOCKER=1
    command: >
      serena start-mcp-server --transport stdio
    restart: "no"
```

- **`image`** - GitHub Container Registry의 공식 이미지 (`ghcr.io/oraios/serena:latest`)
- **`volumes`** - 분석할 프로젝트를 컨테이너 낸부 `/workspace`에 마운트
- **`command`** - stdio transport로 MCP 서버 실행 (Claude Code와 직접 통신)
- **`stdin_open` / `tty`** - MCP stdio 통신에 필요한 TTY 설정

### 2. MCP 설정

`.mcp.json` 파일에 Serena 서버를 등록합니다:

```json
{
  "mcpServers": {
    "serena": {
      "type": "stdio",
      "command": "docker",
      "args": [
        "compose",
        "-f",
        ".claude/docker/docker-compose.yml",
        "run",
        "--rm",
        "serena"
      ]
    }
  }
}
```

---

## 사용 방법

### 서버 실행 테스트

터미널에서 직접 Docker Compose로 실행핼 수 있습니다:

```bash
cd .claude/docker
docker compose run --rm serena
```

정상 실행 시 MCP 서버가 stdio로 초기화되고 명령을 대기합니다.

### Claude Code에서 활용

설정이 완료되면 Claude Code 대화 중 다음과 같이 Serena 기능을 사용할 수 있습니다:

- **코드 검색**: "이 프로젝트에서 `UserService` 클래스가 어디에 정의되어 있어?"
- **심볼 분석**: "`calculateTotal` 함수가 어떤 파라미터를 받는지 알려줘"
- **코드 편집**: "`utils.py`의 `format_date` 함수에 예외 처리를 추가해줘"

### Web Dashboard (선택사항)

Web Dashboard는 브라우저에서 Serena 상태를 확인하는 도구입니다. Claude Code와의 stdio 통신에는 불필요하지만, 로그 확인이나 설정 수정 시 유용합니다.

Dashboard를 활성화하려면 docker-compose에 포트 매핑을 추가하세요:

```yaml
serena:
  # ... 기존 설정
  ports:
    - "24282:24282"  # 기본 포트
    # 또는 충돌 시 다른 포트 사용
    # - "18080:24282"
  environment:
    - SERENA_DOCKER=1
    - SERENA_DASHBOARD_PORT=24282
```

Dashboard 접속: `http://localhost:24282/dashboard`

**참고**: 8080 포트는 일반적으로 웹 서버에서 사용되므로, 충돌을 피하려면 24282(기본) 또는 18080 등 다른 포트를 사용하세요.

---

## 문제 해결

### 이미지 Pull 실패

```bash
# GitHub Container Registry에서 이미지 수동 다운로드
docker pull ghcr.io/oraios/serena:latest
```

### 포트 충돌

Web Dashboard를 사용하는 경우 기본 포트(24282)가 이미 사용 중이면 환경변수로 변경:

```bash
SERENA_DASHBOARD_PORT=18080 docker compose up serena
```

### 볼륨 마운트 오류

상대 경로(`../../`)가 올바르게 작동하지 않으면 절대 경로로 변경:

```yaml
volumes:
  - /Users/moongyeom/IdeaProjects/private/toy:/workspace/toy
```

---

## 참고 자료

- **Serena GitHub Repository**: https://github.com/oraios/serena
- **Docker 설정 공식 문서**: https://github.com/oraios/serena/blob/main/DOCKER.md
- **GitHub Container Registry**: https://github.com/oraios/serena/pkgs/container/serena
- **MCP 공식 문서**: https://modelcontextprotocol.io/
- **Claude Code MCP 설정**: https://docs.anthropic.com/en/docs/claude-code/mcp
