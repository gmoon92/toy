# 교차 검증 — pns-auth-complete(DPoP) vs session-ticket-binding(cnf+PoP)

> 두 설계 문서가 같은 목표(토큰 탈취·재생 방어)에 서로 다른 소유증명(PoP) 메커니즘을 제시.
> 질문: 어느 쪽이 정석인가? 작성: 2026-07-03

## 결론 요약

**"DPoP가 곧 정석"이 아니라, 전송 방식에 따라 정석이 다르다.**

- **HTTP 리소스 서버** → DPoP(RFC 9449, htm/htu 요청 바인딩)가 정석.
- **연결 지향(MQTT/WebSocket/커스텀 TCP, 핸드셰이크 1회)** → cnf + nonce-challenge PoP(RFC 7800 일반 PoP / RFC 8705 mTLS-bound 계열)가 정석.

rview 원격제어는 **연결 지향**이다. 따라서 겹치는 "바인딩 층"에서는 **session-ticket-binding(B안)이 더 정석**이고,
pns-auth-complete는 **HTTP용 도구(DPoP)를 연결 핸드셰이크에 이식**해서 사용자가 느낀 "이상함"이 생겼다.

단, 두 문서는 범위가 달라 **상호 배타가 아니라 상호 보완**이다(아래 §범위).

## 핵심 차이 — PoP 메커니즘

| | pns-auth-complete (Doc1) | session-ticket-binding (Doc2) |
|:--|:--|:--|
| 증명 방식 | **요청 바인딩** — Proof에 htm/htu/iat/jti 서명 | **challenge-response** — 서버 nonce에 서명 |
| 신선도 보장 | 클라 시계(`iat` 창) + jti 캐시 (+ 선택 nonce) | 서버 발급 nonce(1회성) |
| 서버 왕복 | 불필요(nonce는 선택) | 필요(nonce 1왕복) |
| 리플레이 방어 | jti 분산 캐시(Redis/sticky) | nonce로 원천 봉쇄 |
| 표준 매핑 | RFC 9449 DPoP(HTTP 전용) | RFC 7800 cnf 일반 PoP / RFC 8705 계열 |
| 적합 대상 | 무상태 다중 HTTP 요청 | 연결 핸드셰이크 1회 |

### 왜 연결 지향엔 Doc2(challenge-response)가 정석인가
- MQTT 세션은 **CONNECT에서 1회 인증**하고 오래 유지된다. DPoP의 htm/htu+jti캐시는 "무상태 다중 요청을 왕복 없이 인증"하는 최적화인데, **여기엔 그 문제가 없다.**
- 핸드셰이크 1회라면 서버 nonce challenge-response가 엄밀히 우월:
  - 클라 시계창(`iat`) 의존 제거 → 시계 오차 오탐 없음.
  - 분산 jti 리플레이 캐시 불필요 → Doc1의 다중 인스턴스 우회 구멍이 원천 소거.
- TLS·SSH·Kerberos·FIDO/WebAuthn이 전부 challenge-response인 이유와 동일.

## 항목별 정석 판정

| 쟁점 | 정석 | 근거 |
|:--|:--|:--|
| PoP 메커니즘 | **Doc2** | 연결 지향엔 challenge-response가 적합. Doc1은 HTTP 최적화 오이식 |
| 단일사용(redeem) | **Doc2** | binding ≠ single-use. 진짜 단일사용+즉시폐기(T4)는 공유스토어 원자 소비 필요. Doc1은 jti 리플레이 캐시를 단일사용으로 혼용 |
| 위협모델 정직성 | **Doc2** | "감염 PC 실시간 키 오용은 범위 밖" 명시. Doc1은 "DPoP=감염단말 방어" 과장 |
| aud/role | **Doc2** | `aud="signaling"`(AT 혼용 차단) + `role` 방향성(뷰어↔에이전트 격리). Doc1엔 없음 |
| 실현성 정직성 | **Doc2** | 중계팀에 Q1~Q5(프로토콜에 PoP 실을 여지 있나 등) 명시 질문. Doc1은 MQTT-fit 문제 무시 |
| 전송/사고 근본원인 | **Doc1** | roomid 탈취 = Data Relay `tcp://` 평문 → TLS 우선. Doc2엔 이 축이 없음 |
| TLS 우선순위 | **Doc1** | "TLS 없이는 PoP도 무용" 프레이밍은 옳음(단 인증서 검증 전제 누락) |

## 두 문서의 공통 한계

- **장수명 연결 채널 바인딩 없음**: 둘 다 핸드셰이크만 증명하고, 유지 세션은 TLS 무결성에 의존. (내 보안검토 C-2)
- **MQTT v3.1.1 적재 문제**: 소스 확인 결과 Paho mqttv3(헤더·User Property 없음). Doc1의 이중 헤더도, Doc2의 nonce 왕복도 프로토콜 여지가 필요.
  - Doc2는 이를 Q1으로 **정직히 질문**. Doc1은 **무시**.

## 범위 차이 (상호 보완)

- **Doc1** = 사고(roomid 탈취) 대응 + 전송 계층 + 토큰 하드닝 로드맵(TLS→exp→바인딩→DPoP).
- **Doc2** = session-authz v0.1 계약의 **바인딩 층 설계**(iam/EdDSA/RBAC, #273521 파생). 더 엄밀한 계약·위협분해(T1~T5).
- 두 문서가 겹치는 "바인딩 메커니즘"에서는 **Doc2가 우세**, 전송/사고 축에서는 **Doc1이 기여**.

## 이상적 통합안 (정석 재구성)

1. **전송(P0)**: Data Relay `tcp://`→`ssl://` + **서버 인증서 검증 강제**(현 trust-all 제거). — Doc1 축 + 내 C-1 보강.
2. **토큰**: 짧은 `exp` 강제(코드 이미 지원) + `aud`/`role` 강제 + `jti` **redeem(공유스토어 원자 소비)**. — Doc2 축.
3. **바인딩**: `cnf=jkt` 박제 + **연결 시 nonce-challenge PoP**(DPoP htm/htu 대신). — Doc2 축이 정석.
4. **위협 경계 명시**: 감염 단말 실시간 오용은 범위 밖(상위 세션 탈취 탐지 층). — Doc2 프레이밍.
5. **선결 검증**: MQTT v3.1.1에서 nonce 왕복 적재 방법(MQTT5 여부)·브로커 커스텀 수용. — Doc2 Q1.

> 한 줄: **Doc2(cnf+nonce PoP)가 이 연결지향 아키텍처의 정석이고, Doc1의 DPoP는 HTTP용 표준을 잘못 이식한 것.** 다만 Doc1의 "TLS 먼저·사고 근본원인" 축은 Doc2에 없는 별도 기여라 둘을 합쳐야 완성된다.
