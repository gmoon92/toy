package com.gmoon.javacore.util.security;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RsaUtils {
	private static final int KEY_SIZE = 2048;
	private static final String ALGORITHM = "RSA";
	private static final String ALGORITHM_OF_SIGNATURE = "SHA256withRSA";
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	public static String encode(PublicKey publicKey, String plainText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			byte[] encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
			return encodeBase64String(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String encodeBase64String(byte[] encrypted) {
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public static String decode(PrivateKey privateKey, String cipherText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			byte[] decoded = decodeBase64String(cipherText);
			return new String(cipher.doFinal(decoded), CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String sign(PrivateKey privateKey, String data) {
		try {
			Signature signature = Signature.getInstance(ALGORITHM_OF_SIGNATURE);
			signature.initSign(privateKey);
			signature.update(data.getBytes(CHARSET));
			byte[] encodedHex = signature.sign();
			return encodeBase64String(encodedHex);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean verify(PublicKey publicKey, String signatureText, String data) {
		try {
			Signature signature = Signature.getInstance(ALGORITHM_OF_SIGNATURE);
			signature.initVerify(publicKey);
			signature.update(data.getBytes(CHARSET));
			byte[] decodedHex = decodeBase64String(signatureText);
			return signature.verify(decodedHex);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] decodeBase64String(String signatureText) {
		return Base64.getDecoder().decode(signatureText);
	}

	public static PublicKey convertToPublicKeyFromString(String publicKeyText) {
		return KeyPairHolder.NO_OP.createPublicKey(publicKeyText);
	}

	public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) {
		return KeyPairHolder.getPublicKeyFromPrivateKey(privateKey);
	}

	@RequiredArgsConstructor
	@Getter
	static class KeyPairHolder {
		private static final KeyPairHolder NO_OP = new KeyPairHolder(null, null);
		private final PrivateKey privateKey;
		private final PublicKey publicKey;

		public static KeyPairHolder create() {
			KeyPair keyPair = generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			return new KeyPairHolder(privateKey, publicKey);
		}

		public static KeyPairHolder create(String privateKeyText, String publicKeyText) {
			PrivateKey privateKey = NO_OP.createPrivateKey(privateKeyText);
			PublicKey publicKey = NO_OP.createPublicKey(publicKeyText);
			return new KeyPairHolder(privateKey, publicKey);
		}

		private static KeyPair generateKeyPair() {
			try {
				KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
				// generator.initialize(KEY_SIZE, JCAUtil.getSecureRandom());
				generator.initialize(KEY_SIZE);
				return generator.generateKeyPair();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) {
			try {
				RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey)privateKey;
				BigInteger modulus = rsaPrivateCrtKey.getModulus();
				BigInteger publicExponent = rsaPrivateCrtKey.getPublicExponent();
				RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);

				return KeyFactory.getInstance(ALGORITHM).generatePublic(publicKeySpec);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private PrivateKey createPrivateKey(String privateKeyText) {
			try {
				byte[] decoded = decodeBase64String(privateKeyText);
				EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
				return KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private PublicKey createPublicKey(String publicKeyText) {
			try {
				byte[] decoded = decodeBase64String(publicKeyText);
				EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
				return KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			String privateKey = Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
			String publicKey = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
			return String.format("private: %s, publicKey: %s", privateKey, publicKey);
		}
	}
}
