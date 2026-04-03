# Claude Code LSP 가이드

Claude Code는 플러그인 시스템을 통해 LSP(Language Server Protocol) 기능을 제공합니다.

이 문서는 LSP를 이미 알고 있는 분들을 위해 빠른 연동 방법을 먼저 다루고, 이후 LSP 개념과 활용 예시를 설명합니다.

---

## LSP 설정하기

Claude Code는 [v2.0.74(2025년 12월 19일)](https://github.com/anthropics/claude-code/releases/tag/v2.0.74) 버전부터 공식적으로 LSP 기능을 추가했습니다.

LSP 플러그인은 Claude Code가 Language Server와 통신하는 방법(연결 설정)만 담고 있습니다. 실제 코드 분석(타입 추론, 참조 탐색 등)은 Language Server 바이너리가 수행합니다.

따라서 **바이너리가 시스템에 설치되어 있지 않으면 LSP 기능이 동작하지 않습니다.** 플러그인 설치와 별개로 Language Server 바이너리를 먼저 설치해야 합니다.

1. `ENABLE_LSP_TOOL` 환경변수 설정
2. Language Server 바이너리 설치
3. Claude Code LSP 플러그인 설치

### `ENABLE_LSP_TOOL` 환경변수 설정

`ENABLE_LSP_TOOL` 환경변수가 설정되어 있어야 LSP 도구가 활성화됩니다.

```bash
# 실행 시 환경변수 지정
ENABLE_LSP_TOOL=true claude

# 또는 셸 설정 파일(~/.zshrc, ~/.bashrc)에 등록
export ENABLE_LSP_TOOL=true
```

> **`--bare` / `CLAUDE_CODE_SIMPLE=1` 사용 시 LSP가 비활성화됩니다.**
>
> `--bare` 플래그는 내부적으로 `CLAUDE_CODE_SIMPLE=1` 환경변수를 설정하는 **최소 도구 세트 모드**입니다.
> 이 모드에서는 `Bash`, `FileRead`, `FileEdit` 세 가지 도구만 제공되기 때문에 `ENABLE_LSP_TOOL=1`이 설정되어 있어도 LSP 도구가 등록되지 않습니다.
>
> 비활성화되는 주요 기능: LSP, Hooks, 플러그인 자동 동기화, 스킬 탐색, 자동 메모리, MCP 자동 검색
>
> 다음 두 가지는 동일하게 동작합니다:
> ```bash
> claude --bare "작업 내용"
> CLAUDE_CODE_SIMPLE=1 claude "작업 내용"
> ```
>
> `--bare`는 스크립트/자동화 환경에서 성능 최적화 목적으로 사용합니다. LSP가 필요한 코드 분석 작업에서는 사용하지 마세요.

### Language Server 바이너리 설치

아래 표에서 사용 중인 언어의 바이너리 설치 명령어를 확인합니다.

| 언어         | 플러그인                | Language Server 바이너리 설치 명령어                            |
|------------|---------------------|--------------------------------------------------------|
| C/C++      | `clangd-lsp`        | `brew install llvm`                                    |
| C#         | `csharp-lsp`        | `dotnet tool install -g csharp-ls`                     |
| Go         | `gopls-lsp`         | `go install golang.org/x/tools/gopls@latest`           |
| Java       | `jdtls-lsp`         | `brew install jdtls`                                   |
| Kotlin     | `kotlin-lsp`        | `brew install kotlin-language-server`                  |
| Lua        | `lua-lsp`           | `brew install lua-language-server`                     |
| PHP        | `php-lsp`           | `npm install -g intelephense`                          |
| Python     | `pyright-lsp`       | `pip install pyright`                                  |
| Rust       | `rust-analyzer-lsp` | `rustup component add rust-analyzer`                   |
| Swift      | `swift-lsp`         | Xcode에 포함 (macOS)                                      |
| TypeScript | `typescript-lsp`    | `npm install -g typescript-language-server typescript` |

예시 (Java, Kotlin):

```bash
brew install jdtls
brew install kotlin-language-server
```

> *"These plugins require the language server binary to be installed on your system."*
> - [Discover plugins](https://code.claude.com/docs/en/discover-plugins#code-intelligence)
>
> *"You must install the language server binary separately. LSP plugins configure how Claude Code connects to a language
server, but they don't include the server itself."*
> - [Plugins reference](https://code.claude.com/docs/en/plugins-reference#lsp-servers)

### LSP 플러그인 설치

공식 마켓플레이스 `claude-plugins-official`에서 언어별 [LSP 플러그인](https://code.claude.com/docs/en/discover-plugins#code-intelligence) 을 제공합니다. 아래 명령어로 설치합니다.

```bash
claude plugin install jdtls-lsp@claude-plugins-official \
  && claude plugin install kotlin-lsp@claude-plugins-official
```

> **참고**: Claude Code CLI 내부에서도 `/plugin install`로 설치할 수 있습니다.
> 설치 후 `/reload-plugins`로 적용하세요. 설치가 됐더라도 LSP 도구를 호출하지 않는다면, CLI 재시작하세요.
>
> ```bash
> /plugin install jdtls-lsp@claude-plugins-official
> /plugin install kotlin-lsp@claude-plugins-official
>
> # 설치된 플러그인 조회: /plugin 실행 후 Installed 탭 선택
> # 또는 터미널 CLI에서 claude plugin list
> /plugin
>
> /reload-plugins
> ```

### 플러그인 활성화

공식 플러그인은 설치 후 기본적으로 **활성화 상태**입니다.

특정 플러그인을 비활성화하려면 `settings.json`의 [`enabledPlugins`](https://code.claude.com/docs/en/settings#enabledplugins) 항목을 `false`로 설정합니다.

```json
{
  "enabledPlugins": {
    "jdtls-lsp@claude-plugins-official": false
  }
}
```

### 커스텀 LSP 서버 설정

공식 마켓플레이스에 없는 언어를 사용하거나 직접 설치한 Language Server를 연결하려면, 로컬 플러그인을 직접 만들어야 합니다.

LSP 설정 파일(`.lsp.json`)은 플러그인 디렉토리 내부에서만 로드됩니다. 아래 구조로 플러그인을 생성합니다.

```
<프로젝트>/
└── .claude/
    └── plugins/
        └── my-custom-lsp/           # 플러그인 루트 (이름 자유)
            ├── .claude-plugin/
            │   └── plugin.json      # 플러그인 매니페스트
            └── .lsp.json            # LSP 서버 설정
```

`.lsp.json` 예시 (Go):

```json
{
  "go": {
    "command": "gopls",
    "extensionToLanguage": {
      ".go": "go"
    }
  }
}
```

- `command`: 실행할 Language Server 바이너리 (PATH에 있어야 함)
- `extensionToLanguage`: 파일 확장자를 언어 식별자로 매핑

> `command`, `extensionToLanguage`는 필수 필드입니다.
>
> 자세한 설정은 [Plugins Reference - LSP Servers](https://code.claude.com/docs/en/plugins-reference#lsp-servers)를 참고하세요.

---

## LSP란 무엇인가?

LSP(Language Server Protocol)는 IDE와 개발 도구가 프로그래밍 언어 지원 기능을 구현하는 방식을 표준화한 프로토콜입니다.

LSP 이전에는 IDE마다 각 프로그래밍 언어의 분석 기능을 직접 구현해야 했습니다. 새로운 언어가 등장할 때마다 모든 IDE가 개별적으로 지원 기능을 개발해야 했으니, `IDE 수 × 언어 수`만큼의 중복 구현이 불가피한 구조였습니다. LSP는 이 문제를 해소하고자 IDE와 언어 서버 간의 표준 통신 프로토콜을 정의했습니다. 이제 IDE는 LSP 클라이언트만 구현하면 되고, 언어 분석은 별도의 Language Server가 담당합니다.

---

## LSP가 제공하는 가치

Claude Code는 기본적으로 `grep` 기반으로 파일을 탐색합니다.

이 방식은 타입 추론, 타입 사용 위치, 변수 사용처 등을 정확히 파악하기 어렵습니다.

소스 코드 리팩토링이나 분석 작업 시 누락이 발생해 컴파일 오류로 이어지는 경우가 있는데,
LSP를 연동하면 Language Server의 코드 분석을 통해 이 문제를 해소할 수 있습니다.

### 왜 단순 검색으로는 부족한가?

IDE 기능은 AST(Abstract Syntax Tree, 추상 구문 트리), `symbol graph`, `type system` 등 깊이 있는 코드 분석이 필요합니다.

```java
// 예시: 변수 shadowing
class UserService {
	private UserRepository repo;  // 필드

	public void process(Repository repo) {  // 매개변수가 필드를 가림
		repo.save();  // grep "repo"로는 어떤 repo인지 구분 불가
	}
}
```

단순한 텍스트 검색(`grep repo`)으로는 다음과 같은 상황을 정확히 처리할 수 없습니다:

| 상황              | 설명                                | LSP로 해결하는 방법             |
|-----------------|-----------------------------------|--------------------------|
| 변수 Shadowing    | 같은 이름의 변수가 다른 스코프에 존재             | AST 기반 스코프 분석            |
| Import Alias    | `import A as B` 형태의 별칭            | 심볼 해석(symbol resolution) |
| Method Overload | 같은 이름의 메서드 오버로딩                   | 타입 시스템 분석                |
| Generic Type    | `List<String>` vs `List<Integer>` | 타입 파라미터 추적               |

### LSP 지원 기능

- 타입 오류 및 경고 자동 보고: 파일 열기(Read/Edit) 후 LSP 서버가 문법/타입 오류 자동 감지
- 코드 네비게이션: 정의로 이동, 참조 찾기 및 호버 정보
    - 심볼의 정의 위치로 이동: 함수/클래스 선언 위치 찾기
    - 심볼의 모든 참조 찾기: 변수/메서드 사용처 검색
    - 타입 및 문서 정보 표시: 변수/함수 위에서 타입 확인
    - 파일의 모든 심볼 목록: 클래스 멤버, 함수 목록 보기
    - 워크스페이스 전체 심볼 검색: 프로젝트 전체에서 특정 클래스/함수 검색
    - 인터페이스/추상메서드 구현체 찾기: `Repository`의 구현체 찾기
    - 함수 호출 계층 추적: incomingCalls(해당 함수를 호출하는 함수), outgoingCalls(해당 함수가 호출하는 함수)
- 언어 인식: 코드 기호에 대한 타입 정보 및 문서

> [Tools Reference - LSP](https://code.claude.com/docs/en/tools-reference)
> ```
> Code intelligence via language servers. Reports type errors and warnings automatically after file edits. Also supports
> navigation operations: jump to definitions, find references, get type info, list symbols, find implementations, trace
> call hierarchies. Requires a code intelligence plugin and its language server binary.
> ```


## LSP 활용 프롬프트 예시

위에서 살펴본 시나리오처럼 grep으로 해결하기 어려운 상황에서 LSP를 활용하는 프롬프트 예시입니다.

> 만약 LSP 도구 호출 시 아래와 같이 `Executable not found in $PATH` 오류가 발생하면,   
> [Language Server 바이너리 설치](#language-server-바이너리-설치) 섹션의 설치 명령어로 바이너리를 먼저 설치하세요.
> 
> ```
> ⏺ LSP(operation: "goToImplementation", symbol: "CorsOriginRepositoryCustom", ...)
>   ⎿ Error performing goToImplementation: Executable not found in $PATH: "jdtls"
> ```

**1. 인터페이스 구현체 찾기 (Find Implementations)**

`grep`은 인터페이스 선언과 텍스트만 찾지만, LSP는 실제 구현체(`CorsOriginRepositoryCustomImpl`)와 바인딩된 호출을 정확히 추적합니다.

```
CorsOriginRepositoryCustom 인터페이스의 모든 구현체를 찾아서,
각 구현 메서드가 어디서 호출되는지 추적해줘.
```

**2. 메서드 오버로딩 + 호출 계층 추적 (Call Hierarchy)**

동일 이름 메서드가 오버로딩된 경우 `grep`은 구분 불가, LSP는 타입 시그니처 기반으로 정확히 분리합니다.

```
UserService의 모든 메서드를 나열하고,
각 메서드가 어떤 컨트롤러/필터에서 호출되는지 호출 계층을 분석해줘.
```

**3. 타입 계층 분석 (Type Hierarchy)**

Repository 상속 체인(`JpaRepository` → `CrudRepository` → ...)을 LSP 심볼 해석으로 정확히 분석합니다.

```
CorsOriginRepository가 어떤 Spring Data 인터페이스를 상속받는지
타입 계층 전체를 추적하고, JPA 쿼리 메서드가 어떤 타입으로 동작하는지 설명해줘.
```

**4. 참조 찾기 (Find References) — 변수 Shadowing 포함**

`grep`으로는 스코프 구분 불가, LSP는 AST 기반 정확한 스코프 분석이 가능합니다.

```
JwtAuthenticationFilter에서 사용되는 변수들 중
같은 이름이 다른 스코프에서 재사용되는 케이스가 있는지 분석해줘.
```

**5. 심볼 목록 + 타입 정보 (Document Symbol + Hover)**

```
SecurityConfig 클래스의 모든 메서드와 그 반환 타입을 나열하고,
각 Bean이 어디에 주입되어 사용되는지 찾아줘.
```

---

## 참고 자료

- [Claude Code 공식 문서 - Changelog](https://code.claude.com/docs/en/changelog)
- [Claude Code 공식 문서 - Tools Reference](https://code.claude.com/docs/en/tools-reference#tools-reference)
- [Claude Code 공식 문서 - Code Intelligence](https://code.claude.com/docs/en/discover-plugins#code-intelligence)
- [Claude Code 공식 문서 - LSP Servers](https://code.claude.com/docs/en/plugins-reference#lsp-servers)
- [Claude Code 공식 문서 - Add LSP Servers to Your Plugin](https://code.claude.com/docs/en/plugins#add-lsp-servers-to-your-plugin)
- [Claude Code 공식 문서 - Settings - enabledPlugins](https://code.claude.com/docs/en/settings#enabledplugins)
