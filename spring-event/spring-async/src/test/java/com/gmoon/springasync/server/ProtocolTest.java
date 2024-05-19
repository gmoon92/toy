package com.gmoon.springasync.server;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProtocolTest {
	@ParameterizedTest
	@MethodSource("useSslProtocolParams")
	@DisplayName("ssl을 사용 여부에 따라 프로토콜을 반환한다.")
	void of(boolean useSsl, Protocol expected) {
		// given

		// when

		// then
		assertThat(Protocol.of(useSsl))
			 .isEqualTo(expected);
	}

	static Stream<Arguments> useSslProtocolParams() {
		return Stream.of(
			 Arguments.of(true, Protocol.HTTPS),
			 Arguments.of(false, Protocol.HTTP)
		);
	}
}
