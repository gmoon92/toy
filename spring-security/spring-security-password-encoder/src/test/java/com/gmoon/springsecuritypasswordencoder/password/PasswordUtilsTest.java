package com.gmoon.springsecuritypasswordencoder.password;

import static org.assertj.core.api.Assertions.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PasswordUtilsTest {

	@DisplayName("기본 bcyprt 암호화 방식, 패스워드 ID 접두사 유무 검증")
	@Test
	void testMatches() {
		// given
		String rawPassword = "gmoon";

		// when
		String bcryptPassword = "$2a$10$d0QeuUkP.eRZnIXF66LPauq2NI6CYse/tmjml1M9PMsCw66z.TR5q";
		String prefixEncodedPassword = PasswordEncoderHolder.BCRYPT.getPrefixPasswordId()
			 + "$2a$10$4hunjhgMVwMU6cNKXKGG/eqWd/FYY/BsVc5h8rFv.d.YRhnw20KBK";

		// then
		assertThat(PasswordUtils.matches(rawPassword, bcryptPassword)).isTrue();
		assertThat(PasswordUtils.matches(rawPassword, prefixEncodedPassword)).isTrue();
	}

	@DisplayName("SHA1 패스워드 검증"
		 + "패스워드 ID 접두사가 없을 경우 기본 bcrypt 암호화 방식 설정으로 false")
	@Test
	void testMatchesWhenSha1() {
		// given
		String plainPassword = "gmoon";

		// when
		String sha1Password = encodeSH1(plainPassword);

		// then
		assertThat(PasswordUtils.matches(plainPassword, PasswordEncoderHolder.SHA1.getPrefixPasswordId()
			 + sha1Password)).isTrue();
		assertThat(PasswordUtils.matches(plainPassword, sha1Password)).isFalse();
	}

	private String encodeSH1(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.reset();
			digest.update(value.getBytes(StandardCharsets.UTF_8));
			return String.format("%040x", new BigInteger(1, digest.digest()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@DisplayName("패스워드 업그레이드가 필요한지 검증"
		 + "접두사가 없을 경우 -> true"
		 + "접두사가 있지만 기본 인코더 방식을 따르지 않을 경우 -> true")
	@Test
	void testIsNeedToUpgradeEncoding() {
		String bcrypt = "$2a$10$d0QeuUkP.eRZnIXF66LPauq2NI6CYse/tmjml1M9PMsCw66z.TR5q";
		String bcryptWithPrefixId = PasswordEncoderHolder.BCRYPT.getPrefixPasswordId() + bcrypt;

		String sha1 = encodeSH1("$2a$10$d0QeuUkP.eRZnIXF66LPauq2NI6CYse/tmjml1M9PMsCw66z.TR5q");
		String sha1WithPrefixId = PasswordEncoderHolder.SHA1.getPrefixPasswordId() + sha1;

		// then
		assertThat(PasswordUtils.isNeedToUpgradeEncoding(bcryptWithPrefixId)).isFalse();

		assertThat(PasswordUtils.isNeedToUpgradeEncoding(bcrypt)).isTrue();
		assertThat(PasswordUtils.isNeedToUpgradeEncoding(sha1)).isTrue();
		assertThat(PasswordUtils.isNeedToUpgradeEncoding(sha1WithPrefixId)).isTrue();
	}
}
