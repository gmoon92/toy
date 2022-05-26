package com.gmoon.javacore.util.security;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Ase256Utils {

	// padding, CBC(Cipher Block Chaining)
	// PKCS#5: 8바이트 기반 암호화 블록 패딩 처리 방식
	// PKCS#7: 16바이트 기반 암호화 블록 패딩 처리 방식
	// {alg}/{mode}/{padding}
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

	private static final SecretKeySpec KEY_SPEC;
	private static final IvParameterSpec IV_SPEC;

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	static {
		// 키의 길이가 32 이면 AES-256
		// 키의 길이가 24 이면 AES-192
		// 키의 길이가 16 이면 AES-128
		String secretKey = "f54ceaa2f4444adf91ad0abcb726007d";
		// CBC 블록 암호 운용 방식이라면 IV(Initialization Vector) 값 필수
		String iv = secretKey.substring(0, 16);

		KEY_SPEC = new SecretKeySpec(secretKey.getBytes(CHARSET), "AES");
		IV_SPEC = new IvParameterSpec(iv.getBytes(CHARSET));
	}

	public static String encode(String plainText) {
		try {
			Cipher cipher = newEncrypt();
			byte[] encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String decode(String cipherText) {
		Cipher cipher = newDecrypt();

		byte[] decodeBytes = Base64.getDecoder().decode(cipherText);
		try {
			return new String(cipher.doFinal(decodeBytes), CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// non thread safe
	private static Cipher newEncrypt() {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, KEY_SPEC, IV_SPEC);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Cipher newDecrypt() {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, KEY_SPEC, IV_SPEC);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
