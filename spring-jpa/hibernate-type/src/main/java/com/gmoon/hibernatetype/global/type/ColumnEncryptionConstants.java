package com.gmoon.hibernatetype.global.type;

import com.gmoon.hibernatetype.global.utils.CryptoUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.spec.SecretKeySpec;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnEncryptionConstants {

	public static final String SECRET_KEY = "SECRETKEY";
	public static final String ENC_COLUMN = "HEX(AES_ENCRYPT(?, '" + SECRET_KEY + "'))";
	public static final String DEC_EMAIL = "CAST(AES_DECRYPT(UNHEX(email), '" + SECRET_KEY + "') AS CHAR)";

	public static final SecretKeySpec KEY_SPEC;

	static {
		byte[] key = CryptoUtils.encryptXOR(SECRET_KEY, 128);
		KEY_SPEC = new SecretKeySpec(key, "AES");
	}
}
