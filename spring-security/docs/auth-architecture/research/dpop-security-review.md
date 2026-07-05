# PNS DPoP 설계 보안 검토 (분석 결과)

> 대상: `.claude/.tmp/pns/pns-auth-complete.html` (PNS 인증 아키텍처 개선안, 2026-07-02)
> 검토 관점: 보안 관리자 / 소스 대조: `workspace/rviewdev`
> 작성: 2026-07-03

## 총평

방향(TLS → exp → 바인딩 → DPoP)과 표준 선택(RFC 9449)은 타당하나, **문서에 쓰인 그대로는 채택 불가**.
현재 MQTT 스택·TLS 구현·위협 모델과 충돌하는 지점이 있고, 그중 둘은 설계 전제 자체를 무너뜨린다.

- DPoP는 이번 roomid 탈취 사건을 못 고친다(채널 불일치) — 방어 심화일 뿐 사고 대응이 아니다.
- 문서가 딛고 선 P0(TLS) 전제가 현재 코드에서 성립하지 않는다(SSL이 trust-all).

## 소스 대조로 확인한 사실

| 항목 | 확인 결과 | 근거 |
|:--|:--|:--|
| MQTT 버전 | **v3.1.1(Paho mqttv3)** — 헤더·User Property 없음. username/password 2칸뿐 | `import ...mqttv3.*` |
| 인증 주입 | CONNECT username/password 필드 | `MqttIntegrationConfig` setUserName/Password |
| TLS 검증 | **trust-all** — 서버 인증서 검증 안 함(MITM 무방비) | `MqttIntegrationConfig.java:144-155` checkServerTrusted 비어있음, CLIENTAUTH=false |
| exp 지원 | **이미 지원** — 호출부에서 안 넣는 운영 갭 | `MqttAuthHelper.generateToken(id, authority, Date expiryDate)` |
| 키 | EC P-256(ECDSA) | `ECKeyGenerator(Curve.P_256)` |

## Critical (설계 전제 붕괴)

### B-1. 현재 MQTT SSL은 trust-all → P0(TLS) 전제가 거짓
- `checkServerTrusted()` 비어있음 + `getAcceptedIssuers()=null` + `CLIENTAUTH=false`.
- `ssl://`가 "인증된 TLS"가 아니라 "익명 암호화"에 그쳐, 능동적 MITM이 자기 인증서로 그대로 통과.
- 문서의 "TLS = 도청·MITM 원천 차단" 명제가 이 코드에선 성립하지 않음. P0에 **서버 인증서 검증(트러스트 스토어 고정) 필수** 항목이 통째로 빠져 있음.

### B-2. MQTT v3.1.1 → DPoP "이중 헤더"를 실을 자리가 없음
- `Authorization` + `DPoP` 두 헤더 모델이 프로토콜상 표현 불가(헤더 없음, MQTT5 User Property 없음).
- 두 자격증명을 실으려면 한 필드에 구분자로 욱여넣거나 MQTT5 업그레이드가 전제.
- §6의 자기모순: "커스터마이즈 어려운 브로커라 BE 발급 택함"이라면서, DPoP는 브로커에 Proof 파싱·jkt 대조·jti 캐시라는 무거운 커스텀 검증을 요구.

## High (배경 대비 DPoP 고유 취약점)

- **C-1. 사건 채널 불일치**: roomid 탈취는 Data Relay(`tcp://`)에서, DPoP는 PNS(`wss://`·이미 안전)에 적용. DPoP 100% 구현해도 roomid 문제는 그대로. P0 TLS만이 그것을 고침.
- **C-2. 장수명 연결 사각지대**: DPoP은 요청 단위 증명인데 MQTT는 연결 단위. CONNECT 1회만 검증되고 이후 유지 세션엔 매-메시지 증명 없음. 세션 하이재킹 시 DPoP 보호 0. 채널 바인딩 필요(문서에 없음).
- **C-3. "감염 단말" 방어 과장**: DPoP은 키 유출만 막음. 상주 악성코드는 `extractable:false`라도 키 사용은 가능해 Proof를 원할 때 서명. 문서가 인용한 바로 그 "감염 단말" 위협을 못 막음 → 표현 정정 필요.

## Medium (운영·구현)

- **jti 재사용 방지 = 무상태 이점 훼손 + SPOF**: 다중 인스턴스는 Redis 공유 캐시(§6이 피하려던 그 공유 저장소) 또는 sticky routing 필요.
- **서버 nonce가 선택 → 창 내 재생 여지**: 고가치 원격제어 경로는 nonce를 기본으로 승격해야.
- **시계 오차**: `iat` 창 의존은 가용성/보안 딜레마. nonce가 해소.
- **JWK 결착 주입**: 2단계 dpop_jwk 제출 레그가 CSRF/탈취 가능하면 공격자 키로 결착. 발급 레그 인증 강도에 종속.

## Low (구현 시 못 박을 것)

- Proof `alg`=ES256 고정, `none` 거절, proof.jwk는 `cnf.jkt` 대조로만 신뢰.
- 롤아웃 다운그레이드 차단: `cnf` 있으면 DPoP 증명 없는 요청 거절.

## 권고

1. **P0 정정(최우선)**: "TLS 교체"에 "서버 인증서 검증 강제(trust-all 제거)" 포함. 현 코드 trust-all은 즉시 시정 대상.
2. **DPoP 실현성 선검증**: MQTT v3.1.1에서 두 자격증명 적재 방법(MQTT5 여부)·브로커 커스텀 수용 여부를 §6 제약과 대조.
3. **문서 두 과장 정정**: "TLS=MITM 원천 차단", "DPoP=감염단말 방어".
4. **장수명 연결 사각지대 명시 + 채널 바인딩 검토**, nonce 고가치 경로 기본 적용.
5. **기대효과 재정렬**: DPoP은 roomid 사건 해결책이 아니라 TLS·exp 이후 잔여 리플레이 방어(defense-in-depth)임을 §15에 명시.
