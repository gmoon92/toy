---
title: DPoP 기반 실시간 릴레이 인증 아키텍처
description: 비(非)HTTP 실시간 채널(MQTT·WebSocket·TCP)에서 무상태 JWS 검증을 DPoP 소유 증명으로 구성하는 토큰 인증 설계 가이드.
---

# DPoP 기반 실시간 릴레이 인증 아키텍처

> 비(非)HTTP 실시간 채널(MQTT·WebSocket·TCP)에서 **무상태 JWS 검증**을 유지하면서, **DPoP 소유 증명(RFC 9449)**으로 토큰을 발신자에 구속(sender-constrained)하는 설계 가이드

## 목적

1. **발급 서버가 서명한 JWS를 검증자가 무상태로 검증**하는 단방향 비대칭키 구조에서, **DPoP 소유 증명(단말 키 바인딩)**을 적용하는 아키텍처를 정의한다.
2. 그 전제가 되는 **토큰 생태계 표준 기술**(JWT·JWS·JWE·JWK·Base64Url)을 실제 통신 예시와 함께 정의해, 실무자가 즉시 참고할 수 있게 한다.
3. **DPoP에서 차용하는 것은 `cnf` 키 바인딩 아이디어(RFC 7800/9449)**이며, DPoP의 HTTP 전용 증명 형식(`htm`/`htu` 요청 바인딩, 서버 `DPoP-Nonce` 왕복)은 쓰지 않는다. 이 아키텍처의 핵심은 **바인딩(cnf)을 "토큰 발급 시점"에 이미 확정**해 두어, 중계가 **키 협상·challenge-response 왕복(핸드셰이크) 없이** 무상태로 이중 검증만 하면 되도록 만드는 것이다. 데이터 중계는 순수 TCP(헤더 없음)이므로 토큰·Proof는 **연결 최초 프레임(바이트 스트림)에 1회 실어** 보낸다.

## ToC

**Part I. 토큰 생태계 표준 개념**

1. JWT·JWS·JWE·JWK 한눈에
2. JWT - 데이터 구조와 형식 (Base64Url 포함)
3. JWK - 키 표현 규격
4. JWS - 서명(변조 방지)
5. JWE - 암호화(기밀성)

**Part II. 무상태 검증 구조와 서버 역할**

6. 서버 역할 분리 (시그널링 vs 데이터 중계)
7. 현재 구조와 그 이유 (무상태·초고속 검증)

**Part III. DPoP 소유 증명 (cnf 키 바인딩)**

8. cnf 키 바인딩 소유 증명 개념
9. 왕복 없는 무상태 이중 검증
10. 세션 수립 6단계 시퀀스
11. 복제 리플레이 방어 (단일사용 redeem)
12. 역할별 구현 (발급 서버·검증자·클라이언트)

---

# Part I · 토큰 생태계 표준 개념

## 1. JWT · JWS · JWE · JWK

먼저 용어 정리부터 하겠습니다.

**JWT는 "정보 전달 형식"이라는 상위 개념**이고,
JWS와 JWE는 그 JWT를 실제로 만드는 두 가지 구현 방식이며, JWK는 서명·암호화에 쓰는 **키 자체의 표현 규격**입니다.

| 용어       | 정식 명칭               | 역할                            | 보장하는 것   | 비유      |
|----------|---------------------|-------------------------------|----------|---------|
| **JWT**  | JSON Web Token      | 정보 전달 표준 문자열 (JWS 또는 JWE로 구현) | -        | 택배 상자   |
| **JWS**  | JSON Web Signature  | 내용은 **공개**, 서명으로 위변조만 방지      | 무결성 · 진위 | 봉인 스티커  |
| **JWE**  | JSON Web Encryption | Payload 자체를 **암호화**           | 기밀성      | 금고 상자   |
| **JWK**  | JSON Web Key        | 키를 JSON으로 표현하는 규격             | -        | 열쇠 그 자체 |
| **JWKS** | JWK Set             | 공개키 JWK 여러 개의 묶음 (키 롤링용)      | -        | 열쇠 꾸러미  |

> **흔한 오해:** "JWT = JWS"가 아닙니다. JWT라는 개념이 먼저 있고, 그것을 *서명만 하면* JWS, *암호화하면* JWE입니다. 이 아키텍처가 쓰는 것은 정확히는 **JWS**(서명만, 내용은 평문 공개)입니다.

## 2. JWT - 정보 전달의 "그릇": 데이터 구조와 형식

§1에서 봤듯 JWT은 그 자체로 서명이나 암호화를 하지 않습니다. 클레임(정보)을 담아 **어디에 실어도 안전한 문자열 하나로 직렬화하는 "형식(format)"**이며, 실제 서명은 JWS(§4)가, 암호화는 JWE(§5)가 담당합니다. 그런데 JWS든 JWE든 **"JSON을 Base64Url로 인코딩해 점(`.`)으로 잇는다"는 직렬화 원리는 동일**합니다. 그래서 이 공통 형식을 먼저 세우고, 뒤에서 서명(§4)·암호화(§5)의 차이만 다루겠습니다.

### 파트를 점(`.`)으로 이어붙인 단일 문자열

JWT은 여러 파트로 나뉘며, 각 파트는 내부적으로 JSON 형태를 띠지만 **주고받을 때 JSON의 괄호 `{ }` 모양 그대로 전달되지 않습니다.** 실제로는 각 파트를 Base64Url로 인코딩한 뒤 마침표(`.`)로 이어붙인 **하나의 문자열**로 최종 발급됩니다.

| 구현 | 파트 수 | 형태 |
|------|--------|------|
| **JWS**(서명) | 3부분 (점 2개) | `Base64Url(Header).Base64Url(Payload).Base64Url(Signature)` |
| **JWE**(암호화) | 5부분 (점 4개) | `Base64Url(Header).…(EncryptedKey).…(IV).…(Ciphertext).…(AuthTag)` (§5) |

