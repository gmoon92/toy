package com.gmoon.springsecurityjose.jose.jwk;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import com.gmoon.springsecurityjose.jose.jws.JwsUtil;

@DisplayName("JwkStore — kid→JsonWebKey 캐시")
class JwkStoreTest {

	private JWTClaimsSet sampleClaims() {
		return new JWTClaimsSet.Builder()
			 .subject("svc::WebServer")
			 .expirationTime(Date.from(Instant.now().plusSeconds(300)))
			 .build();
	}

	@Nested
	@DisplayName("register / get")
	class Register {

		@Test
		@DisplayName("kid로 등록한 값 객체를 그대로 조회한다")
		void registersAndGets() {
			JwkStore store = new JwkStore();
			JsonWebKey jwk = JsonWebKey.generate();

			store.register(jwk);

			assertThat(store.get(jwk.getId())).as("등록한 값 객체를 kid로 조회").isEqualTo(jwk);
		}

		@Test
		@DisplayName("등록되지 않은 kid 조회는 실패한다")
		void unknownKidFails() {
			JwkStore store = new JwkStore();

			assertThatThrownBy(() -> store.get("unknown"))
				 .as("미등록 kid는 예외")
				 .isInstanceOf(IllegalArgumentException.class)
				 .hasMessageContaining("No key registered");
		}
	}

	@Nested
	@DisplayName("키 복원 조회")
	class Lookup {

		@Test
		@DisplayName("서명 키는 개인키를 포함하고 검증 키는 공개키만이다")
		void signingHasPrivateVerificationDoesNot() {
			JwkStore store = new JwkStore();
			JsonWebKey jwk = JsonWebKey.generate();
			store.register(jwk);

			assertThat(store.getSigningKey(jwk.getId()).isPrivate()).as("서명 키엔 개인키(d)").isTrue();
			assertThat(store.getVerificationKey(jwk.getId()).isPrivate()).as("검증 키엔 개인키 없음").isFalse();
		}
	}

	@Nested
	@DisplayName("JWKS 배포")
	class Jwks {

		@Test
		@DisplayName("여러 kid를 담고 개인키(d)는 노출하지 않는다")
		void exposesPublicKeysOnly() {
			JwkStore store = new JwkStore();
			JsonWebKey a = JsonWebKey.generate();
			JsonWebKey b = JsonWebKey.generate();
			store.register(a);
			store.register(b);

			String jwks = store.toPublicJwkSet();

			assertThat(jwks).as("두 kid가 함께 게시된다(rotation)").contains(a.getId()).contains(b.getId());
			assertThat(jwks).as("공개 좌표 x·y는 포함").contains("\"x\"").contains("\"y\"");
			assertThat(jwks).as("개인키 d는 미포함").doesNotContain("\"d\"");
		}

		@Test
		@DisplayName("등록된 키가 없으면 빈 keys 배열을 반환한다")
		void emptyStoreReturnsEmptyKeys() {
			JwkStore store = new JwkStore();

			String jwks = store.toPublicJwkSet();

			assertThat(jwks).as("빈 JWKS에도 keys 배열은 존재").contains("\"keys\"");
			assertThat(jwks).as("담긴 키가 없다").doesNotContain("\"kid\"");
		}
	}

	@Nested
	@DisplayName("전체 흐름")
	class EndToEnd {

		@Test
		@DisplayName("서명 후 헤더 kid로 공개키를 지목해 검증한다")
		void signThenVerifyByKid() throws Exception {
			JwkStore store = new JwkStore();
			JsonWebKey jwk = JsonWebKey.generate();
			store.register(jwk);
			String kid = jwk.getId();

			String jws = JwsUtil.sign(store.getSigningKey(kid), sampleClaims());

			String kidFromHeader = SignedJWT.parse(jws).getHeader().getKeyID();
			JWTClaimsSet claims = JwsUtil.verify(jws, store.getVerificationKey(kidFromHeader));

			assertThat(kidFromHeader).as("헤더 kid는 서명키의 kid").isEqualTo(kid);
			assertThat(claims.getSubject()).as("전체 흐름 검증 성공").isEqualTo("svc::WebServer");
		}
	}
}
