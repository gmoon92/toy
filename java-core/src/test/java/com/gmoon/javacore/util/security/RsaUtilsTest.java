package com.gmoon.javacore.util.security;

import static org.assertj.core.api.Assertions.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;

import com.gmoon.javacore.util.PropertiesUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RsaUtilsTest {
	final String PATH_OF_PROPERTIES = "security/rsa/key.properties";

	RsaUtils.KeyPairHolder keyPair;

	@BeforeEach
	void setUp() {
		keyPair = RsaUtils.KeyPairHolder.create();
	}

	@Test
	@DisplayName("RSA KeyPair 생성")
	void testCreateKeyPair() {
		// when then
		log.info("keyPair: {}", keyPair);
		assertThat(keyPair.getPublicKey()).isNotNull();
		assertThat(keyPair.getPrivateKey()).isNotNull();
	}

	@Test
	@DisplayName("RSA 암복호화")
	void testEncryptAndDecrypt() {
		// given
		String plainText = "gmoon";

		// when
		String cipherText = RsaUtils.encode(keyPair.getPublicKey(), plainText);

		// then
		assertThat(RsaUtils.decode(keyPair.getPrivateKey(), cipherText))
			.isEqualTo(plainText);
	}

	@Test
	@DisplayName("RSA 공개키/비공개키 생성")
	void testGenerateKeyPair() {
		// given
		Properties properties = PropertiesUtils.load(PATH_OF_PROPERTIES);
		String privateKeyText =  properties.getProperty("private");
		String publicKeyText = properties.getProperty("public");

		// when
		RsaUtils.KeyPairHolder keyPair = RsaUtils.KeyPairHolder.create(privateKeyText, publicKeyText);
		PublicKey publicKey = keyPair.getPublicKey();
		PrivateKey privateKey = keyPair.getPrivateKey();

		// then
		assertThat(Base64Utils.encodeToString(publicKey.getEncoded())).isEqualTo(publicKeyText);
		assertThat(Base64Utils.encodeToString(privateKey.getEncoded())).isEqualTo(privateKeyText);
	}
}
