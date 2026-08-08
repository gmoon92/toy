package com.gmoon.springsecurityjose.jose.jwk;

import java.util.UUID;

import com.nimbusds.jose.jwk.ECKey;

import com.gmoon.springsecurityjose.jose.jws.JwsUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "id")
public final class JsonWebKey {

	private final String id;
	private final String privateKey;
	private final String publicKey;

	public JsonWebKey() {
		ECKey ecKey = JwsUtil.generateEcKey(UUID.randomUUID().toString());
		this.id = ecKey.getKeyID();
		this.privateKey = ecKey.toJSONString();
		this.publicKey = ecKey.toPublicJWK().toJSONString();
	}

	public static JsonWebKey generate() {
		return new JsonWebKey();
	}

	public ECKey toSigningKey() {
		return JwsUtil.parseEcKey(privateKey);
	}

	public ECKey toVerificationKey() {
		return JwsUtil.parseEcKey(publicKey);
	}
}
