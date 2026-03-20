# Claude Code LSP 가이드

> Claude Code의 Language Server Protocol(LSP) 지원 기능을 소개하고 설정 방법을 안내합니다.

## 200토큰 요약

Claude Code v2.0.74 이상부터 LSP(Language Server Protocol)를 공식 지원합니다. LSP는 IDE와 언어 서버 간 표준 통신 프로토콜로, 기존 N×M 구조의 문제를 N+M 구조로
해결합니다. 이 문서에서는 LSP의 개념, Claude Code에서의 설정 방법, 사용 예시를 설명합니다.

---

## LSP란 무엇인가?

### LSP가 해결하는 문제

LSP(Language Server Protocol)는 IDE와 개발 도구가 프로그래밍 언어 지원 기능을 구현하는 방식을 표준화한 프로토콜입니다.

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

즉, IDE는 **컴파일러 수준의 코드 분석기**가 필요합니다.

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

## LSP 지원 기능

Claude Code는 LSP를 통해 다음과 같은 기능을 제공합니다:

| 기능                   | 설명            | LSP 메서드                           | 활용 예시                   |
|----------------------|---------------|-----------------------------------|-------------------------|
| **Go to Definition** | 심볼의 정의 위치로 이동 | `textDocument/definition`         | 함수가 어디서 선언되었는지 찾기       |
| **Find References**  | 심볼의 모든 참조 찾기  | `textDocument/references`         | 변수의 모든 사용처 검색           |
| **Hover 정보**         | 타입 및 문서 정보 표시 | `textDocument/hover`              | 변수 위에 마우스를 올려 타입 확인     |
| **Rename Symbol**    | 심볼 이름 일괄 변경   | `textDocument/rename`             | 변수명을 변경하면 모든 참조 자동 업데이트 |
| **Diagnostics**      | 실시간 오류 및 경고   | `textDocument/publishDiagnostics` | 코드 작성 중 문법 오류 감지        |

---

## Claude Code에서 LSP 설정하기

### 요구사항

- Claude Code v2.0.74 이상
- LSP 환경 변수 설정
- 언어별 Language Server 설치

### 1. 환경 변수 설정

LSP 기능을 활성화하려면 `ENABLE_LSP_TOOL` 환경 변수를 설정해야 합니다.

#### 방법 1: Claude Code 설정 파일 사용 (권장)

`.claude/settings.json` 파일에 환경 변수를 추가합니다:

```json
{
  "env": {
    "ENABLE_LSP_TOOL": "1"
  }
}
```

이 방법은 Claude Code 내에서만 환경 변수가 적용되므로 시스템 전체에 영향을 주지 않습니다.

#### 방법 2: 시스템 환경 변수 설정

터미널에서 일회성으로 설정:

```bash
export ENABLE_LSP_TOOL=1
```

영구적으로 설정하려면 쉘 설정 파일에 추가하세요:

```bash
# zsh 사용 시
echo 'export ENABLE_LSP_TOOL=1' >> ~/.zshrc

# bash 사용 시
echo 'export ENABLE_LSP_TOOL=1' >> ~/.bashrc
```

설정 후 터미널을 재시작하거나 `source ~/.zshrc` 명령어를 실행하세요.

### 2. 언어별 플러그인 설치

Claude Code CLI에서 다음 명령어를 실행하여 LSP 플러그인을 설치합니다:

```bash
/plugin install typescript-lsp@claude-plugins-official
/plugin install jdtls-lsp@claude-plugins-official
/plugin install kotlin-lsp@claude-plugins-official
/plugin install gopls-lsp@claude-plugins-official
/plugin install rust-analyzer-lsp@claude-plugins-official
```

설치 후 플러그인을 로드합니다:

```bash
/reload-plugins
```

### 3. 지원 언어 및 플러그인

| 언어                    | 플러그인 명령어                                                    | Language Server            |
|-----------------------|-------------------------------------------------------------|----------------------------|
| TypeScript/JavaScript | `/plugin install typescript-lsp@claude-plugins-official`    | typescript-language-server |
| Java                  | `/plugin install jdtls-lsp@claude-plugins-official`         | jdtls                      |
| Kotlin                | `/plugin install kotlin-lsp@claude-plugins-official`        | kotlin-language-server     |
| Python                | `/plugin install pyright-lsp@claude-plugins-official`       | pyright-langserver         |
| Go                    | `/plugin install gopls-lsp@claude-plugins-official`         | gopls                      |
| Rust                  | `/plugin install rust-analyzer-lsp@claude-plugins-official` | rust-analyzer              |
| C/C++                 | `/plugin install clangd-lsp@claude-plugins-official`        | clangd                     |

