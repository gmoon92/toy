package com.gmoon.springsecurityjose.jose.jws;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@DisplayName("JwsUtil — ES256 서명/검증/키 파싱")
class JwsUtilTest {

	private static final String KID = "issuer-2026-07-a";

	private JWTClaimsSet claims(Instant expiration) {
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
			 .issuer("https://issuer.example.com")
			 .subject("svc::WebServer")
			 .issueTime(Date.from(Instant.now()));
		if (expiration != null) {
			builder.expirationTime(Date.from(expiration));
		}
		return builder.build();
	}

	@Nested
	@DisplayName("generateEcKey")
	class GenerateEcKey {

		@Test
		@DisplayName("개인키를 포함한 P-256 키를 요청한 kid로 생성한다")
		void generatesPrivateP256KeyWithKid() {
			ECKey key = JwsUtil.generateEcKey(KID);

			assertThat(key.isPrivate()).as("서명하려면 개인키(d)를 포함해야 한다").isTrue();
			assertThat(key.getKeyID()).as("요청한 kid가 그대로 부여된다").isEqualTo(KID);
			assertThat(key.getCurve().getName()).as("곡선은 P-256").isEqualTo("P-256");
		}

		@Test
		@DisplayName("같은 kid로 생성해도 매번 다른 키쌍이 나온다 (kid는 라벨일 뿐 시드가 아니다)")
		void sameKidProducesDifferentKeyPair() {
			ECKey a = JwsUtil.generateEcKey("same-kid");
			ECKey b = JwsUtil.generateEcKey("same-kid");

			assertThat(a.getKeyID()).as("kid(라벨)는 같지만").isEqualTo(b.getKeyID());
			assertThat(a.getD()).as("개인키 d는 매번 무작위라 다르다").isNotEqualTo(b.getD());
			assertThat(a.getX()).as("공개키 x도 다르다").isNotEqualTo(b.getX());
			assertThat(a.getY()).as("공개키 y도 다르다").isNotEqualTo(b.getY());
		}
	}

	@Nested
	@DisplayName("sign")
	class Sign {

		@Test
		@DisplayName("header.payload.signature 3부분으로 직렬화하고 헤더에 alg·kid를 담는다")
		void producesThreePartJwsWithHeader() throws Exception {
			ECKey key = JwsUtil.generateEcKey(KID);

			String jws = JwsUtil.sign(key, claims(Instant.now().plusSeconds(300)));

			assertThat(jws.split("\\.")).as("JWS는 점으로 이은 3부분").hasSize(3);
			SignedJWT parsed = SignedJWT.parse(jws);
			assertThat(parsed.getHeader().getAlgorithm().getName()).as("alg는 ES256").isEqualTo("ES256");
			assertThat(parsed.getHeader().getKeyID()).as("헤더 kid는 서명키의 kid").isEqualTo(KID);
		}
	}

	@Nested
	@DisplayName("verify")
	class Verify {

		@Test
		@DisplayName("개인키로 서명하고 공개키로 검증하면 클레임이 복원된다")
		void verifiesWithPublicKeyAndReturnsClaims() {
			ECKey key = JwsUtil.generateEcKey(KID);
			String jws = JwsUtil.sign(key, claims(Instant.now().plusSeconds(300)));

			JWTClaimsSet verified = JwsUtil.verify(jws, key.toPublicJWK());

			assertThat(verified.getSubject()).as("sub 클레임 복원").isEqualTo("svc::WebServer");
			assertThat(verified.getIssuer()).as("iss 클레임 복원").isEqualTo("https://issuer.example.com");
		}

		@Test
		@DisplayName("다른 키로 검증하면 서명이 일치하지 않아 실패한다")
		void failsWithWrongKey() {
			ECKey signing = JwsUtil.generateEcKey(KID);
			ECKey intruder = JwsUtil.generateEcKey("intruder");
			String jws = JwsUtil.sign(signing, claims(Instant.now().plusSeconds(300)));

			assertThatThrownBy(() -> JwsUtil.verify(jws, intruder.toPublicJWK()))
				 .as("키 불일치는 서명 검증 실패")
				 .isInstanceOf(IllegalArgumentException.class)
				 .hasMessageContaining("verification failed");
		}

		@Test
		@DisplayName("Payload를 한 글자라도 바꾸면 서명이 깨져 실패한다")
		void failsOnTamperedPayload() {
			ECKey key = JwsUtil.generateEcKey(KID);
			String jws = JwsUtil.sign(key, claims(Instant.now().plusSeconds(300)));

			String[] parts = jws.split("\\.");
			char[] payload = parts[1].toCharArray();
			payload[0] = payload[0] == 'A' ? 'B' : 'A';
			String tampered = parts[0] + "." + new String(payload) + "." + parts[2];

			assertThatThrownBy(() -> JwsUtil.verify(tampered, key.toPublicJWK()))
				 .as("변조된 payload는 검증 실패")
				 .isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("exp가 과거인 토큰은 만료로 거부한다")
		void failsOnExpiredToken() {
			ECKey key = JwsUtil.generateEcKey(KID);
			String jws = JwsUtil.sign(key, claims(Instant.now().minusSeconds(60)));

			assertThatThrownBy(() -> JwsUtil.verify(jws, key.toPublicJWK()))
				 .as("exp가 지났으면 만료로 거부")
				 .isInstanceOf(IllegalArgumentException.class)
				 .hasMessageContaining("expired");
		}

		@Test
		@DisplayName("exp가 없는 토큰은 만료 검사 없이 통과한다")
		void passesWhenNoExpiration() {
			ECKey key = JwsUtil.generateEcKey(KID);
			String jws = JwsUtil.sign(key, claims(null));

			JWTClaimsSet verified = JwsUtil.verify(jws, key.toPublicJWK());

			assertThat(verified.getExpirationTime()).as("exp 없음 → 만료 검사 스킵").isNull();
		}

		@Test
		@DisplayName("형식이 깨진 토큰은 파싱 단계에서 실패한다")
		void failsOnMalformedToken() {
			ECKey key = JwsUtil.generateEcKey(KID);

			assertThatThrownBy(() -> JwsUtil.verify("not.a.jwt", key.toPublicJWK()))
				 .as("직렬화 형식이 아니면 파싱 실패")
				 .isInstanceOf(IllegalArgumentException.class)
				 .hasMessageContaining("parse");
		}
	}

	@Nested
	@DisplayName("parseEcKey")
	class ParseEcKey {

		@Test
		@DisplayName("JWK JSON 문자열을 ECKey로 복원한다 (개인키까지 왕복 동일)")
		void restoresEcKeyFromJson() {
			ECKey original = JwsUtil.generateEcKey(KID);

			ECKey restored = JwsUtil.parseEcKey(original.toJSONString());

			assertThat(restored).as("직렬화→복원 왕복은 원본과 동일").isEqualTo(original);
			assertThat(restored.isPrivate()).as("개인키까지 복원").isTrue();
		}

		@Test
		@DisplayName("파싱할 수 없는 JSON은 예외를 던진다")
		void failsOnInvalidJson() {
			assertThatThrownBy(() -> JwsUtil.parseEcKey("{ not-a-jwk }"))
				 .as("파싱 불가한 JSON은 예외")
				 .isInstanceOf(IllegalArgumentException.class)
				 .hasMessageContaining("Invalid JWK JSON");
		}
	}
}