아래는 실제 HTTP 헤더에 실린 JWS 형태 JWT입니다(전부 한 줄).

```http
GET /connect HTTP/1.1
Host: relay.example.com
Authorization: Bearer eyJhbGciOiJFUzI1NiIsImtpZCI6Imlzc3Vlci0yMDI2LTA3LWEifQ.eyJzdWIiOiJzdmM6OldlYlNlcnZlciIsImV4cCI6MTc1MTQxNDcwMH0.MEUCIQDx7k2...
                       └                    Header                  ─┘ └────                   Payload                  ────┘ └─  Sig   ─┘  (전부 한 줄)
```

### JSON → Base64Url → HTTP: 어떻게 문자열이 되나

```mermaid
flowchart TD
    A["<b>1단계 · 원본 JSON</b><br/>Header { alg, kid } · Payload { sub, exp } · Signature(원시 바이트)"]
    B["<b>2단계 · 각 파트를 Base64Url 문자열로</b><br/>eyJhbGci... · eyJzdWI... · MEUCIQDx7k2..."]
    C["<b>3단계 · 마침표(.)로 결합한 최종 문자열</b><br/>eyJhbGci....eyJzdWI....MEUCIQDx7k2...  → HTTP 헤더에 실림"]
    A -->|" 각 파트 Base64Url 인코딩 "| B
    B -->|" 마침표(.)로 결합 "| C
```

### Payload는 "숨겨지지" 않는다 — 인코딩 ≠ 암호화

여기서 가장 중요한 점: **Payload는 암호화되지 않습니다.** Base64Url은 암호화가 아니라 **인코딩**일 뿐이라, 누구나 즉시 디코딩해 그 안의 내용을 읽어볼 수 있습니다. `eyJ...`로 보여 암호화된 듯하지만 착각입니다. 그래서 (§4에서 볼) **JWS의 서명은 데이터를 숨기기 위함이 아니라 오직 내용의 "위변조 여부"를 증명**하는 역할을 합니다. 내용을 정말 숨겨야 한다면 Payload 자체를 암호화하는 JWE(§5)를 써야 합니다.

### Base64Url을 쓰는 이유 - URL·헤더에서 안전

JWT은 URL·HTTP 헤더·쿠키에 실려 다닙니다. 그런데 일반 Base64가 쓰는 `+` `/` `=` 세 문자는 이들 맥락에서 **특수 의미**를 가져 깨집니다. Base64Url은 이를 치환해 해결합니다.

| 문자   | 일반 Base64 | Base64Url | 치환 이유                      |
|------|-----------|-----------|----------------------------|
| 62번째 | `+`       | `-`       | URL에서 `+`는 공백(space)으로 해석됨 |
| 63번째 | `/`       | `_`       | `/`는 URL 경로 구분자로 오인됨       |
| 패딩   | `=`       | **제거**    | `=`는 쿼리스트링 `key=value`와 충돌 |

```text
// 같은 원본, 다른 인코딩
원본 바이트 → 일반 Base64 :  "a+b/c8=="
원본 바이트 → Base64Url   :  "a-b_c8"   (+→-, /→_, 패딩= 삭제)
```

> 💡 **왜 굳이 인코딩을 거치나? - HTTP 헤더·URL에 안전하게 싣기 위함이다.** JSON 원문에는 `{` `}` `"` 공백·줄바꿈 등 헤더·쿼리 파라미터에서 깨지거나 재해석되는 문자가 많습니다. 토큰은 `Authorization` 헤더나 쿼리스트링에 실려야 하므로, **어느 맥락에 넣어도 안전한 문자 집합(`A–Z a–z 0–9 - _`)으로 먼저 변환**해 두는 것입니다.

> ⚠️ **주의:** Base64Url은 **인코딩**일 뿐 **암호화가 아닙니다.** Payload가 `eyJ...`로 보여 암호화된 듯하지만 누구나 즉시 디코딩해 읽을 수 있습니다(위 "Payload는 숨겨지지 않는다" 참조).

> ↳ 단, JWK/JWKS(§3)는 헤더가 아니라 **응답 바디(JSON)**로 오가므로 인코딩이 필요 없어 평문 그대로 전송됩니다.

## [3. JWK: 키를 표현하는 순수 JSON 규격](https://datatracker.ietf.org/doc/html/rfc7517)

JWK는 공개키/개인키를 **인코딩 없이 평문 JSON**으로 표현합니다.

검증자(시그널링 서버, Resource Server)는 발급자(발급 서버, 인증 서버)의 **공개키 JWK**를 받아 서명을 검증합니다.

### [JWK Format: 주요 필드](https://datatracker.ietf.org/doc/html/rfc7517#section-4)

```json
{
  "kty": "EC",
  "kid": "issuer-2026-07-UUID",
  "crv": "P-256",
  "alg": "ES256",
  "use": "sig",
  "x": "f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU",
  "y": "x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0"
}
```

_Example 공개키 JWK 예시 (검증자가 받는 것): EC P-256 기준_

| 필드       | 의미                                          | 예시 값                           |
|----------|---------------------------------------------|--------------------------------|
| `kty`    | Key Type, 키 유형                              | `EC` (타원곡선), `RSA`, `oct`(대칭키) |
| `kid`    | Key ID, 키 식별자. 키 롤링 시 어떤 키로 검증할지 지목         | `"issuer-2026-07-UUID"`        |
| `crv`    | Curve, 타원곡선 종류 (kty=EC일 때)                  | `"P-256"`                      |
| `alg`    | 알고리즘                                        | `"ES256"`                      |
| `use`    | 공개 키 용도<br/> - `sig`: 서명 <br/> - `enc`: 암호화 | `"sig"`                        |
| `x`, `y` | 공개키 좌표 (Base64Url 인코딩된 곡선 위의 점)             | `"f83OJ3D2..."`                |
| `d`      | **개인키** 값. 공개키 JWK에는 **절대 포함 금지**           | 발급 서버만 보유                      |

