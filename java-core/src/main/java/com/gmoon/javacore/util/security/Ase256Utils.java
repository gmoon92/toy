package com.gmoon.javacore.util.security;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ase256Utils {
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	public static String encode(String plainText) {
		try {
			Cipher cipher = CipherHolder.ENCRYPT;
			byte[] encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String decode(String cipherText) {
		Cipher cipher = CipherHolder.DECRYPT;

		byte[] decodeBytes = Base64.getDecoder().decode(cipherText);
		try {
			return new String(cipher.doFinal(decodeBytes), CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class CipherHolder {
		private static final Cipher ENCRYPT;
		private static final Cipher DECRYPT;

		// padding, CBC(Cipher Block Chaining)
		// PKCS#5: 8바이트 기반 암호화 블록 패딩 처리 방식
		// PKCS#7: 16바이트 기반 암호화 블록 패딩 처리 방식
		// {alg}/{mode}/{padding}
		private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

		static {
			// 키의 길이가 32 이면 AES-256
			// 키의 길이가 24 이면 AES-192
			// 키의 길이가 16 이면 AES-128
			String secretKey = "f54ceaa2f4444adf91ad0abcb726007d";
			// CBC 블록 암호 운용 방식이라면 IV(Initialization Vector) 값 필수
			String iv = secretKey.substring(0, 16);

			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(CHARSET), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));

			try {
				ENCRYPT = Cipher.getInstance(ALGORITHM);
				ENCRYPT.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

				DECRYPT = Cipher.getInstance(ALGORITHM);
				DECRYPT.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
