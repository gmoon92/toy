package com.gmoon.hibernatetype.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CryptoUtilsTest {

	private SecretKeySpec keySpec;

	@BeforeEach
	void setUp() {
		byte[] key = CryptoUtils.encryptXOR(UUID.randomUUID().toString(), 128);
		keySpec = new SecretKeySpec(key, "AES");
	}

	@Test
	void encryptAndDecrypt() {
		String plain = "gmoon";

		String encrypted = CryptoUtils.encrypt(plain, keySpec);
		String decrypted = CryptoUtils.decrypt(encrypted, keySpec);

		assertThat(decrypted).isEqualTo(plain);
	}
}
