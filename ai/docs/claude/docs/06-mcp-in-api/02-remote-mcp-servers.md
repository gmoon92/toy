# [원격 MCP 서버](https://platform.claude.com/docs/en/agents-and-tools/remote-mcp-servers)

---

여러 회사들이 개발자들이 Anthropic MCP 커넥터 API를 통해 연결할 수 있는 원격 MCP 서버를 배포했습니다. 이러한 서버들은 MCP 프로토콜을 통해 다양한 서비스와 도구에 대한 원격 액세스를 제공함으로써 개발자와 최종 사용자가 사용할 수 있는 기능을 확장합니다.


> 아래 나열된 원격 MCP 서버는 Claude API와 함께 작동하도록 설계된 서드파티 서비스입니다. 이러한 서버는
> Anthropic이 소유하거나 운영하거나 보증하는 것이 아닙니다. 사용자는 신뢰할 수 있는 원격 MCP 서버에만 연결해야 하며
> 연결하기 전에 각 서버의 보안 관행과 약관을 검토해야 합니다.


## 원격 MCP 서버 연결하기

원격 MCP 서버에 연결하려면:

1. 사용하려는 특정 서버에 대한 문서를 검토합니다.
2. 필요한 인증 자격 증명이 있는지 확인합니다.
3. 각 회사에서 제공하는 서버별 연결 지침을 따릅니다.

Claude API와 함께 원격 MCP 서버를 사용하는 방법에 대한 자세한 내용은 [MCP 커넥터 문서](./06-mcp-in-api-01-mcp-connector.md)를 참조하세요.

## 원격 MCP 서버 예시

<MCPServersTable platform="mcpConnector" />


> **더 많은 서버를 찾고 계신가요?** [GitHub에서 수백 개의 MCP 서버를 찾아보세요](https://github.com/modelcontextprotocol/servers).

