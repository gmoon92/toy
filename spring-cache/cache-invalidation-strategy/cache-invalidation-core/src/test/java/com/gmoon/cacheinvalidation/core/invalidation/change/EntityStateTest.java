package com.gmoon.cacheinvalidation.core.invalidation.change;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("변경 전 상태")
class EntityStateTest {

	private static final String[] PROPERTY_NAMES = {"username", "email"};

	@Nested
	@DisplayName("속성명으로 값을 조회하면")
	class WhenLookedUpByPropertyName {

		private final EntityState state = EntityState.of(PROPERTY_NAMES,
			 new Object[] {"before", "before@mail.com"});

		@Test
		@DisplayName("같은 위치의 값을 반환한다")
		void returnsValueAtSameIndex() {
			assertThat(state.valueOf("email")).contains("before@mail.com");
		}

		@Test
		@DisplayName("모르는 속성명은 빈 값을 반환한다")
		void returnsEmptyForUnknownProperty() {
			assertThat(state.valueOf("unknown")).isEmpty();
		}
	}

	@Nested
	@DisplayName("값이 null인 속성은")
	class WhenValueIsNull {

		private final EntityState state = EntityState.of(PROPERTY_NAMES, new Object[] {"before", null});

		@Test
		@DisplayName("예외 없이 빈 값을 반환한다")
		void returnsEmptyWithoutThrowing() {
			assertThat(state.valueOf("email"))
				 .as("Hibernate의 oldState는 null을 포함할 수 있다")
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("내용이 같은 두 상태는")
	class WhenContentsAreEqual {

		@Test
		@DisplayName("서로 동등하다")
		void areEqualToEachOther() {
			EntityState one = EntityState.of(PROPERTY_NAMES, new Object[] {"before", null});
			EntityState other = EntityState.of(PROPERTY_NAMES, new Object[] {"before", null});

			assertThat(one)
				 .as("배열을 record 컴포넌트로 두면 참조 비교가 되어 동등성이 깨진다")
				 .isEqualTo(other)
				 .hasSameHashCodeAs(other);
		}
	}

	@Nested
	@DisplayName("원본 배열이 나중에 바뀌어도")
	class WhenSourceArrayMutatedLater {

		@Test
		@DisplayName("보관된 값은 영향을 받지 않는다")
		void keepsSnapshotUnaffected() {
			Object[] source = {"before", "before@mail.com"};
			EntityState state = EntityState.of(PROPERTY_NAMES, source);

			source[0] = "mutated";

			assertThat(state.valueOf("username")).contains("before");
		}
	}

	@Nested
	@DisplayName("배열이 없으면")
	class WhenArraysAreAbsent {

		@Test
		@DisplayName("빈 상태로 취급한다")
		void treatedAsEmpty() {
			assertThat(EntityState.of(null, null)).isEqualTo(EntityState.EMPTY);
			assertThat(EntityState.EMPTY.isEmpty()).isTrue();
		}
	}

	@Nested
	@DisplayName("보관된 목록은")
	class WhenExposedList {

		@Test
		@DisplayName("수정할 수 없다")
		void isUnmodifiable() {
			EntityState state = EntityState.of(PROPERTY_NAMES, new Object[] {"before", "x"});

			assertThatThrownBy(() -> state.values().add("injected"))
				 .isInstanceOf(UnsupportedOperationException.class);
		}
	}

	@Nested
	@DisplayName("속성명보다 값이 적으면")
	class WhenValuesAreShorterThanNames {

		@Test
		@DisplayName("범위를 벗어난 속성은 빈 값을 반환한다")
		void returnsEmptyForOutOfRange() {
			EntityState state = new EntityState(List.of("username", "email"), List.of("before"));

			assertThat(state.valueOf("email")).isEmpty();
		}
	}
}
