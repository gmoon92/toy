package com.gmoon.javacore.util.security;

import static org.assertj.core.api.Assertions.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
	@DisplayName("RSA KeyPair 생성, 키 크기는 1024, 2048 권장")
	void testCreateKeyPair() {
		// given
		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublicKey();
		RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivateKey();

		// when then
		int keySizeOfRSA = 2048;
		assertThat(publicKey.getModulus().bitLength()).isEqualTo(keySizeOfRSA);
		assertThat(privateKey.getModulus().bitLength()).isEqualTo(keySizeOfRSA);
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
	@DisplayName("RSA 공개키/개인키 생성")
	void testGenerateKeyPair() {
		// given
		Properties properties = PropertiesUtils.load(PATH_OF_PROPERTIES);
		String privateKeyText = properties.getProperty("private");
		String publicKeyText = properties.getProperty("public");

		// when
		RsaUtils.KeyPairHolder keyPair = RsaUtils.KeyPairHolder.create(privateKeyText, publicKeyText);
		PublicKey publicKey = keyPair.getPublicKey();
		PrivateKey privateKey = keyPair.getPrivateKey();

		// then
		assertThat(RsaUtils.encodeBase64String(publicKey.getEncoded())).isEqualTo(publicKeyText);
		assertThat(RsaUtils.encodeBase64String(privateKey.getEncoded())).isEqualTo(privateKeyText);
	}

	@Test
	@DisplayName("private key 기준으로 public key 추출")
	void testGetPublicKeyFromPrivateKey() {
		// given
		PrivateKey privateKey = keyPair.getPrivateKey();

		// when
		PublicKey publicKey = RsaUtils.getPublicKeyFromPrivateKey(privateKey);

		// then
		assertThat(publicKey).isEqualTo(keyPair.getPublicKey());
	}

	@Nested
	@DisplayName("RSA 디지털 서명")
	class HashAndDigitalSigning {

		@Test
		@DisplayName("SHA-256 알고리즘으로 원본 데이터를 Hashing "
			 + "이후 개인키로 암호화, Signature 생성")
		void testSign() {
			// given
			PrivateKey privateKey = keyPair.getPrivateKey();
			String plainText = "gmoon";

			// when
			String signature = RsaUtils.sign(privateKey, plainText);

			// then
			int sha256BitSize = 256;
			assertThat(RsaUtils.decodeBase64String(signature).length)
				 .isEqualTo(sha256BitSize);
		}

		@Test
		@DisplayName("공개키를 사용하여 서명 검증")
		void testSignAndVerify() {
			// given
			PrivateKey privateKey = keyPair.getPrivateKey();
			PublicKey publicKey = keyPair.getPublicKey();

			String data = "gmoon";
			String signature = RsaUtils.sign(privateKey, data);

			// when
			boolean verified = RsaUtils.verify(publicKey, signature, data);

			// then
			assertThat(verified).isTrue();
		}

		@Test
		@DisplayName("위변조된 '공개키'를 사용하여 서명 실패 검증")
		void testSignAndVerifyFail() {
			// given
			PrivateKey privateKey = keyPair.getPrivateKey();
			Properties properties = PropertiesUtils.load(PATH_OF_PROPERTIES);
			String publicKeyText = properties.getProperty("public");
			PublicKey invalidPublicKey = RsaUtils.convertToPublicKeyFromString(publicKeyText);

			String data = "gmoon";
			String signature = RsaUtils.sign(privateKey, data);

			// when
			boolean verified = RsaUtils.verify(invalidPublicKey, signature, data);

			// then
			assertThat(verified).isFalse();
		}

		@Test
		@DisplayName("위변조된 '데이터'를 사용하여 서명 실패 검증")
		void testSignAndVerifyFailWhenForgedData() {
			// given
			PrivateKey privateKey = keyPair.getPrivateKey();
			Properties properties = PropertiesUtils.load(PATH_OF_PROPERTIES);
			String publicKeyText = properties.getProperty("public");
			PublicKey invalidPublicKey = RsaUtils.convertToPublicKeyFromString(publicKeyText);

			String data = "gmoon";
			String signature = RsaUtils.sign(privateKey, data);

			// when
			String forgedData = "gmoon2";
			boolean verified = RsaUtils.verify(invalidPublicKey, signature, forgedData);

			// then
			assertThat(verified).isFalse();
		}
	}
}
