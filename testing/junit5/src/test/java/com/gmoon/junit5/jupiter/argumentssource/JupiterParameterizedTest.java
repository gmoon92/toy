package com.gmoon.junit5.jupiter.argumentssource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link org.junit.jupiter.params.ParameterizedTestExtension}
 * {@link org.junit.jupiter.params.provider.ArgumentsSource}
 * {@link org.junit.jupiter.params.provider.ArgumentsProvider}
 * <p>
 * {@link org.junit.jupiter.params.provider.CsvFileSource}
 * {@link org.junit.jupiter.params.provider.CsvSource}
 * {@link org.junit.jupiter.params.provider.EmptySource}
 * {@link org.junit.jupiter.params.provider.EnumSource}
 * {@link org.junit.jupiter.params.provider.MethodSource}
 * {@link org.junit.jupiter.params.provider.NullSource}
 * {@link org.junit.jupiter.params.provider.ValueSource}
 */
public class JupiterParameterizedTest {

	@DisplayName("사원 CSV 파일 데이터 검증")
	@ParameterizedTest(name = "[{index}] {0}({1})... arguments: {arguments}")
	@CsvFileSource(resources = "/user.csv", delimiter = '|', numLinesToSkip = 1)
	void csvFilesSource(String userName, Boolean enabled) {
		assumingThat(Boolean.TRUE == enabled,
			() -> assertThat(userName).isEqualTo("gmoon")
		);

		assumingThat(Boolean.FALSE == enabled,
			() -> assertThat(userName)
				.containsAnyOf("guest", "super, moon")
		);
	}

	@DisplayName("사원 CSV 데이터 검증")
	@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
	@CsvSource(value = {
		"gmoon|true",
		"'super, moon'|false",
		"guest|false"
	}, delimiter = '|')
	void csvSource(String userName, Boolean enabled) {
		assumingThat(Boolean.TRUE == enabled,
			() -> assertThat(userName).isEqualTo("gmoon")
		);

		assumingThat(Boolean.FALSE == enabled,
			() -> assertThat(userName)
				.containsAnyOf("guest", "super, moon")
		);
	}
}
