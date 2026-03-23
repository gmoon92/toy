# Claude Code LSP 가이드

Claude Code는 플러그인 시스템을 통해 LSP(Language Server Protocol) 기능을 제공합니다.

이 문서에서는 LSP의 개념, Claude Code에서의 플러그인 설치 및 설정 방법, 사용 예시를 설명합니다.

---

## LSP란 무엇인가?

LSP는 IDE와 언어 서버 간 표준 통신 프로토콜로, 기존 N×M 구조의 문제를 N+M 구조로 해결합니다.

### LSP가 해결하는 문제

LSP(Language Server Protocol)는 IDE와 개발 도구가 프로그래밍 언어 지원 기능을 구현하는 방식을 표준화한 프로토콜입니다.

- **기존 방식 (N×M 구조)**: VSCode, IntelliJ, Vim 등 각 IDE마다 Java, Python, Go 지원 기능을 따로 개발 → IDE 3개 × 언어 3개 = 9개 구현 필요
- **LSP 방식 (N+M 구조)**: 각 IDE는 "LSP 클라이언트"만, 각 언어는 "LSP 서버"만 구현 → IDE 3개 + 언어 3개 = 6개 구현으로 충분

#### 기존 방식의 문제 (N×M 구조)

기존에는 IDE마다 각 프로그래밍 언어에 대한 분석 기능을 직접 구현해야 했습니다.

| IDE      | Java 지원 | Python 지원 | Go 지원 | Rust 지원 |
|----------|---------|-----------|-------|---------|
| VSCode   | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
| IntelliJ | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
| Eclipse  | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
| Neovim   | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |

새로운 언어가 등장할 때마다 모든 IDE가 해당 언어에 대한 지원 기능을 개발해야 했습니다. 이는 `IDE 수 × 언어 수` 만큼의 구현이 필요한 비효율적인 구조였습니다.

#### LSP 도입 후 (N+M 구조)

```
┌─────────────┐      LSP       ┌─────────────────┐
│   Editor    │ ◄────────────► │ Language Server │
│  (Claude    │                │   (gopls,       │
│   Code)     │                │  rust-analyzer) │
└─────────────┘                └─────────────────┘
```

LSP는 IDE와 언어 서버 간의 표준 통신 프로토콜을 제공합니다. 이제 IDE는 LSP 클라이언트만 구현하면 되고, 언어 분석은 별도의 Language Server가 담당합니다.

| 구성 요소           | 역할                  | 예시                          |
|-----------------|---------------------|-----------------------------|
| Editor          | UI 제공, LSP 클라이언트 역할 | Claude Code, VSCode, Neovim |
| LSP             | 통신 프로토콜             | JSON-RPC 기반 표준 프로토콜         |
| Language Server | 코드 분석, AST 파싱       | gopls, rust-analyzer, jdtls |

**구현 중복 제거:**

- 기존: `IDE × 언어` 개의 구현 필요
- LSP 이후: `IDE + 언어` 개의 구현 필요

즉, **Language Server는 컴파일러 수준의 코드 분석기**입니다.

### 왜 단순 검색으로는 부족한가?

IDE 기능은 AST(Abstract Syntax Tree, 추상 구문 트리), symbol graph, type system 등의 깊이 있는 코드 분석이 필요합니다.

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

---

## Claude Code에서 LSP 설정하기

### 1. LSP 지원 기능

Claude Code는 LSP를 통해 다음과 같은 기능을 제공합니다:

| 기능                   | 설명            | LSP 메서드                           | 활용 예시                   |
|----------------------|---------------|-----------------------------------|-------------------------|
| **Go to Definition** | 심볼의 정의 위치로 이동 | `textDocument/definition`         | 함수가 어디서 선언되었는지 찾기       |
| **Find References**  | 심볼의 모든 참조 찾기  | `textDocument/references`         | 변수의 모든 사용처 검색           |
| **Hover 정보**         | 타입 및 문서 정보 표시 | `textDocument/hover`              | 변수 위에서 타입 및 문서 정보 확인    |
| **Rename Symbol**    | 심볼 이름 일괄 변경   | `textDocument/rename`             | 변수명을 변경하면 모든 참조 자동 업데이트 |
| **Diagnostics**      | 실시간 오류 및 경고   | `textDocument/publishDiagnostics` | 코드 작성 중 문법 오류 감지        |

