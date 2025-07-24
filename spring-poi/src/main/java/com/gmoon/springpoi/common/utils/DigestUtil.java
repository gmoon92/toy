package com.gmoon.springpoi.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DigestUtil {
	private static final String SHA_256 = "SHA-256";

	public static String sha256(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance(SHA_256);
			md.digest(input.getBytes(StandardCharsets.UTF_8));
			byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

			StringBuilder result = new StringBuilder(hashBytes.length * 2);
			for (byte b : hashBytes) {
				result.append(String.format("%02x", b));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Invalid hashing algorithm: " + SHA_256, e);
		}
	}
}
