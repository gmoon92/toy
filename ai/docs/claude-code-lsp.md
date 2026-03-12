# Claude Code LSP

Claude Code v2.0.74부터 Language Server Protocol(LSP) 이 기본 지원됩니다.

LSP(Language Server Protocol) 설정 방법 가이드 문서입니다.

## 설정 방법

https://spec-weave.com/docs/guides/lsp-integration/?utm_source=chatgpt.com

----

## LSP 가 뭔데?

LSP 는 이미 우리가 사용하고 있습니다.

이클립스나 인텔리제이 와 같은 ide 에선 lsp 를 사용하여 리팩토링이나 타입 추론을 심볼릭 판단을 지원합니다.

IDE에서 **Language Server Protocol (LSP)** 가 등장한 배경은 단순히 “grep보다 빠르다”가 아니라, **IDE 기능을 구현하는 구조적 문제** 때문이었습니다.
핵심은 **언어별 분석 로직이 IDE마다 중복 구현되는 문제**였습니다.

아래는 실제 배경을 기술 문서 기반으로 정리한 것입니다.

---

# 1️⃣ LSP 등장 이전 IDE 구조 문제

LSP 이전에는 IDE가 **각 프로그래밍 언어 지원 기능을 직접 구현**했습니다.

예:

| IDE                | 언어         | 구현 방식     |
|--------------------|------------|-----------|
| Visual Studio Code | TypeScript | IDE 내부 구현 |
| Eclipse IDE        | Java       | 플러그인      |
| IntelliJ IDEA      | Python     | 자체 플러그인   |
| Atom (text editor) | Go         | 별도 패키지    |

문제는 다음이었습니다.

### 동일 기능을 IDE마다 다시 개발

예:
“Go to Definition” 기능

```
IDE A → Java parser 구현
IDE B → Java parser 구현
IDE C → Java parser 구현
```

즉

```
IDE × 언어 수 만큼 구현 필요
```

Microsoft가 LSP 발표에서 직접 설명한 문제입니다.

> Every language needed a custom integration with every development tool.

출처

