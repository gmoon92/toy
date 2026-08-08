package com.gmoon.springsecurityjose.jose.jws;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class JwsUtil {

	private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.ES256;

	private JwsUtil() {
	}

	public static ECKey generateEcKey(String kid) {
		try {
			return new ECKeyGenerator(Curve.P_256)
				 .keyID(kid)
				 .algorithm(ALGORITHM)
				 .generate();
		} catch (JOSEException e) {
			throw new IllegalStateException("Failed to generate EC key: kid=" + kid, e);
		}
	}

	public static String sign(ECKey signingKey, JWTClaimsSet claims) {
		try {
			JWSHeader header = new JWSHeader.Builder(ALGORITHM)
				 .type(JOSEObjectType.JWT)
				 .keyID(signingKey.getKeyID())
				 .build();

			SignedJWT signedJWT = new SignedJWT(header, claims);
			signedJWT.sign(new ECDSASigner(signingKey));
			return signedJWT.serialize();
		} catch (JOSEException e) {
			throw new IllegalStateException("Failed to sign JWS", e);
		}
	}

	public static JWTClaimsSet verify(String token, ECKey publicKey) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			if (!signedJWT.verify(new ECDSAVerifier(publicKey))) {
				throw new IllegalArgumentException("JWS signature verification failed: tampered or key mismatch");
			}

			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
			Date expiration = claims.getExpirationTime();
			if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
				throw new IllegalArgumentException("JWS expired: exp=" + expiration.toInstant());
			}
			return claims;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Failed to parse JWS: malformed serialization", e);
		} catch (JOSEException e) {
			throw new IllegalStateException("Failed to process JWS verification", e);
		}
	}

	public static ECKey parseEcKey(String jwkJson) {
		try {
			return ECKey.parse(jwkJson);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid JWK JSON", e);
		}
	}
}
