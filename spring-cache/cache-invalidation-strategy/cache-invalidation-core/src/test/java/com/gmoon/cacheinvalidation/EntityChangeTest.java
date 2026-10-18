package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("엔티티 변경 이벤트")
class EntityChangeTest {

	private static final String[] PROPERTY_NAMES = {"username", "email"};

	@Nested
	@DisplayName("수정 이벤트는 이전 상태를 함께 싣는다")
	class WhenUpdated {

		private final EntityChange change = EntityChange.of(
			 new Object(), EntityChange.Type.UPDATE, EntityChange.State.of(PROPERTY_NAMES, new Object[] {"before", "before@mail.com"}));

		@Test
		@DisplayName("속성명으로 변경 전 값을 되찾는다")
		void findsPreviousValueByPropertyName() {
			assertThat(change.previousValueOf("username"))
				 .as("캐시 키가 username이면 옛 키를 이 값으로 재구성한다")
				 .contains("before");
		}

		@Test
		@DisplayName("존재하지 않는 속성명은 빈 값을 반환한다")
		void returnsEmptyForUnknownProperty() {
			assertThat(change.previousValueOf("unknown")).isEmpty();
		}
	}

	@Nested
	@DisplayName("등록 이벤트는 이전 상태가 없다")
	class WhenInserted {

		private final EntityChange change = EntityChange.of(new Object(), EntityChange.Type.INSERT);

		@Test
		@DisplayName("어떤 속성을 물어도 빈 값을 반환한다")
		void hasNoPreviousValue() {
			assertThat(change.previousValueOf("username")).isEmpty();
		}

	}

	@Nested
	@DisplayName("같은 엔티티를 담아도")
	class WhenSameEntity {

		@Test
		@DisplayName("등록과 삭제는 서로 다른 변경이다")
		void insertAndDeleteAreDistinct() {
			Object entity = new Object();

			assertThat(EntityChange.of(entity, EntityChange.Type.INSERT))
				 .as("변경 종류를 잃으면 규칙이 등록과 삭제를 구분할 수 없는 같은 신호로 받는다")
				 .isNotEqualTo(EntityChange.of(entity, EntityChange.Type.DELETE));
		}
	}

	@Nested
	@DisplayName("변경 전 상태는")
	class PreviousState {

		@Nested
		@DisplayName("속성명으로 값을 조회하면")
		class WhenLookedUpByPropertyName {

			private final EntityChange.State state = EntityChange.State.of(PROPERTY_NAMES,
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

			private final EntityChange.State state = EntityChange.State.of(PROPERTY_NAMES, new Object[] {"before", null});

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
				EntityChange.State one = EntityChange.State.of(PROPERTY_NAMES, new Object[] {"before", null});
				EntityChange.State other = EntityChange.State.of(PROPERTY_NAMES, new Object[] {"before", null});

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
				EntityChange.State state = EntityChange.State.of(PROPERTY_NAMES, source);

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
				assertThat(EntityChange.State.of(null, null))
					 .as("하이버네이트는 oldState 를 주지 않는 경우가 있고, 그때 규칙이 NPE 로 깨지면 안 된다")
					 .isEqualTo(EntityChange.State.EMPTY);
			}
		}

		@Nested
		@DisplayName("보관된 목록은")
		class WhenExposedList {

			@Test
			@DisplayName("수정할 수 없다")
			void isUnmodifiable() {
				EntityChange.State state = EntityChange.State.of(PROPERTY_NAMES, new Object[] {"before", "x"});

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
				EntityChange.State state = new EntityChange.State(List.of("username", "email"), List.of("before"));

				assertThat(state.valueOf("email")).isEmpty();
			}
		}
	}
}
