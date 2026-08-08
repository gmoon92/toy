# spring-security-jose

JOSE(JavaScript Object Signing and Encryption) 표준 - **JWS·JWE·JWK·JWT** - 를 한 모듈에서 다루는 학습·샘플 모듈입니다.

## Dependencies

```yaml
dependencies {
    implementation(com.nimbusds:nimbus-jose-jwt:10.9.1)
}
```

> 구현체는 [`com.nimbusds:nimbus-jose-jwt`](https://connect2id.com/products/nimbus-jose-jwt)를 사용합니다.

## 구성

- `jws/JwsUtil` - ES256(ECDSA P-256) 서명/검증 샘플. 헤더의 `kid`로 JWK를 지목하는 흐름을 보여줍니다.
- `jwk/JsonWebKey` - `kid`와 개인·공개 키를 **JWK JSON 문자열**로 담는 값 객체. DB에 문자열로 영속하기 좋고(향후 `@Embeddable`), `toSigningKey()`/`toVerificationKey()`로 `ECKey`로 복원합니다. 동등성은 `kid` 기준.
- `jwk/JwkStore` - `kid → JsonWebKey` 인메모리 캐시. 개인키로 서명하고 검증자에겐 공개키 묶음(JWKS)만 노출합니다. 같은 `kid`는 매번 다른 키가 나오므로 키를 한 번 만들어 등록해 두고 `kid`로 조회합니다. 다중 `kid` 공존으로 무중단 키 교체(rotation)를 지원합니다.

## Specs

모듈 구현 스펙은 [`docs/`](docs/README.md)에 정리되어 있습니다.

- [토큰 형식 스펙](docs/token-format-spec.md) - JWS 직렬화, 헤더 파라미터, 클레임(`jti` 포함) JSON 규격
- [키·동작 스펙](docs/key-and-operation-spec.md) - JWK·JWKS 규격, `kid` 생명주기·회전, 서명/검증 시퀀스, 코드 매핑

## Reference

- [RFC 7515 - JWS: 서명 - 위·변조 방지(무결성·진위)](https://datatracker.ietf.org/doc/html/rfc7515)
- [RFC 7516 - JWE: 암호화 - 내용 자체를 감춤(기밀성)](https://datatracker.ietf.org/doc/html/rfc7516)
- [RFC 7517 - JWK: 키를 JSON으로 표현, `kid`로 식별](https://datatracker.ietf.org/doc/html/rfc7517)
- [RFC 7519 - JWT: 클레임을 담는 그릇](https://datatracker.ietf.org/doc/html/rfc7519)
- [JWT · JWS · JWE · JWK 토큰 생태계 표준 개념](../docs/auth-architecture/jwt-jws-jwe-jwk-concepts.md) - 세션에서 토큰까지 발전 과정 입문
