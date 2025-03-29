package com.gmoon.springsecuritypasswordencoder.password;

import com.gmoon.javacore.util.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderHolderTest {

	@ParameterizedTest
	@ValueSource(ints = {1, 9, 10, 11, 72})
	void length(int length) {
		String rawPassword = StringUtils.randomAlphabetic(length);

		verifyEncryptedLength(PasswordEncoderHolder.NOOP, rawPassword, length);
		verifyEncryptedLength(PasswordEncoderHolder.BCRYPT, rawPassword, 60);
		verifyEncryptedLength(PasswordEncoderHolder.MD4, rawPassword, 32);
		verifyEncryptedLength(PasswordEncoderHolder.MD5, rawPassword, 32);
		verifyEncryptedLength(PasswordEncoderHolder.PBKDF2, rawPassword, 96);
		verifyEncryptedLength(PasswordEncoderHolder.SHA1, rawPassword, 86);
		verifyEncryptedLength(PasswordEncoderHolder.SHA256, rawPassword, 110);
		verifyEncryptedLength(PasswordEncoderHolder.ARGON2, rawPassword, 97);
	}

	private void verifyEncryptedLength(PasswordEncoderHolder encoder, String rawPassword, int expected) {
		String encoded = encoder.encode(rawPassword);
		if (PasswordEncoderHolder.MD4 == encoder || PasswordEncoderHolder.MD5 == encoder) {
			String prefixAndSuffix = "{}";
			int base64KeyLength = 44; // Base64StringKeyGenerator#DEFAULT_KEY_LENGTH
			int salt = base64KeyLength + prefixAndSuffix.length();
			expected += salt;
		}
		assertThat(encoded).hasSize(expected);
	}
}
