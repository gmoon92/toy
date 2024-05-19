package com.gmoon.springsecuritypasswordencoder.password;

import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtils {
	private static final PasswordEncoderHolder DEFAULT_ENCODE = PasswordEncoderHolder.BCRYPT;
	private static final PasswordEncoder PASSWORD_ENCODER;

	static {
		DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder(
			 DEFAULT_ENCODE.getId(),
			 PasswordEncoderHolder.ALL,
			 PasswordEncoderHolder.ID_PREFIX, PasswordEncoderHolder.ID_SUFFIX);
		delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(DEFAULT_ENCODE.getValue());

		PASSWORD_ENCODER = delegatingPasswordEncoder;
	}

	public static boolean matches(String plainPassword, String encodedPassword) {
		return PASSWORD_ENCODER.matches(plainPassword, encodedPassword);
	}

	public static boolean isNeedToUpgradeEncoding(String encodedPassword) {
		return PASSWORD_ENCODER.upgradeEncoding(encodedPassword);
	}
}
