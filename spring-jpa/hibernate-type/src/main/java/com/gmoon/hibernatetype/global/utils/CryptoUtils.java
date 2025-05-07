package com.gmoon.hibernatetype.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * https://dev.mysql.com/doc/refman/8.0/en/encryption-functions.html#function_aes-encrypt
 * https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Electronic_codebook_.28ECB.29
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoUtils {

	private static final Charset UTF8 = StandardCharsets.UTF_8;

	public static byte[] encryptXOR(String plain, int bitLength) {
		int bitSizeConverted1Byte = 8;
		byte[] result = new byte[bitLength / bitSizeConverted1Byte];
		byte[] plainBytes = plain.getBytes(UTF8);
		for (int i = 0; i < plainBytes.length; i++) {
			result[i % result.length] ^= plainBytes[i];
		}
		return result;
	}

	public static String encrypt(String plain, SecretKeySpec keySpec) {
		try {
			Cipher cipher = newCipher(Cipher.ENCRYPT_MODE, keySpec);

			byte[] encrypted = cipher.doFinal(plain.getBytes(UTF8));
			String hex = new String(Hex.encodeHex(encrypted));
			return hex.toUpperCase();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(String encrypted, SecretKeySpec keySpec) {
		try {
			Cipher cipher = newCipher(Cipher.DECRYPT_MODE, keySpec);

			byte[] decodeHex = Hex.decodeHex(encrypted.toCharArray());
			return new String(cipher.doFinal(decodeHex));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Cipher newCipher(int mode, SecretKeySpec keySpec) {
		try {
			// Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Cipher cipher = Cipher.getInstance(keySpec.getAlgorithm());
			cipher.init(mode, keySpec);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
