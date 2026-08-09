---
title: spring-security-jose 스펙 문서
description: spring-security-jose 모듈이 구현한 JOSE(JWS·JWE·JWK·JWT) 표준의 헤더 파라미터·클레임 JSON 규격과 서명·검증·키 관리 동작방식을 코드와 결부해 상세히 정리한 스펙 문서 모음.
---

# spring-security-jose 스펙 문서

이 디렉토리는 `spring-security-jose` 모듈이 구현한 **JOSE(JavaScript Object Signing and Encryption)** 표준의 **정확한 규격과 동작**을 다룹니다. "왜 토큰인가, 왜 비대칭 서명인가" 같은 **개념·발전 과정**은 아키텍처 문서군의 선행 지식 문서에 있으며, 여기서는 그 개념이 **JSON 필드 하나하나·동작 시퀀스·이 모듈의 코드**로 어떻게 구현되는지에 집중합니다.

## 문서 구성

| 문서 | 다루는 것 |
| --- | --- |
| [토큰 형식 스펙](token-format-spec.md) | JWS 직렬화 구조, **헤더 파라미터**(`alg`·`typ`·`kid`…), **클레임 JSON 규격**(`iss`·`sub`·`exp`·**`jti`**…), 타입 규칙 |
| [키·동작 스펙](key-and-operation-spec.md) | **JWK 필드 규격**, **JWKS**, `kid` 생명주기·회전(rotation), **서명·검증 동작 시퀀스**, 코드 매핑 |

## 구현 코드

- [`jws/JwsUtil`](../src/main/java/com/gmoon/springsecurityjose/jose/jws/JwsUtil.java) — ES256(ECDSA P-256) 서명/검증
- [`jwk/JwkStore`](../src/main/java/com/gmoon/springsecurityjose/jose/jwk/JwkStore.java) — `kid → JWK` 인메모리 키 스토어, JWKS 노출

## 개념 입문 (선행 지식)

토큰이 왜 필요한지, 세션에서 JWT까지의 발전 과정이 궁금하다면 먼저 읽으세요.

- [JWT · JWS · JWE · JWK 토큰 생태계 표준 개념](../../docs/auth-architecture/jwt-jws-jwe-jwk-concepts.md)

## Reference

- [RFC 7519 — JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [RFC 7515 — JSON Web Signature (JWS)](https://datatracker.ietf.org/doc/html/rfc7515)
- [RFC 7516 — JSON Web Encryption (JWE)](https://datatracker.ietf.org/doc/html/rfc7516)
- [RFC 7517 — JSON Web Key (JWK)](https://datatracker.ietf.org/doc/html/rfc7517)
- [RFC 7518 — JSON Web Algorithms (JWA)](https://datatracker.ietf.org/doc/html/rfc7518)