> 자세한 내용은 [Tools Reference - LSP](https://code.claude.com/docs/en/tools-reference)를 참고하세요.

### 2. LSP 플러그인 설치

Claude Code에서는 공식 `claude-plugins-official` 마켓플레이스에서 언어별 LSP 플러그인을 설치할 수 있습니다.

| 플러그인             | Language Server            | 언어                     |
|------------------|----------------------------|------------------------|
| `typescript-lsp` | TypeScript Language Server | TypeScript, JavaScript |
| `pyright-lsp`    | Pyright                    | Python                 |
| `rust-lsp`       | rust-analyzer              | Rust                   |

```bash
/plugin install typescript-lsp@claude-plugins-official
/reload-plugins
```

> **참고**: `claude-plugins-official` 플러그인을 사용하면 Language Server 바이너리가 **자동으로 설치**됩니다. 별도로 시스템에 바이너리를 설치할 필요가 없습니다.
>
> 더 많은 LSP 목록은 [Code Intelligence](https://code.claude.com/docs/en/discover-plugins#code-intelligence)를 참고하세요.

### 3. 플러그인 활성화

#### 공식 플러그인 사용 (권장)

공식 플러그인은 설치 후 기본적으로 **활성화 상태**입니다. `/reload-plugins` 후 바로 사용할 수 있습니다.

**플러그인 비활성화**

특정 플러그인을 비활성화하려면 `settings.json`의 [`enabledPlugins`](https://code.claude.com/docs/en/settings#enabledplugins)에 `false`로
설정합니다:

```json
{
  "enabledPlugins": {
    "typescript-lsp@claude-plugins-official": false
  }
}
```

#### 수동 설치 (고급 사용자용)

커스텀 Language Server를 사용하거나 공식 플러그인 외의 방식으로 LSP를 구성하려면 다음 단계를 따르세요.

⚠️ **중요**: LSP 플러그인은 Language Server와의 연결 설정만 제공합니다. Language Server 바이너리는 별도로 설치해야 합니다.

**1. Language Server 바이너리 설치**

| 플러그인             | Language Server            | 설치 명령어                                                                              |
|------------------|----------------------------|-------------------------------------------------------------------------------------|
| `pyright-lsp`    | Pyright (Python)           | `pip install pyright` 또는 `npm install -g pyright`                                   |
| `typescript-lsp` | TypeScript Language Server | `npm install -g typescript-language-server typescript`                              |
| `rust-lsp`       | rust-analyzer              | [rust-analyzer 설치 가이드](https://rust-analyzer.github.io/manual.html#installation) 참조 |

**2. 플러그인 설치**

마켓플레이스에서 해당 플러그인을 설치합니다:

```bash
/plugin install typescript-lsp
/reload-plugins
```

**3. `.lsp.json`으로 직접 설정 (플러그인 없이)**

플러그인을 사용하지 않고 직접 LSP 서버를 구성하려면 프로젝트 루트에 `.lsp.json` 파일을 생성합니다:

```json
{
  "typescript": {
    "command": "typescript-language-server",
    "args": [
      "--stdio"
    ],
    "extensionToLanguage": {
      ".ts": "typescript",
      ".tsx": "typescript"
    }
  }
}
```

- 필수 필드
    - `command`             : 실행할 LSP 바이너리 (PATH에 있어야 함)
    - `extensionToLanguage` : 파일 확장자를 언어 식별자로 매핑
- 선택적 필드
    - `args`                  : LSP 서버에 전달할 명령줄 인자
    - `env`                   : 환경 변수 설정
    - `initializationOptions` : 서버 초기화 옵션
    - `settings`              : `workspace/didChangeConfiguration`로 전달할 설정

> 자세한 내용은 [Plugins Reference - LSP Servers](https://code.claude.com/docs/en/plugins-reference#lsp-servers)를 참고하세요.

---

## 참고 자료

- https://code.claude.com/docs/en/changelog
- https://code.claude.com/docs/en/tools-reference#tools-reference
- https://code.claude.com/docs/en/discover-plugins#code-intelligence
- https://code.claude.com/docs/en/plugins-reference#lsp-servers
- https://code.claude.com/docs/en/plugins#add-lsp-servers-to-your-plugin
- https://code.claude.com/docs/en/settings#enabledplugins
