package com.gmoon.hibernatetype.global.crypt;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

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