> **참고**: `claude-plugins-official` 플러그인을 사용하면 Language Server 바이너리가 **자동으로 설치**됩니다. 별도로 시스템에 바이너리를 설치할 필요가 없습니다.

#### 수동 설치가 필요한 경우

커스텀 Language Server 버전을 사용하거나, 공식 플러그인 외의 방식으로 LSP를 구성하려는 경우에만 아래 명령어로 수동 설치하세요:

| 언어         | 수동 설치 명령어                                                        |
|------------|------------------------------------------------------------------|
| TypeScript | `npm install -g typescript-language-server`                      |
| Java       | `brew install jdtls`                                             |
| Kotlin     | [공식 GitHub](https://github.com/fwcd/kotlin-language-server)에서 설치 |
| Python     | `pip install pyright`                                            |
| Go         | `go install golang.org/x/tools/gopls@latest`                     |
| Rust       | `rustup component add rust-analyzer`                             |
| C/C++      | LLVM 설치 (clangd 포함)                                              |

### 4. 설정 파일 (settings.json)

Claude Code 설정 파일에서 활성화할 LSP 플러그인을 지정합니다.

```json
{
  "enabledPlugins": {
    "typescript-lsp@claude-plugins-official": true,
    "jdtls-lsp@claude-plugins-official": true,
    "pyright-lsp@claude-plugins-official": true,
    "gopls-lsp@claude-plugins-official": true,
    "rust-analyzer-lsp@claude-plugins-official": true
  }
}
```

---

## 사용 예시

### Go 프로젝트에서 LSP 사용하기

```go
// main.go
package main

type User struct {
    ID   int
    Name string
}

func (u User) GetName() string {
    return u.Name
}

func main() {
    user := User{ID: 1, Name: "Alice"}
    println(user.GetName())  // 여기서 GetName()을 선택
}
```

1. `GetName()` 위에서 `/lsp definition` 실행 → 함수 정의로 이동
2. `GetName()` 위에서 `/lsp references` 실행 → 모든 사용처 표시
3. `User` 타입 위에서 Hover → 타입 정보 표시

### Java 프로젝트에서 LSP 사용하기

```java
// UserService.java
public class UserService {
	private UserRepository userRepository;

	public User findUser(Long id) {
		return userRepository.findById(id);  // findById에서 /lsp definition
	}
}
```

### TypeScript 프로젝트에서 LSP 사용하기

```typescript
// userService.ts
interface User {
    id: number;
    name: string;
}

class UserService {
    private users: User[] = [];

    findUser(id: number): User | undefined {
        return this.users.find(user => user.id === id);  // find에서 /lsp definition
    }

    addUser(user: User): void {
        this.users.push(user);
    }
}

const service = new UserService();
service.addUser({ id: 1, name: "Alice" });
```

1. `findUser()` 위에서 `/lsp definition` 실행 → 메서드 정의로 이동
2. `User` 인터페이스 위에서 Hover → 타입 정보 및 프로퍼티 표시
3. `users` 배열 위에서 `/lsp references` 실행 → 모든 참조 위치 표시

---

## 문제 해결

### LSP가 작동하지 않을 때

1. **환경 변수 확인**
   ```bash
   echo $ENABLE_LSP_TOOL  # 1이 출력되어야 함
   ```

2. **Language Server 설치 확인**
   ```bash
   which gopls  # 또는 해당 언어의 서버
   ```

3. **플러그인 활성화 확인**
   ```bash
   /plugin list  # 설치된 플러그인 목록 확인
   ```

4. **Claude Code 버전 확인**
   ```bash
   claude --version  # v2.0.74 이상 필요
   ```

### 자주 발생하는 오류

| 오류 메시지                      | 원인                   | 해결 방법                        |
|-----------------------------|----------------------|------------------------------|
| `LSP tool not enabled`      | 환경 변수 미설정            | `export ENABLE_LSP_TOOL=1`   |
| `Language server not found` | 바이너리 미설치 또는 PATH 미등록 | Language Server 설치 및 PATH 설정 |
| `Connection refused`        | Language Server 미실행  | IDE 재시작 또는 수동 서버 실행          |

---

## 참고 자료

- [Language Server Protocol 공식 문서](https://microsoft.github.io/language-server-protocol/)
- [Claude Code 공식 문서 - Settings](https://code.claude.com/docs/en/settings#tools-available-to-claude)
- [LSP 구현체 목록](https://microsoft.github.io/language-server-protocol/implementors/servers/)
