package com.gmoon.springsecurityjose.jose.jwk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

public final class JwkStore {

	private final Map<String, JsonWebKey> keys = new ConcurrentHashMap<>();

	public void register(JsonWebKey key) {
		keys.put(key.getId(), key);
	}

	public JsonWebKey get(String kid) {
		JsonWebKey key = keys.get(kid);
		if (key == null) {
			throw new IllegalArgumentException("No key registered for kid=" + kid);
		}
		return key;
	}

	public ECKey getSigningKey(String kid) {
		return get(kid).toSigningKey();
	}

	public ECKey getVerificationKey(String kid) {
		return get(kid).toVerificationKey();
	}

	public String toPublicJwkSet() {
		List<JWK> publicKeys = keys.values().stream()
			 .map(JsonWebKey::toVerificationKey)
			 .collect(Collectors.toList());
		return new JWKSet(publicKeys).toString();
	}
}
