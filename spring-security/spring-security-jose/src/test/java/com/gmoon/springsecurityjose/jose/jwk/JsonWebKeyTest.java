package com.gmoon.springsecurityjose.jose.jwk;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;

import com.gmoon.springsecurityjose.jose.jws.JwsUtil;

@DisplayName("JsonWebKey — 문자열 기반 키 값 객체")
class JsonWebKeyTest {

	@Nested
	@DisplayName("generate")
	class Generate {

		@Test
		@DisplayName("kid(UUID)를 부여하고 개인·공개 키를 JSON 문자열로 담는다")
		void generatesWithJsonStrings() {
			JsonWebKey jwk = JsonWebKey.generate();

			assertThat(jwk.getId()).as("kid가 내부 UUID로 부여된다").isNotBlank();
			assertThat(jwk.getPrivateKey()).as("privateKey JSON엔 개인키 d가 포함된다(평문)").contains("\"d\"");
			assertThat(jwk.getPublicKey()).as("publicKey JSON엔 개인키 d가 없다").doesNotContain("\"d\"");
		}

		@Test
		@DisplayName("호출마다 서로 다른 kid가 부여된다 (외부 지정 불가)")
		void eachGenerateHasDistinctKid() {
			JsonWebKey a = JsonWebKey.generate();
			JsonWebKey b = JsonWebKey.generate();

			assertThat(a.getId()).as("매 생성마다 새 UUID kid").isNotEqualTo(b.getId());
		}
	}

	@Nested
	@DisplayName("복원 (embedded round-trip)")
	class Restore {

		@Test
		@DisplayName("문자열로 보관한 키를 서명용·검증용 ECKey로 복원한다")
		void restoresSigningAndVerificationKey() {
			JsonWebKey jwk = JsonWebKey.generate();

			assertThat(jwk.toSigningKey().isPrivate()).as("서명 키는 개인키를 포함").isTrue();
			assertThat(jwk.toVerificationKey().isPrivate()).as("검증 키는 공개키만").isFalse();
		}

		@Test
		@DisplayName("복원한 키로 서명하고 검증까지 왕복 동작한다")
		void restoredKeySignsAndVerifies() {
			JsonWebKey jwk = JsonWebKey.generate();
			JWTClaimsSet claims = new JWTClaimsSet.Builder()
				 .subject("svc::WebServer")
				 .expirationTime(Date.from(Instant.now().plusSeconds(300)))
				 .build();

			String jws = JwsUtil.sign(jwk.toSigningKey(), claims);
			JWTClaimsSet verified = JwsUtil.verify(jws, jwk.toVerificationKey());

			assertThat(verified.getSubject()).as("문자열→복원→서명→검증 왕복 성공").isEqualTo("svc::WebServer");
		}
	}

	@Nested
	@DisplayName("동등성")
	class Equality {

		@Test
		@DisplayName("동등성은 id(kid)만으로 판정한다")
		void equalsById() {
			JsonWebKey a = JsonWebKey.generate();
			JsonWebKey b = JsonWebKey.generate();

			assertThat(a).as("자기 자신과 동등").isEqualTo(a);
			assertThat(a).as("kid가 다르면 다른 객체").isNotEqualTo(b);
			assertThat(a).as("hashCode도 kid 기준으로 일관").hasSameHashCodeAs(a);
		}
	}
}
