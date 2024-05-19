package com.gmoon.javacore.util.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Ase256UtilsTest {

	@Test
	void testEncodeAndDecode() {
		// given
		String plainText = "gmoon";

		// when
		String encrypt = Ase256Utils.encode(plainText);

		// then
		assertThat(Ase256Utils.decode(encrypt))
			 .isEqualTo(plainText);
	}
}
