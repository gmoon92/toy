package com.gmoon.springsecuritypasswordencoder.password;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
enum PasswordEncoderHolder {
	BCRYPT("bcrypt", new BCryptPasswordEncoder()), // 60
	MD4("MD4", new Md4PasswordEncoder()), // 32
	MD5("MD5", new MessageDigestPasswordEncoder("MD5")), // 32
	NOOP("noop", NoOpPasswordEncoder.getInstance()),
	PBKDF2("pbkdf2", new Pbkdf2PasswordEncoder()), // 32
	SHA1("SHA-1", new MessageDigestPasswordEncoder("SHA-1")), // 40
	SHA256("SHA-256", new MessageDigestPasswordEncoder("SHA-256")), // 64
	ARGON2("argon2", new Argon2PasswordEncoder()); // 66

	public static final Map<String, PasswordEncoder> ALL = Arrays.stream(values())
		 .collect(collectingAndThen(
			  toMap(PasswordEncoderHolder::getId, PasswordEncoderHolder::getValue),
			  Collections::unmodifiableMap)
		 );

	public static final String ID_PREFIX = "{";
	public static final String ID_SUFFIX = "}";
	private static final String ID_FORMAT = ID_PREFIX + "%s" + ID_SUFFIX;

	private final String id;
	private final PasswordEncoder value;

	public String getPrefixPasswordId() {
		return String.format(ID_FORMAT, id);
	}
}
