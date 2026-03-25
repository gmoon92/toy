# Claude Code LSP 가이드

Claude Code는 플러그인 시스템을 통해 LSP(Language Server Protocol) 기능을 제공합니다.

이 문서에서는 LSP의 개념, Claude Code에서의 플러그인 설치 및 설정 방법, 사용 예시를 설명합니다.

---

## LSP 설정하기

Claude Code는 [v2.0.74(2025년 12월 19일)](https://github.com/anthropics/claude-code/releases/tag/v2.0.74) 버전부터 공식적으로 LSP 기능을 추가했습니다.

### LSP 플러그인 설치

Claude Code는 공식 마켓플레이스 `claude-plugins-official`에서 언어별 [LSP 플러그인](https://code.claude.com/docs/en/discover-plugins#code-intelligence)을 제공합니다.

```bash
claude plugin install kotlin-lsp@claude-plugins-official \
  & claude plugin install jdtls-lsp@claude-plugins-official
```

- Kotlin: kotlin-lsp@claude-plugins-official
- Java: jdtls-lsp@claude-plugins-official

LSP 기능은 Language Server 바이너리가 필요하지만, 공식 플러그인은 Language Server 바이너리가 **자동으로 설치**됩니다. 별도로 시스템에 바이너리를 설치할 필요가 없습니다.

> **참고**: Claude Code CLI 낵부에서도 `/plugin install`로 설치할 수 있습니다. 설치 후 `/reload-plugins`로 적용하세요.
>
>```bash
>/plugin install kotlin-lsp@claude-plugins-official
>/plugin install jdtls-lsp@claude-plugins-official
>
>/plugin list
>
>/reload-plugins
>```

### 플러그인 활성화

공식 플러그인은 설치 후 기본적으로 **활성화 상태**입니다.

특정 플러그인을 비활성화하려면 `settings.json`의 [`enabledPlugins`](https://code.claude.com/docs/en/settings#enabledplugins)에 `false`로 설정합니다.

```json
{
  "enabledPlugins": {
    "typescript-lsp@claude-plugins-official": false
  }
}
```

### 커스텀 LSP 서버 설정

시스템에 직접 설치한 LSP 서버를 사용하려면 프로젝트 루트에 `plugin.json` 또는 `.lsp.json` 파일을 생성하여 LSP 서버를 연결해야 합니다.

**1. LSP 서버 바이너리 설치**

| 언어         | Language Server            | 설치 명령어                                                                                                   |
|------------|----------------------------|----------------------------------------------------------------------------------------------------------|
| Python     | Pyright                    | `pip install pyright` 또는 `npm install -g pyright`                                                        |
| TypeScript | TypeScript Language Server | `npm install -g typescript-language-server typescript`                                                   |
| Rust       | rust-analyzer              | [rust-analyzer 설치 가이드](https://rust-analyzer.github.io/manual.html#installation) 참조                      |
| Java       | Eclipse JDTLS              | `brew install jdtls` 또는 [jdtls 설치 가이드](https://github.com/eclipse/eclipse.jdt.ls) 참조                     |
| Kotlin     | Kotlin Language Server     | `brew install kotlin-language-server` 또는 [kls 설치 가이드](https://github.com/fwcd/kotlin-language-server) 참조 |

**2. 플러그인 설정 파일 생성**

프로젝트 루트에 `plugin.json` 또는 `.lsp.json` 파일을 생성하여 LSP 서버를 연결합니다:

```json
{
  "go": {
    "command": "gopls",
    "args": ["serve"],
    "extensionToLanguage": {
      ".go": "go"
    }
  }
}
```

- `command`: 실행할 LSP 바이너리 (PATH에 있어야 함)
- `extensionToLanguage`: 파일 확장자를 언어 식별자로 매핑

> `command`, `extensionToLanguage`는 필수 필드입니다.
>
> 자세한 설정은 [Plugins Reference - LSP Servers](https://code.claude.com/docs/en/plugins-reference#lsp-servers)를 참고하세요.

---

## LSP란 무엇인가?

LSP(Language Server Protocol)는 IDE와 개발 도구가 프로그래밍 언어 지원 기능을 구현하는 방식을 표준화한 프로토콜입니다.

#### 기존 방식의 문제 (N×M 구조)

기존에는 IDE마다 각 프로그래밍 언어에 대한 분석 기능을 직접 구현해야 했습니다.

| IDE      | Java 지원 | Python 지원 | Go 지원 | Rust 지원 |
|----------|---------|-----------|-------|---------|
| IntelliJ | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
| Eclipse  | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
| VSCode   | 별도 구현   | 별도 구현     | 별도 구현 | 별도 구현   |
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

- 기존: `IDE × 언어` 개의 구현 필요
- LSP 이후: `IDE + 언어` 개의 구현 필요

즉, **Language Server는 컴파일러 수준의 코드 분석기**입니다.

---

## LSP가 제공하는 가치

클로드 코드는 `grep` 기반으로 우선 적용하여 파일 탐색합니다.

타입 추론 또는 타입이 실제 사용하는 위치 변수 사용처 등 알 수 없습니다.

따라서 소스 코드 리팩토링 또는 분석 작업 요청시 누락된 부분으로 인해 컴파일 에러 또는 자세히 분석을 못한다고 느꼈을 텐데,
이점 lsp 를 연동하여 사용하여 누락된 파일없이 완벽하게 해소 시킬 수 있습니다.

### 왜 단순 검색으로는 부족한가?

IDE 기능은 AST(Abstract Syntax Tree, 추상 구문 트리), `symbol` `graph`, `type` `system` 등의 깊이 있는 코드 분석이 필요합니다.

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

- 타입 오류 및 경고 자동 보고: 코드 저장 후 문법/타입 오류 감지
- 코드 네비게이션: 정의로 이동, 참조 찾기 및 호버 정보
  - 심볼의 정의 위치로 이동: 함수/클스 선언 위치 찾기
  - 심볼의 모든 참조 찾기: 변수/메서드 사용처 검색
  - 타입 및 문서 정보 표시: 변수/함수 위에서 타입 확인
  - 파일의 모든 심볼 목록: 클래스 멤버, 함수 목록 보기
  - 인터페이스/추상메서드 구현체 찾기: `Repository`의 구현체 찾기
  - 함수 호출 계층 추적: 호출 관계 및 의존성 분석
- 언어 인식: 코드 기호에 대한 타입 정보 및 문서

> [Tools Reference - LSP](https://code.claude.com/docs/en/tools-reference)
> ```
> Code intelligence via language servers. Reports type errors and warnings automatically after file edits. Also supports
> navigation operations: jump to definitions, find references, get type info, list symbols, find implementations, trace
> call hierarchies. Requires a code intelligence plugin and its language server binary.
> ```

### grep vs LSP: 코드 리팩토링

아래 `OrderService` 클래스 하나로 grep의 한계와 LSP의 강점을 비교합니다.

```java
@Service
public class OrderService {
    private final OrderRepository repository;
    private final PaymentProcessor processor;

    public Order process(Long orderId) {
        Order order = repository.findById(orderId).orElseThrow();
        return process(order);  // 메서드 오버로딩
    }

    public Order process(Order order) {
        // 변수 shadowing: 파라미터 'order'가 필드/메서드명과 중복
        List<Item> items = order.getItems();
        Payment payment = processor.process(order.getTotal());
        return save(order, payment);
    }

    private Order save(Order order, Payment payment) {
        // 제네릭 타입: List<Order> vs List<Payment>
        List<Order> saved = repository.saveAll(List.of(order));
        return saved.get(0);
    }
}
```

#### 시나리오별 비교

| 상황               | grep 한계                                    | LSP 강점                                                    |
|------------------|--------------------------------------------|-----------------------------------------------------------|
| **변수 Shadowing** | `grep "order"` → 필드, 파라미터, 변수, 메서드명 모두 검색됨 | 정확한 스코프 구분. 파라미터 `order`만 선택 가능                           |
| **메서드 오버로딩**     | `process(Long)` vs `process(Order)` 구분 불가  | 타입 시그니처 기반 정확한 참조 분리                                      |
| **인터페이스 다형성**    | `processor.process()` 호출 지점만 찾음            | `Find Implementations`로 실제 구현체(`CardPaymentProcessor`) 이동 |
| **제네릭 타입**       | `List` 검색 시 타입 정보 없음                       | `List<Order>` vs `List<Payment>` 정확히 구분                   |

---

## 참고 자료

- https://code.claude.com/docs/en/changelog
- https://code.claude.com/docs/en/tools-reference#tools-reference
- https://code.claude.com/docs/en/discover-plugins#code-intelligence
- https://code.claude.com/docs/en/plugins-reference#lsp-servers
- https://code.claude.com/docs/en/plugins#add-lsp-servers-to-your-plugin
- https://code.claude.com/docs/en/settings#enabledplugins