* [https://microsoft.github.io/language-server-protocol/](https://microsoft.github.io/language-server-protocol/)

---

# 2️⃣ 코드 분석 기능 구현 난이도 문제

IDE 기능은 단순 텍스트 검색으로는 구현이 어렵습니다.

대표적인 IDE 기능

| 기능                | 필요한 정보             |
|-------------------|--------------------|
| Go to Definition  | AST                |
| Find References   | symbol graph       |
| Rename Symbol     | reference tracking |
| Auto Completion   | type system        |
| Error Diagnostics | compiler           |

즉 IDE는 **컴파일러 수준의 코드 분석기**가 필요합니다.

예

```
class UserService {
   private UserRepository repo;
}
```

grep으로는 해결 불가

```
grep repo
```

문제

* 변수 shadowing
* import alias
* overload
* generic type
* inheritance

그래서 IDE들은 **언어별 분석 엔진**을 만들어야 했습니다.

---

# 3️⃣ IDE ↔ 언어 지원의 조합 폭발 문제

Microsoft가 설명한 핵심 문제는 **N × M 문제**였습니다.

```
IDE 수 × 언어 수
```

예

| IDE     | Java | Python | Go | Rust |
|---------|------|--------|----|------|
| VSCode  | 구현   | 구현     | 구현 | 구현   |
| Eclipse | 구현   | 구현     | 구현 | 구현   |
| Atom    | 구현   | 구현     | 구현 | 구현   |

즉

```
IDE마다 언어 지원을 다시 만들어야 함
```

Microsoft 문서 설명:

> Without a common protocol, each language had to implement integration for every editor.

출처

* [https://microsoft.github.io/language-server-protocol/](https://microsoft.github.io/language-server-protocol/)

---

# 4️⃣ 해결책: LSP 구조

Microsoft는 다음 구조를 제안했습니다.

```
IDE
 ↓
Language Server Protocol
 ↓
Language Server
```

구조

| 구성              | 역할      |
|-----------------|---------|
| Editor          | UI      |
| LSP             | 통신 프로토콜 |
| Language Server | 코드 분석   |

예

| 언어         | Language Server |
|------------|-----------------|
| TypeScript | tsserver        |
| Python     | pylsp           |
| Go         | gopls           |
| Rust       | rust-analyzer   |

IDE는 단순히 **프로토콜 클라이언트**만 구현하면 됩니다.

---

# 5️⃣ LSP 구조 장점

### 1️⃣ 구현 중복 제거

기존

```
IDE × 언어
```

LSP 이후

```
IDE + Language Server
```

즉

```
N + M 구조
```

---

### 2️⃣ 언어 개발자가 직접 IDE 지원 가능

예

* Rust → rust-analyzer
* Go → gopls
* TypeScript → tsserver

IDE가 아니라 **언어 팀이 유지보수**합니다.

---

### 3️⃣ IDE 기능 통합

가능한 기능

| 기능               | LSP 메서드                         |
|------------------|---------------------------------|
| Go to Definition | textDocument/definition         |
| Find References  | textDocument/references         |
| Hover            | textDocument/hover              |
| Rename           | textDocument/rename             |
| Diagnostics      | textDocument/publishDiagnostics |

---

# 6️⃣ LSP 등장 시점

Language Server Protocol 은 **2016년 Microsoft**가 발표했습니다.

목적

> Standardize communication between editors and language servers.

출처

* [https://microsoft.github.io/language-server-protocol/](https://microsoft.github.io/language-server-protocol/)

---

# 7️⃣ 실제 IDE 적용 사례

대표 IDE들은 대부분 LSP 기반입니다.

| IDE                | LSP 지원         |
|--------------------|----------------|
| Visual Studio Code | 기본 구조          |
| Neovim             | builtin client |
| Emacs              | lsp-mode       |
| Sublime Text       | LSP plugin     |

---

# 8️⃣ 정리 (도입 배경 핵심)

LSP가 등장한 이유는 **grep 속도 문제가 아니라 구조 문제**였습니다.

기존

```
IDE × 언어
중복 구현 폭발
```

문제

* IDE마다 언어 분석기 구현
* 유지보수 비용 증가
* 기능 불일치

해결

```
IDE
 ↓
LSP
 ↓
Language Server
```

결과

* 언어 분석 로직 공유
* IDE 기능 표준화
* 확장성 증가

---

💡 참고로 재미있는 점 하나 있습니다.

요즘 **AI 코드 도구(Claude Code, Cursor)** 가 LSP를 쓰는 이유도 **IDE와 동일한 문제 때문**입니다.

AI가 코드 리팩토링하려면

```
grep
```

이 아니라

```
symbol graph
AST
reference graph
```

가 필요하기 때문입니다.

---

원하시면 제가 **“왜 grep 기반 AI 코드 분석이 실패하는지 (실제 사례)”**도 설명해드리겠습니다.
이거 이해하면 **왜 AI coding tool이 LSP에 집착하는지** 정확히 보입니다.


---

## Grep vs LSP 900 배 빠르다?

파일 탐색 속도는
Claude Code 2.0.74 버전에서 **LSP(Language Server Protocol) 도구**가 추가

- [Claude Code v2.0.74 boosts navigation 900x with LSP support](https://www.linkedin.com/posts/farivar-tabatabaei_claudecode-lsp-aicoding-activity-7413201214047297536-raSw?utm_source=chatgpt.com)
- [github anthropics - VSCode LSP API 활성화: 코드 탐색 성능 100~1000배 향상](https://github.com/anthropics/claude-code/issues/5495?utm_source=chatgpt.com)

| 작업        | 기존 방식 (grep 기반) | LSP 기반 |
|-----------|-----------------|--------|
| 함수 정의 찾기  | 15~30초          | < 1초   |
| 함수 사용처 찾기 | 약 45초           | 약 50ms |
| 전체 코드 탐색  | 수십 초            | 즉시     |

### serena 쓰지마세요.

serena 가 왜 인기가 있었나

serena mcp 의 주안점은
심볼릭으로 탐색하여 기존 대비 토큰 비용 감소와 빠른 응답이 가장 큰 장점으로 많은 개발자가 초기에 세레나 mcp 서버를 연동하여 사용했다.

하지만 lsp 를 공식적으로 지원하니 이제는 mcp 연동 대신 클로드 코드에서

```
ENABLE_LSP_TOOL=1

#https://code.claude.com/docs/en/discover-plugins#code-intelligence

/plugin install jdtls-lsp@claude-plugins-official
/plugin install kotlin-lsp@claude-plugins-official
/plugin install typescript-lsp@claude-plugins-official

/reload-plugins
```

---
|Language| Plugin| Binary| required|
---

| C/C++       | 	clangd-lsp        | 	clangd                     |
|-------------|--------------------|-----------------------------|
| C#	         | csharp-lsp	        | csharp-ls                   |
| Go	         | gopls-lsp	         | gopls                       |
| Java        | 	jdtls-lsp	        | jdtls                       |
| Kotlin      | 	kotlin-lsp        | 	kotlin-language-server     |
| Lua         | 	lua-lsp	          | lua-language-server         |
| PHP         | 	php-lsp	          | intelephense                |
| Python      | 	pyright-lsp       | 	pyright-langserver         |
| Rust        | 	rust-analyzer-lsp | 	rust-analyzer              |
| Swift	      | swift-lsp	         | sourcekit-lsp               |
| TypeScript	 | typescript-lsp     | 	typescript-language-server |

- https://code.claude.com/docs/en/discover-plugins#code-intelligence
- settings.json:
    ```
    {
      "enabledPlugins": {
        "typescript-lsp@claude-plugins-official": true
      }
    }
    ```

## Reference

- [Claude Code - Settings](https://code.claude.com/docs/en/settings)
- [Claude Code - Tools available to claude](https://code.claude.com/docs/en/settings#tools-available-to-claude)
- [Claude Code - Code intelligence](https://code.claude.com/docs/en/discover-plugins#code-intelligence)