발급 서버는 `ECKeyGenerator(Curve.P_256)` 류의 API로 EC 키쌍을 생성하고 `kid`를 부여합니다.
다수의 공개키 JWK를 배열로 묶은 것이 **JWKS**이며, 키 롤링시 신, 구 키를 함께 노출해 무중단 교체를 가능케 합니다.


> `EC P-256` 란 타원곡선 암호화(ECC)의 표준
> - [RFC 5349: ECC(타원 곡선 암호화)](https://datatracker.ietf.org/doc/html/rfc5349)

```http
GET /.well-known/jwks.json HTTP/1.1

HTTP/1.1 200 OK
Content-Type: application/json

{
    "keys": [
        {
            "kty":"EC", "crv":"P-256",
            "kid":"issuer-2026-07-a",
            "x":"f83OJ...",
            "y":"x_FEz..."
        }
    ]
}
```

- JWK/JWKS는 인코딩 없이 "평문 JSON 그대로" 전송된다.
    - 응답: Base64Url 아님, JSON 평문 그대로
    - JWS, JWE는 전송 시 Base64Url 문자열 덩어리(`eyJ...`)로 변환되지만, **JWK와 JWKS는 예외**다.
- 검증자가 공개키를 가져올 때 **순수 JSON 본문 그대로** 응답으로 받는다. (공개키는 애초에 공개돼도 되는 정보라 숨길 이유가 없다.)

## 4. JWS - 서명 메커니즘: 내용은 투명, 위변조만 방지

§2의 JWT 형식을 **"서명"으로 구현**한 것이 JWS입니다(3부분 구조·Base64Url 직렬화는 §2 참조). Payload는 공개되어 누구나 읽을 수 있고(§2), 서명은 오직 **무결성·진위(위변조 방지)**만 보장합니다. 이 아키텍처가 쓰는 것이 바로 이 JWS입니다.

세 파트가 실제로 담는 내용은 다음과 같습니다.

- Header - 서명 알고리즘과 키 지목
    ```json
    // kid 로 "JWKS 중 어떤 공개키로 검증하라"를 지목. JWK 전체가 아니라 kid만 넣는다.
    {
      "alg": "ES256",
      "typ": "JWT",
      "kid": "issuer-2026-07-a"
    }
    ```
- Payload - 클레임(Claim) 집합
    ```json
    {
      "iss": "https://issuer.example.com",  // 발급자
      "sub": "svc::WebServer",              // 주체(역할)
      "authority": { "sub": ["topic/..."], "pub": ["topic/..."] },
      "iat": 1751414400,            // 발급 시각
      "exp": 1751414700             // 만료 시각
    }
    ```
- Signature - 무결성 증명
    ```text
    // 검증자는 공개키(x,y)로 이 서명을 검증. 한 글자라도 바뀌면 검증 실패.
    Signature = ECDSA_SHA256(
        Base64Url(Header) + "." + Base64Url(Payload),
        발급 서버의 개인키(d)
    )
    ```

> 검증자(시그널링 서버)는 §3에서 받은 **공개키 JWK**를 `kid`로 골라 서명을 검증합니다. Payload를 한 글자라도 바꾸면 서명이 깨져 즉시 탐지되지만, **내용을 읽는 것 자체는 막지 못합니다**(그것이 JWS의 설계 의도). 최종적으로 `eyJ....eyJ....MEUC...` 형태의 단일 문자열로 직렬화되는 과정은 §2를 참조하세요.

## 5. JWE - Payload 자체를 암호화 (기밀성)

JWE는 내용을 **암호화**해 기밀성을 보장합니다. 이는 전송 계층 암호화(TLS)와는 별개인 **애플리케이션 계층 암호화**로, 중간 저장소나 로그에 남아도 내용이 노출되지 않습니다. 구조는 마침표로 구분된 **5단**입니다.

```mermaid
flowchart LR
    H["①Header"] --- K["②EncryptedKey"] --- I["③IV"] --- CT["④Ciphertext"] --- T["⑤AuthTag"]
```

| 단 | 구성요소               | 설명                                        |
|---|--------------------|-------------------------------------------|
| ① | JOSE Header        | `alg`(키 암호화 방식) · `enc`(본문 암호화 방식)        |
| ② | Encrypted Key      | 본문 암호화에 쓸 **대칭키(CEK)**를 수신자 **공개키**로 감싼 것 |
| ③ | IV                 | 초기화 벡터 (동일 평문의 반복 암호화 방지)                 |
| ④ | Ciphertext         | 대칭키로 암호화된 실제 Payload                      |
| ⑤ | Authentication Tag | 암호문 변조 탐지용 무결성 태그 (AEAD)                  |

### 대칭키 · 비대칭키 혼합(Hybrid) 원리

> 💡 **왜 섞는가?** 비대칭 암호화는 느리고 큰 데이터에 부적합, 대칭 암호화는 빠르지만 키 공유가 어렵습니다. JWE는 둘의 장점만 취합니다.
> 1. 본문은 임의 생성한 **대칭키(CEK)**로 빠르게 암호화 (단 ④)
> 2. 그 대칭키를 수신자 **공개키(비대칭)**로 안전하게 감싸 전달 (단 ②)
> 3. 수신자는 자신의 **개인키**로 대칭키를 풀고 → 대칭키로 본문 복호화

```json
// JWE Header(①) 원본 JSON - RSA로 대칭키를 감싸고, 본문은 AES-GCM으로 암호화
{
  "alg": "RSA-OAEP-256",
  "enc": "A256GCM",
  "kid": "verifier-enc-01"
}
```

**JWS와 다른 점:** JWS는 점 **2개(3부분)**, JWE는 점 **4개(5부분)**. 하지만 §2의 직렬화 원리(JSON → Base64Url → 점 결합)는 동일합니다. 차이는 JWE의 ④Ciphertext가 암호화돼 있어 **Base64Url을 풀어도 원본을 읽을 수 없다**(JWS Payload는 풀면 바로 읽힘)는 것뿐입니다.

이 아키텍처는 JWS(서명)만 사용하며 JWE는 쓰지 않습니다. 권한 정보는 민감정보가 아니고 TLS로 전송 구간이 보호되기 때문입니다. JWE는 향후 Payload에 기밀 데이터를 실어야 할 경우의 선택지로 남겨둡니다.

---

# Part II · 무상태 검증 구조와 서버 역할

## 6. 서버 역할 분리 - 시그널링 vs 데이터 중계

> 🔴 **역할 정의:** 이 문서에서 인증 관련 세 서버 역할은 다음과 같이 부릅니다.
> · **발급 서버(Issuer)** = 개인키 보유 = 토큰 서명·발급 (OAuth의 Authorization Server. 통상 백엔드/웹앱 서버가 겸함)
> · **시그널링 서버(Verifier)** = 공개키 보유 = 서명 검증 = 세션 제어 (실시간 메시징 브로커, 예: MQTT over `wss://`)
> · **데이터 중계 서버** = 실제 스트림 데이터(화면·제어) 전달 (실시간 저지연 채널, 예: TCP)

흔히 "중계 서버"로 통칭하지만, 실시간 원격 제어(또는 스트리밍) 경로는 **역할이 다른 두 서버**로 분리되는 것이 일반적입니다. DPoP 소유 증명 검증을 어느 지점에 둘지(§8~§12)를 정하려면 이 구분이 전제가 됩니다.

| 구분         | ① 시그널링 서버 (메시징 브로커)         | ② 데이터 중계 서버                       |
|------------|---------------------------------|-------------------------------------|
| 역할         | **세션 제어 + 토큰 인증**             | **실제 화면·제어 데이터 전달**               |
| 무엇이 흐르나    | 세션 수립·제어 신호, JWS 인증             | 클라이언트 ↔ 대상(에이전트) 간 실시간 제어 스트림     |
| 전송 (전형)    | `wss://`                        | `tcp://` (순수 TCP, HTTP 헤더 없음)      |
| 인증         | 여기서 발급 토큰을 검증                   | 연결 최초 프레임에서 토큰·Proof 검증(§10~§12)   |

> 💡 **핵심 분업:** **인증(JWS·소유 증명)은 검증자에서**, **실데이터는 데이터 중계 서버에서** 처리된다. 데이터 중계는 순수 TCP라 HTTP 헤더를 쓸 수 없으므로, 소유 증명 검증은 **연결 최초 프레임**에서 수행한다(§10 5단계).

## 7. 현재 구조와 그 이유 - 무상태·초고속 검증

### 핵심 사실: 검증자에는 DB가 없다

발급 서버가 발급 주체가 된 것은 우연이 아니라 다음 제약에서 온 **실무적 타협**입니다.

1. **검증자에 저장소가 없음** - 사용자·에이전트 관계를 조회할 DB가 없어 상태 없는(stateless) 검증만 가능. 권한 정보를 토큰 Payload에 실으면 DB 없이 검증됨.
2. **상용 메시징 브로커** - 검증자가 상용 브로커라면 JWT 발급 로직을 커스텀 삽입하기 어렵고 운영 복잡도가 급증함.
3. **패치 주기 분리** - 웹(발급 서버) 패치는 자유롭지만 브로커는 인프라라 자주 못 바꿈. 인증 로직을 발급 서버에 두면 발급 서버만 패치하면 됨.

### 왜 클라이언트가 토큰을 직접 들고 다니는가 - 무상태·초고속 검증

이 구조의 진짜 설계 의도는 "편의"가 아니라 **대규모 트래픽에서의 검증 성능**입니다. 실시간 세션은 수많은 클라이언트가 동시에 검증자에 붙습니다. 이때 검증을 위해 아래 두 경로를 택했다면 모두 병목·의존을 만듭니다.

| 택하지 않은 방식                                                              | 왜 안 쓰는가 (단호히)                                                                                                       |
|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| 검증자 ↔ 발급 서버 **이종 서버 간 검증(S2S)**<br>(검증자가 접속마다 발급 서버에 "이 토큰 유효?" 질의)   | 접속 1건마다 서버 간 왕복(RTT)이 붙어 **지연이 트래픽에 비례해 폭증**. 발급 서버가 검증 트래픽의 단일 병목·단일 장애점이 된다. 대규모 동시 접속에서 사실상 불가.                  |
| 클라이언트가 **검증 API를 발급 서버에 직접 호출**<br>(붙기 전에 물어보고 붙기)                     | 검증 주체를 발급 서버로 되돌리는 것이라 **JWS를 쓰는 의미 자체가 사라진다.** 매번 왕복 + 의존 → stateless 이점 소멸. "빠르게, 서버 간 통신 없이" 검증하려는 목적과 정면 충돌.  |

> 💡 **그래서 선택한 것:** 클라이언트가 **위조 불가능한 "출입증"(JWS)**을 직접 들고 다니고, 검증자는 **손에 든 공개키만으로 그 자리에서(로컬) 검증**한다.
> - **무상태(Stateless) 검증** - 검증자는 DB도, 발급 서버 질의도 필요 없이 토큰 서명만 검증한다. (단, 소유 증명 도입 시 **토큰 단일사용(redeem)** 목적의 공유 상태만 예외로 둔다 - "서명·cnf·ath 검증은 무상태 + redeem만 공유 스토어".)
> - **초고속·수평 확장** - 검증이 CPU 로컬 연산이라 지연이 트래픽과 무관. 검증자 인스턴스를 늘려도 공유 저장소가 필요 없다.
> - **서버 간 통신 0회** - 공개키(JWKS)는 시작 시/주기적으로 한 번만 받아 캐싱하면, 이후 접속마다의 발급 서버 통신이 없다.

즉 "발급 서버 발급 → 클라이언트 소지 → 검증자 로컬 검증"은 **이종 서버 간 실시간 검증을 없애기 위한** 의도적 설계입니다. DPoP 소유 증명은 이 무상태 정신을 유지한 채(§9), 토큰을 Bearer에서 발신자 구속(sender-constrained)으로 전환합니다.

### 구현 예시 (의사코드)

발급 서버가 EC P-256 키쌍을 생성·보관하고, 역할별로 JWT를 발급하며, 실시간 연결에 자격 증명을 주입하는 흐름은 언어·프레임워크 무관하게 다음과 같습니다.

```text
// ① 발급 서버: EC P-256 키쌍 생성 (kid, 발급시각 부여)
keyPair = generateKeyPair("EC", "P-256"); keyPair.kid = newKid()

// ② 역할별 JWT 발급 (권한을 클레임에 실음)
authority = { sub: subscribeTopics, pub: publishTopics }
token = sign({ id: "svc::WebServer", authority, exp }, keyPair.private)

// ③ 실시간 연결(메시징/WebSocket)에 자격 증명 주입
connectOptions.username = connId
connectOptions.password = token
```

---

# Part III · DPoP 소유 증명 (cnf 키 바인딩)

## 8. cnf 키 바인딩 소유 증명 개념 (PoP · 왕복 없는 이중 검증)

DPoP는 토큰을 **특정 키의 소유에 묶어(bind)**, 토큰을 손에 넣어도 그 키의 개인키가 없으면 쓸 수 없게 만드는 소유 증명(Proof of Possession) 방식입니다. 순수 JWS(Bearer)는 "서명이 유효한가"만 보고 "누가 소지했는가"는 보지 않는데, DPoP는 여기에 **키 소유 증명**을 더합니다.

### 먼저 개념부터 - 두 눈높이로

> 🟢 **신입 개발자용 (Easy) - "지문 인식 호텔 카드키"**
> - **일반 JWS는 그냥 호텔 카드키**다. 발급 서버가 발급해 주고, 문(검증자)은 "카드가 진짜인가"만 본다. **주우면 아무나 문을 연다.**
> - **DPoP는 "지문 인식 카드키"**다. 카드(토큰)에 **내 지문(공개키 지문)**이 새겨져 있고, 문을 열 때마다 **실제 내 손가락(개인키)**을 대야 열린다.
> - 그래서 **카드를 훔쳐도 소용없다.** 도둑에겐 내 손가락(개인키)이 없으니까. 개인키는 내 기기 밖으로 **절대 나가지 않는다.** 훔칠 카드에 애초에 손가락이 안 담겨 있다.

> 🔵 **시니어 개발자용 (Deep) - Cryptographic Key-Binding**
> - **Ephemeral Key Pair**: 클라이언트가 세션 단위로 EC P-256 **임시 키쌍**을 생성한다. 개인키는 프로세스 메모리(또는 보안 저장소)에만 존재하고 네트워크로 반출되지 않는다.
> - **JWK Thumbprint (RFC 7638)**: 공개키 JWK의 정규(canonical) 필드를 정렬·직렬화해 SHA-256 해시한 값(`jkt`). 공개키의 고정 지문이며, 발급 서버는 이 `jkt`를 Access Token의 `cnf`(confirmation) 클레임에 박제한다.
> - **Cryptographic Key-Binding**: 이로써 토큰은 특정 키쌍에 **암호학적으로 결착**된다. Bearer(소지자) 모델에서 **Sender-Constrained(발신자 구속)** 모델로 전환된다.
> - 검증자는 매 요청의 **Proof JWT 서명 검증 + `jkt` 크로스 매칭**을 순수 CPU 연산으로 수행 → 무상태 검증 정신(§7) 유지, `jti` 재사용 방지용 단기 캐시만 로컬에 둔다.

**역할 매핑:** 발급은 **발급 서버(Issuer)**가, 소유 증명 검증은 **세션에 접속하는 지점의 검증자**가 담당합니다. 실시간 스트림의 실제 진입점이 데이터 중계(순수 TCP)이므로, **소유 증명 검증은 데이터 중계 접속 지점에 둡니다**.

## 9. 왕복 없는 무상태 이중 검증

이 방식이 challenge-response(서버가 nonce를 내려 서명받는 왕복)와 결정적으로 다른 점은 **바인딩이 "연결 시점"이 아니라 "토큰 발급 시점"에 이미 확정**된다는 것입니다.

```mermaid
flowchart LR
    A["원격제어 요청 시점<br/>클라가 키쌍 생성"] --> B["공개키를 요청에 실어 발급 서버로"]
    B --> C["발급 서버: 그 공개키 지문(jkt)을<br/>토큰 cnf에 박제해 발급"]
    C --> D["연결 시: 토큰 + Proof를 1회 전송"]
    D --> E["중계: 무상태 이중 검증<br/>(토큰 서명 + proof·cnf 일치)"]
    style C fill:#d1e7dd,stroke:#198754
    style E fill:#d1ecf1,stroke:#17a2b8
```

- **키 협상 왕복 없음** — 중계가 클라와 키를 주고받거나 nonce를 발급하는 왕복이 필요 없다. 신뢰(cnf)는 발급 서버가 토큰에 **미리 구워** 두었고, 중계는 그것을 **검증만** 한다.
- **무상태** — 중계는 발급 서버 공개키(JWKS, 시작 시 캐시)만 있으면 되고, 접속마다의 외부 질의·nonce 상태가 없다. §7의 "무상태·서버간 통신 0회" 원칙과 정확히 일치한다.
- **1-RTT** — 클라는 토큰과 Proof를 **연결과 함께 한 번에** 보내고, 중계는 즉시 판정한다. (challenge-response는 nonce 발급→서명→검증으로 1왕복이 더 든다.)

> **트레이드오프(정직하게):** 왕복이 없는 대신 신선도는 서버 nonce가 아니라 **토큰 단일사용(redeem) + 짧은 `exp`**로 보장하며(§11), 전송 구간 TLS를 전제로 한다.

## 10. 세션 수립 6단계 시퀀스

```mermaid
sequenceDiagram
    participant C as Client (뷰어/호스트)
    participant W as 발급 서버 (Issuer)
    participant R as 검증자 (데이터 중계 접속 지점)
    Note over C: ① 임시 키쌍 생성<br/>(priv 은닉, pub=JWK)
    C ->> W: ② 원격제어 요청 + 공개키(JWK) 전달
    Note over W: ③ jkt = SHA256(JWK) 연산<br/>cnf.jkt 박제해 토큰 발급
    W -->> C: Access Token (cnf 포함)
    Note over C: ④ Proof JWT 생성<br/>(ath=토큰 해시·iat, priv로 서명)
    C ->> R: ⑤ 연결 최초 프레임 1회 전송<br/>토큰 + Proof (왕복 없음)
    Note over R: ⑥ 3중 교차 검증
    R -->> C: 통과 시 세션 수립
```

### 1단계 · 클라이언트 임시 키쌍 생성 (개인키 은닉)

클라이언트(뷰어·호스트)가 세션 시작 시 EC P-256 키쌍을 만든다. **개인키(priv)는 기기 메모리 밖으로 절대 나가지 않는다.** 공개키(pub)만 JWK 형태로 다음 단계에 실린다.

```text
// Client - 런타임 키쌍 생성 (pseudo)
keyPair    = generateKeyPair("EC", "P-256")
privateKey = keyPair.private   // 메모리에만 보관, 반출 금지
publicJwk  = keyPair.public.toJWK()   // { kty:"EC", crv:"P-256", x, y }
```

> **클라이언트 유형별 개인키 보관 (반드시 고려)** — 이 설계는 "개인키가 기기 밖으로 안 나간다"를 전제로 하므로, 클라 유형마다 보관 방식·비용을 달리 설계한다.

| 클라 유형 | 개인키 저장 | 확보 방법 | 비용·주의 |
|---|---|---|---|
| **에이전트(디바이스)** | OS 키스토어(비추출) | 기기 등록 시 만든 키쌍 재사용 가능 | ~0 (이미 있으면) |
| **네이티브 앱 뷰어** | Android Keystore / iOS Keychain | 앱 최초 실행 시 키쌍 생성·공개키 등록 | 낮음 |
| **웹 뷰어(브라우저)** | WebCrypto **non-extractable** `CryptoKey`(P-256) | 로그인/세션 요청 시 생성·공개키 등록 | **중** — 시크릿 모드·다중 탭·새로고침·재발급 시 키 수명 관리 설계 필요 |

> ⚠️ **웹 뷰어가 가장 까다롭다.** non-extractable 키는 JS로 값을 못 읽어 XSS 유출은 막지만, **탭/세션마다 키가 갈리고 새로고침 시 소실**될 수 있다. 세션 단위 키 재사용·재발급 정책을 명시해야 하며, 로그·전송·디스크 평문 저장은 금지한다.

### 2단계 · 발급 서버에 원격제어 요청 + 공개키(JWK) 전달

```http
POST /api/remote/session HTTP/1.1     // 발급 서버 (wss/https)
Authorization: Bearer <사용자 로그인 세션/토큰>
Content-Type: application/json

{ "sessionId": "session-1234", "dpop_jwk": { "kty":"EC", "crv":"P-256", "x":"f83OJ...", "y":"x_FEz..." } }
```

### 3단계 · 발급 서버의 지문 연산(SHA-256) + `cnf.jkt` 박제 발급

발급 서버는 받은 공개키 JWK로 **RFC 7638 Thumbprint(`jkt`)**를 계산해 Access Token의 `cnf` 클레임에 넣고 서명·발급한다.

```json
// Access Token(JWS) Payload - 발급 서버가 발급
{
  "sub": "agent-8842",
  "exp": 1751414700,             // 짧은 만료
  "cnf": {
    "jkt": "0ZcOCORZNYy-DWpqq30jZyJGHTN0d2HglBV3uiguA4I"  // = SHA-256 thumbprint(공개키 JWK)
  }
}
```

### 4단계 · 클라이언트 Proof JWT 생성 + 개인키 서명

클라이언트는 연결 시 Proof JWT를 만들어 **1단계의 개인키로 서명**한다. **DPoP의 `htm`/`htu`(HTTP 요청 바인딩)는 쓰지 않는다** — 연결 대상 엔드포인트가 사실상 하나라 무의미하기 때문이다. 대신 Proof를 **바로 이 Access Token에 결착**시킨다(`ath` = 토큰 해시). 이로써 Proof는 다른 토큰에 재사용될 수 없고, "이 토큰의 cnf 키를 지금 쥐고 있음"만 증명한다.

```text
// 소유 증명 Proof JWT — 토큰 결착형 (htm/htu·서버 nonce 없음)
Header  = { "typ":"pop+jwt", "alg":"ES256", "jwk":{ 공개키 그대로 } }
Payload = { "ath": BASE64URL(SHA-256(access_token)), "iat": 1751414400 }  // ath = 이 토큰에 결착
Signature = ECDSA(Base64Url(Header)+"."+Base64Url(Payload), privateKey)   // ← 개인키 서명
```

> **신선도·리플레이는 어떻게?** Proof 자체의 `iat` 창·`jti` 캐시에 의존하지 않는다. Proof가 **토큰에 결착(`ath`)**돼 있고, **토큰이 짧은 `exp` + 단일사용(redeem)**이므로, (토큰+Proof) 쌍을 복제해 재전송해도 **토큰이 이미 소비되어 거부**된다(§11). 즉 리플레이 방어의 축이 "Proof 신선도"가 아니라 **"토큰 단일사용"**으로 옮겨간다.

### 5단계 · 데이터 중계 서버로 연결 최초 프레임 1회 전송 (왕복 없음)

데이터 중계는 순수 TCP라 `Authorization`/`DPoP` **HTTP 헤더를 쓸 수 없다.** 연결 직후 **릴레이 프로토콜이 정한 최초 프레임**(길이 접두 바이너리 또는 JSON 등)에 토큰과 Proof를 **한 번에** 담아 보낸다. 중계가 nonce를 먼저 내려주는 **왕복(challenge-response)이 없다.**

```text
// 순수 TCP 데이터 중계: 연결 직후 최초 1회 보내는 HELLO 프레임 (왕복 없음)
// 프레이밍은 릴레이 프로토콜 규격을 따른다(예: [4바이트 길이][JSON] 반복)

HELLO {
  "ver": 1,
  "access_token": "eyJ...(발급 서버 발급 Access Token, cnf 포함)...",   // ← "무엇을 할 수 있나"(권한)
  "pop_proof":    "eyJ0eXAiOiJwb3Arand0...(Proof JWT, ath+개인키 서명)..." // ← "내가 그 cnf 키의 주인"(소유)
}
// 검증 전에는 어떤 스트림 데이터도 흘려보내지 않는다(연결 게이트).
```

> 시그널링이 WebSocket(`wss://`)인 경우엔 최초 upgrade가 HTTP라서 같은 두 값을 헤더로 실을 수도 있다. 그러나 데이터 중계는 순수 TCP이므로, 위 프레임 검증이 핵심이다.

**두 값의 역할 분담:** `access_token`은 발급 서버가 보증한 **권한**(누구·무엇), `pop_proof`는 클라이언트가 증명하는 **소유**(진짜 주인). 둘이 짝(cnf)이 맞아야 통과한다.

### 6단계 · 검증자의 무상태 이중 검증 (+ 단일사용)

```text
// 검증자 - 순수 CPU 연산 (외부 질의 없음). redeem만 공유 스토어.
1) 발급자 보증 검증: verify(access_token.sig, 발급 서버 공개키(JWKS))      // 토큰이 진짜 발급 서버 발급인가
2) 소유 권한 검증:  verify(pop_proof.sig, pop_proof.header.jwk)           // 요청자가 그 개인키를 쥐고 있나
3) 크로스 매칭:    SHA256(pop_proof.header.jwk) == access_token.cnf.jkt  // 토큰의 주인 == 지금 서명한 자
4) 토큰 결착:      pop_proof.ath == SHA256(access_token)                 // 이 Proof가 이 토큰 전용인가
5) 유효·단일사용:  access_token.exp 유효 ∧ redeem(access_token.jti)      // 만료 전 ∧ 처음 쓰는 토큰인가(원자 소비)
// 하나라도 실패 = 연결 거부(fail-closed). 통과 후에야 스트림 개시.
```

> ✅ **왜 이렇게인가:** ①은 "위조 안 된 정상 토큰인가", ②는 "요청자가 키 주인이 맞나", ③은 "①의 토큰과 ②의 주인이 **동일인인가**", ④는 "이 Proof가 **이 토큰 전용**인가", ⑤는 "**처음 쓰는** 토큰인가"를 잇는다. **탈취범은 ②(개인키)가 없어 반드시 걸리고**, 복제 재전송은 ⑤(단일사용)에서 걸린다. ①~④는 순수 CPU 무상태 연산이고, ⑤(redeem)만 공유 스토어를 쓴다.

## 11. 복제 리플레이 방어 (단일사용 redeem)

> ⚠️ **정당한 의문:** §10 5단계의 최초 프레임(`access_token`+`pop_proof`)을 **그대로 복사해 재전송**하면 글자까지 동일하다. 그럼 어떻게 막나?

핵심: 공격자는 **"복제"만 되고 "새 Proof 생성"은 안 된다**(개인키가 없으므로). 그리고 Proof가 **토큰에 결착(`ath`)**돼 있어 다른 토큰에 못 붙인다. 그래서 남는 것은 "(토큰+Proof) 쌍을 통째로 재전송"뿐인데, **이건 nonce가 아니라 "토큰 단일사용(redeem)"이 막는다.**

| 복제 시나리오                     | 막는 장치                                            | 누가 |
|------------------------------|--------------------------------------------------|-----|
| (토큰+Proof) 쌍 즉시/지연 재전송      | **redeem** — 토큰 `jti` 원자 compare-and-delete → 두 번째 소비 거부 | **검증자** |
| Proof를 다른 토큰에 갖다 붙임         | `ath` 결착 — `ath != SHA256(그 토큰)` → 거부           | **검증자** |
| 만료 노린 지연 재전송               | 토큰 `exp`(짧게) 경과 → 거부                            | **검증자** |

> 🔴 **왜 nonce 캐시가 아니라 redeem인가:** 이 설계는 왕복(핸드셰이크)이 없으므로 서버 nonce가 없고, Proof에 `jti` 캐시를 두지도 않는다. 대신 **"토큰 자체를 한 번만 쓰게(single-use)"** 만들어 복제 재전송을 막는다. redeem은 **공유 스토어(Redis 등)의 원자적 compare-and-delete**로 다중 인스턴스에서도 "클러스터 전체 1회"를 보장한다(TOCTOU·노드 우회 차단, fail-closed).

> 💡 **§7 "무상태"와의 관계:** 검증의 ①~④(서명·cnf·ath 대조)는 **순수 CPU 무상태**다. **redeem(⑤)만 공유 스토어**를 쓰는데, 이는 "복제 방지 캐시"가 아니라 **단일사용·즉시폐기라는 별도 기능**의 비용이다. (DPoP식 `jti` 캐시가 요청량에 비례해 커지는 것과 달리, redeem은 토큰 단위 소비라 수명이 짧다.)

## 12. 역할별 구현 (발급 서버 · 검증자 · 클라이언트)

각 파트의 구현 스펙이다. 언어 무관 pseudo-code로 로직 흐름만 제시한다.

### 🟧 발급 서버 담당

- **클라이언트 JWK 수신 API 설계** - 세션 개설 요청 바디에 `dpop_jwk`(공개키) 필드 수용. 필수값·형식(kty/crv/x/y) 검증, 잘못된 곡선·필드 거절.
- **RFC 7638 Thumbprint 연산** - 공개키 JWK의 **필수 필드만**(`{crv,kty,x,y}`) **사전순 정렬 + 공백 없는 JSON**으로 직렬화 후 SHA-256 → Base64Url. (라이브러리 있으면 재구현 금지: 예를 들어 Nimbus JOSE의 `ECKey.computeThumbprint()` 등.)
- **JWT `cnf` 클레임 주입** - 발급 시 `claims.cnf = { jkt: <thumbprint> }`. 발급 로직에 `exp`(짧게)와 함께 추가.

```text
// 발급 서버 pseudo
jwk = request.body.dpop_jwk
validateEcP256(jwk)                        // 형식·곡선 검증
jkt = base64url(sha256(canonicalJson(jwk, ["crv","kty","x","y"])))
token = sign({ sub, exp: now+300, cnf: { jkt } }, issuerPrivateKey)
return token
```

### 🟦 검증자(시그널링) 담당

- **핸드셰이크 프레임 파싱 (순수 TCP)** - 데이터 중계는 HTTP 헤더가 없으므로, 연결 직후 최초 HELLO 프레임에서 `access_token`·`pop_proof`를 추출하는 커넥션 게이트. 둘 중 하나라도 없으면 스트림을 열기 전에 즉시 거절. (시그널링이 WebSocket이면 upgrade 헤더에서 추출 병행)
- **CPU 무상태 이중 검증 + 크로스 체크** - 외부 서버 질의 없이 로컬 연산으로 §10 6단계의 ①~④(토큰 서명·proof 서명·cnf 매칭·ath 결착) 수행. `jkt` 매칭이 핵심.
- **단일사용 redeem (⑤)** - 복제 재전송 방어는 **토큰 `jti`를 공유 스토어에서 원자 compare-and-delete**로 소비. 두 번째 접속 거부. `jti` 캐시(요청량 비례)가 아니라 토큰 단위 소비라 수명이 짧다. **이것이 이 설계의 유일한 공유 상태.**

```text
// 검증자 pseudo - 연결 최초 프레임 게이트 (왕복 없음)
frame = readHelloFrame(conn)              // 순수 TCP: HTTP 헤더가 아니라 프레임
at  = frame.access_token
prf = frame.pop_proof
if (!at || !prf) reject()
if (!verify(at, issuerPubKey))                  reject("bad token")     // ① 발급자 서명
if (!verify(prf, prf.header.jwk))               reject("bad proof")     // ② 개인키 소유
if (sha256thumb(prf.header.jwk) != at.cnf.jkt)  reject("key mismatch")  // ③ 소유==토큰주인
if (prf.ath != base64url(sha256(at)))           reject("token mismatch")// ④ 이 토큰 전용 Proof
if (now() > at.exp)                             reject("expired")       // ⑤a 만료
if (!redeemAtomic(at.jti))                      reject("replay/used")   // ⑤b 단일사용(원자 소비)
accept()                                  // 통과 후에야 스트림 개시
```

### 🟩 클라이언트 (뷰어 · 호스트/에이전트 · 프론트) 담당

- **런타임 EC P-256 키쌍 생성 + 안전 보관** - 세션 시작 시 키쌍 생성. 개인키는 **메모리/보안 저장소**에만(브라우저는 `CryptoKey extractable:false`, 네이티브는 Keychain/Keystore). 로그·전송·디스크 평문 저장 금지.
- **연결 시 Proof JWT 서명 (토큰 결착형)** - `htm`/`htu`·서버 nonce 없이, **이 토큰에 결착**하는 `ath`(=토큰 SHA-256 해시)와 `iat`만 담아 개인키로 서명. 발급받은 토큰 1건에 대해 만들며, 토큰이 단일사용이라 재전송은 검증자 redeem에서 걸린다.
- **파트별 스택** - 프론트/뷰어(JS): WebCrypto `subtle.generateKey/sign`. 호스트·에이전트(네이티브): 플랫폼 crypto + OS 보안 저장소.

```text
// Client pseudo - 연결 시 (왕복 없음)
proof = sign(
  { typ:"pop+jwt", alg:"ES256", jwk: publicJwk },              // header
  { ath: base64url(sha256(accessToken)), iat: now() },         // payload — 이 토큰에 결착
  privateKey                                                    // 은닉된 개인키
)
sendHelloFrame(conn, { access_token: accessToken, pop_proof: proof })  // 최초 프레임 1회
```

### 평가 요약

| 항목     | 평가                                                                               |
|--------|----------------------------------------------------------------------------------|
| 구현 난이도 | 🟡 중간~높음 - 3개 파트(발급 서버·검증자·클라이언트) 동시 작업 필요                                     |
| 강점     | 토큰만 탈취해도 **개인키 없으면 무용지물**. IP 변동 무관(오탐 없음). 표준(RFC 9449). 검증은 CPU 로컬 연산이라 무상태 유지 |
| 약점     | 3개 파트 동시 개발 부담. `jti` 재사용 방지용 **단기 캐시** 필요(단, 짧은 TTL 로컬로 충분)                     |
| 적합 상황  | 클라이언트를 커스텀 제어 가능한 경우(에이전트·런처·뷰어)                                             |

---

## Reference

- [RFC 9449 — OAuth 2.0 Demonstrating Proof of Possession (DPoP)](https://datatracker.ietf.org/doc/html/rfc9449)
- [RFC 7638 — JSON Web Key (JWK) Thumbprint](https://datatracker.ietf.org/doc/html/rfc7638)
- [RFC 7517 — JSON Web Key (JWK)](https://datatracker.ietf.org/doc/html/rfc7517)
- [IBM Docs — Demonstrating Proof of Possession](https://www.ibm.com/docs/ko/security-verify?topic=connect-demonstrating-proof-possession)

*관련 문서: [DPoP / PoP 정공법 아키텍처](dpop-pop-architecture.md) · [OpenID Connect(OIDC) 아키텍처 완전 정복](oidc-architecture.md)*
