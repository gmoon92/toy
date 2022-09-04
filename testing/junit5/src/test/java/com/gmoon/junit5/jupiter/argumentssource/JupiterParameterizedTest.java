package com.gmoon.junit5.jupiter.argumentssource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumingThat;
import static org.mockito.Mockito.when;
import com.gmoon.junit5.jupiter.argumentssource.contants.Role;
import com.gmoon.junit5.member.MemberRepository;
import com.gmoon.junit5.member.MemberService;
import com.gmoon.junit5.member.SystemException;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link org.junit.jupiter.params.provider.ArgumentsSource}
 * {@link org.junit.jupiter.params.provider.ArgumentsProvider}
 * <p>
 * {@link org.junit.jupiter.params.provider.ValueSource}
 * {@link org.junit.jupiter.params.provider.MethodSource}
 */
@ExtendWith(MockitoExtension.class)
class JupiterParameterizedTest {

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

	@DisplayName("이넘 클래스 인수 테스트")
	@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
	@EnumSource(Role.class)
	void enumSource(Role role) {
		// given
		int value = role.getValue();

		// when then
		assertThat(Role.ALL.get(value))
			.isEqualTo(role);
	}

	@DisplayName("Null 인수 테스트")
	@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
	@NullSource
	void nullSource(String actual) {
		assertThat(Objects.isNull(actual)).isTrue();
	}

	@DisplayName("empty 인수 테스트")
	@Nested
	class EmptySourceTest {

		@DisplayName("빈 문자열")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@EmptySource
		void case1(String actual) {
			assertThat(actual).isBlank();
		}

		@DisplayName("빈 리스트")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@EmptySource
		void case2(List<String> actual) {
			assertThat(actual).isEmpty();
		}

		@DisplayName("빈 원시 타입 배열")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@EmptySource
		void case3(byte[] actual) {
			assertThat(actual).isEmpty();
		}

		@DisplayName("빈 배열")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@EmptySource
		void case4(Integer[] actual) {
			assertThat(actual).isEmpty();
		}
	}

	@DisplayName("null 또는 빈 문자열")
	@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
	@NullAndEmptySource
	void nullAndEmptySource(String actual) {
		assertThat(StringUtils.isBlank(actual)).isTrue();
	}

	@DisplayName("값 인수 테스트")
	@Nested
	class ValueSourceTest {

		@InjectMocks
		MemberService memberService;

		@Mock
		MemberRepository memberRepository;

		@DisplayName("값 인수 테스트")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@ValueSource(ints = {1, 2, 3, 4})
		void case1(int actual) {
			assertThat(actual > 0 && actual < 4).isTrue();
		}

		@DisplayName("예외 클래스 테스트")
		@ParameterizedTest(name = "{displayName}[{index}] - {argumentsWithNames}")
		@ValueSource(classes = { IllegalArgumentException.class, EntityNotFoundException.class, PersistenceException.class })
		void case2(Class<? extends RuntimeException> exception) {
			// given
			Long memberId = 0L;

			// when
			when(memberRepository.findById(memberId))
				.thenThrow(exception);

			// then
			assertThatThrownBy(() -> memberService.disable(memberId))
				.isInstanceOf(SystemException.class);
		}
	}
}
