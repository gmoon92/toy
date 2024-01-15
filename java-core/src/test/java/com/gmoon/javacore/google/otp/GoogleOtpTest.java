package com.gmoon.javacore.google.otp;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class GoogleOtpTest {

	@DisplayName("시크릿 키 생성")
	@Test
	void generatorSecret() {
		GoogleOtp googleOtp = new GoogleOtp();

		assertThat(googleOtp.getSecret()).isNotEmpty();
	}

	@Test
	void imageDataUrl() {
		GoogleOtp googleOtp = new GoogleOtp();

		assertThat(googleOtp.getImageDataUrl()).isNotEmpty();
	}

	@Disabled
	@Test
	void verify() {
		String secret = "AXVYWUDNNLQGCPWGNSUM6L5KYNZVRTNK";

		GoogleOtp googleOtp = new GoogleOtp(secret);

		assertThat(googleOtp.getImageDataUrl()).isEqualTo("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPoAAAD6AQAAAACgl2eQAAACg0lEQVR42u2ZQW6rQBBE22LBco4wN8EXswQSF4ObzBFYskB0qmomwY6+/tb99Y2UmAxvQY+rq3s65n+/FvsAH+AfBA7DNRa7bZb7FYv9ijsu5kAAPreH7TefvV+A7rftkfv6IA6AAEbf71jeEY/j2ZSBPqIBeHbnLuOngOo8IjByxQwLEMWSjhwOoB5yfyYEMBiUMSTFc0QCmEQA1u351+/UezPQLmQWNt2S7v5kYm8FEAXtCfuN9BroVkOaselpDgTgu1/SlP2UaCEKl5HefvQQA8BK5xWg21MeJjeIA+Bll7a3xWhPtCwzplckAH7UVbdHFIfJSPHnGAjwonzCM9RN32j0iUK+9BAAODIDsJ56gD2N0EPS2hVmAIDfPQJgFH7izu5bVyDaORKQq9s7jQrKKIhnZKKNgQDpQXtLINWGE5uOXikQ4Hrt2sGxwlOvz+kfAuCKF+kVemA+Ad1jAWqTaJ+KJ03GXim/CCYEgHwqLtEqvdhwSshxAOwyO+DaIdGZ5KEveggAcGXGM1oqc4x16bUDeT/ArYYz7TquGe9YN9XOBwLYEk3YW5UkHcx1nExHKIDH8XZSW9khab/PK/1jAGft5SYzVUu6fXOrMEA7Tra6WXsRacQDAcyu2oEU1KWZvt+xlk45EJDZF7l/R9E8dH1q9iIAtHfz1stN5DVCmEMB9HgdIq32Ikh/0wApDqBLBwq99dg89PKoCMD3HEbK5fJOvcpSAwGabrAkGQeYj1wPlsurR70d0El8b73czDmMZjOXiYUBXPbJI7oyX7U0HMBRdVHDXgeYy1PrHgGok/97PViOdQ4DYI4EKLOsjv8HAqjwRaPCOMDnn24f4H8DvgCSVpvTwmsX6gAAAABJRU5ErkJggg==");

		assertThat(googleOtp.verify("036736")).isTrue();
	}
}
