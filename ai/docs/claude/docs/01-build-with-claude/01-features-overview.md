# [기능 개요](https://platform.claude.com/docs/en/build-with-claude/overview)

Claude의 고급 기능과 성능을 살펴보세요.

---

## 핵심 기능

이러한 기능들은 Claude의 기본 능력을 향상시켜 다양한 형식과 사용 사례에 걸쳐 콘텐츠를 처리하고, 분석하고, 생성합니다.

| 기능                                                             | 설명                                                                                                                                                      | 가용성                                                                         |
|----------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| [1M 토큰 컨텍스트 윈도우](../01-build-with-claude/02-context-windows.md) | 훨씬 더 큰 문서를 처리하고, 더 긴 대화를 유지하며, 더 광범위한 코드베이스로 작업할 수 있는 확장된 컨텍스트 윈도우입니다.                                                                                  | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [Agent Skills](04-agent-skills-01-overview.md)                 | Skills로 Claude의 능력을 확장합니다. 사전 빌드된 Skills(PowerPoint, Excel, Word, PDF)를 사용하거나 지침과 스크립트로 커스텀 Skills를 생성할 수 있습니다. Skills는 점진적 공개를 사용하여 컨텍스트를 효율적으로 관리합니다. | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [배치 처리](../02-capabilities/06-batch-processing.md)              | 비용 절감을 위해 대량의 요청을 비동기적으로 처리합니다. 배치당 많은 수의 쿼리가 포함된 배치를 전송할 수 있습니다. Batch API 호출은 표준 API 호출보다 50% 저렴합니다.                                                  | <PlatformAvailability claudeApi bedrock vertexAi />                         |
| [인용](../02-capabilities/07-citations.md)                        | Claude의 응답을 소스 문서에 근거합니다. Citations를 사용하면 Claude는 응답을 생성하는 데 사용하는 정확한 문장과 구절에 대한 상세한 참조를 제공할 수 있어, 더 검증 가능하고 신뢰할 수 있는 출력을 제공합니다.                        | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [컨텍스트 편집](../02-capabilities/02-context-editing.md)             | 구성 가능한 전략으로 대화 컨텍스트를 자동으로 관리합니다. 토큰 제한에 도달할 때 도구 결과를 지우고 확장된 사고 대화에서 사고 블록을 관리하는 기능을 지원합니다.                                                             | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [노력](../02-capabilities/04-effort.md)                           | effort 매개변수를 사용하여 Claude가 응답할 때 사용하는 토큰 수를 제어하고, 응답의 철저함과 토큰 효율성 간의 균형을 조정합니다.                                                                          | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [확장된 사고](../02-capabilities/03-extended-thinking.md)            | 복잡한 작업을 위한 향상된 추론 기능으로, 최종 답변을 제공하기 전에 Claude의 단계별 사고 과정을 투명하게 보여줍니다.                                                                                   | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [Files API](../02-capabilities/13-files-api.md)                 | 각 요청마다 콘텐츠를 다시 업로드하지 않고도 Claude와 함께 사용할 파일을 업로드하고 관리합니다. PDF, 이미지, 텍스트 파일을 지원합니다.                                                                       | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [PDF 지원](../02-capabilities/12-pdf-support.md)                  | PDF 문서에서 텍스트와 시각적 콘텐츠를 처리하고 분석합니다.                                                                                                                      | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [프롬프트 캐싱 (5분)](../02-capabilities/01-prompt-caching.md)         | Claude에게 더 많은 배경 지식과 예제 출력을 제공하여 비용과 지연 시간을 줄입니다.                                                                                                       | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [프롬프트 캐싱 (1시간)](../02-capabilities/01-prompt-caching.md)        | 자주 액세스하지 않지만 중요한 컨텍스트를 위한 1시간 연장 캐시 지속 시간으로, 표준 5분 캐시를 보완합니다.                                                                                           | <PlatformAvailability claudeApi azureAi />                                  |
| [검색 결과](../02-capabilities/14-search-results.md)                | 적절한 소스 귀속과 함께 검색 결과를 제공하여 RAG 애플리케이션에 대한 자연스러운 인용을 활성화합니다. 커스텀 지식 베이스 및 도구에 대해 웹 검색 품질의 인용을 달성합니다.                                                      | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [구조화된 출력](../02-capabilities/15-structured-outputs.md)          | 두 가지 접근 방식으로 스키마 준수를 보장합니다: 구조화된 데이터 응답을 위한 JSON 출력과 검증된 도구 입력을 위한 엄격한 도구 사용. Sonnet 4.5, Opus 4.5, Haiku 4.5에서 사용 가능합니다.                               | <PlatformAvailability claudeApi bedrockBeta azureAiBeta />                  |
| [토큰 계산](../02-capabilities/09-token-counting.md)                | 토큰 계산을 사용하면 메시지를 Claude에 보내기 전에 메시지의 토큰 수를 결정할 수 있어, 프롬프트와 사용량에 대해 정보에 입각한 결정을 내릴 수 있습니다.                                                               | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [도구 사용](../03-tools/01-overview.md)                             | Claude가 외부 도구 및 API와 상호 작용하여 더 다양한 작업을 수행할 수 있도록 합니다. 지원되는 도구 목록은 [도구 테이블](#tools)을 참조하세요.                                                              | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |

## 도구

이러한 기능들은 Claude가 외부 시스템과 상호 작용하고, 코드를 실행하며, 다양한 도구 인터페이스를 통해 자동화된 작업을 수행할 수 있도록 합니다.

code-execution-tool

| 기능                                                           | 설명                                                                                                | 가용성                                                                         |
|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| [Bash](../03-tools/04-bash-tool.md)                           | bash 명령과 스크립트를 실행하여 시스템 셸과 상호 작용하고 명령줄 작업을 수행합니다.                                                 | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [코드 실행](../03-tools/05-code-execution-tool.md)                | 고급 데이터 분석을 위해 샌드박스 환경에서 Python 코드를 실행합니다.                                                         | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [프로그래밍 방식 도구 호출](../03-tools/06-programmatic-tool-calling.md) | Claude가 코드 실행 컨테이너 내에서 프로그래밍 방식으로 도구를 호출할 수 있도록 하여, 다중 도구 워크플로의 지연 시간과 토큰 소비를 줄입니다.               | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [컴퓨터 사용](../03-tools/07-computer-use-tool.md)                 | 스크린샷을 찍고 마우스 및 키보드 명령을 실행하여 컴퓨터 인터페이스를 제어합니다.                                                     | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [세밀한 도구 스트리밍](../03-tools/03-fine-grained-tool-streaming.md)  | 버퍼링/JSON 검증 없이 도구 사용 매개변수를 스트리밍하여 대형 매개변수 수신의 지연 시간을 줄입니다.                                        | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [MCP 커넥터](./06-mcp-in-api-01-mcp-connector.md)               | 별도의 MCP 클라이언트 없이 Messages API에서 직접 원격 [MCP](https://platform.claude.com/docs/en/mcp) 서버에 연결합니다.                              | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [메모리](../03-tools/11-memory-tool.md)                          | Claude가 대화 간에 정보를 저장하고 검색할 수 있도록 합니다. 시간이 지남에 따라 지식 베이스를 구축하고, 프로젝트 컨텍스트를 유지하며, 과거 상호 작용에서 학습합니다. | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [텍스트 에디터](../03-tools/08-text-editor-tool.md)                 | 파일 조작 작업을 위한 내장 텍스트 에디터 인터페이스로 텍스트 파일을 생성하고 편집합니다.                                                | <PlatformAvailability claudeApi bedrock vertexAi azureAi />                 |
| [도구 검색](../03-tools/12-tool-search-tool.md)                   | 정규식 기반 검색을 사용하여 필요 시 동적으로 도구를 발견하고 로드하여 수천 개의 도구로 확장하고, 컨텍스트 사용을 최적화하며, 도구 선택 정확도를 향상시킵니다.        | <PlatformAvailability claudeApiBeta bedrockBeta vertexAiBeta azureAiBeta /> |
| [웹 가져오기](../03-tools/09-web-fetch-tool.md)                    | 심층 분석을 위해 지정된 웹 페이지 및 PDF 문서에서 전체 콘텐츠를 검색합니다.                                                     | <PlatformAvailability claudeApiBeta azureAiBeta />                          |
| [웹 검색](../03-tools/10-web-search-tool.md)                     | 웹 전체의 현재 실제 데이터로 Claude의 포괄적인 지식을 보강합니다.                                                          | <PlatformAvailability claudeApi vertexAi azureAi />                         |
