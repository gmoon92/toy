package com.gmoon.hibernatetype.global.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnEncryptionConstants {

	private static final String SECRET_KEY = "SECRETKEY";
	public static final String ENC_COLUMN = "HEX(AES_ENCRYPT(?, '" + SECRET_KEY + "'))";
	public static final String DEC_EMAIL = "CAST(AES_DECRYPT(UNHEX(email), '" + SECRET_KEY + "') AS CHAR)";
}
