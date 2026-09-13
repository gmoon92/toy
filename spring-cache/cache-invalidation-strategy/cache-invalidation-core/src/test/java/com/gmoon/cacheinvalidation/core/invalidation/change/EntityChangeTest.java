package com.gmoon.cacheinvalidation.core.invalidation.change;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("엔티티 변경 이벤트")
class EntityChangeTest {

	private static final String[] PROPERTY_NAMES = {"username", "email"};

	@Nested
	@DisplayName("수정 이벤트는 이전 상태를 함께 싣는다")
	class WhenUpdated {

		private final EntityChange change = EntityChange.updated(
			 new Object(), EntityState.of(PROPERTY_NAMES, new Object[] {"before", "before@mail.com"}));

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

		private final EntityChange change = EntityChange.inserted(new Object());

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

			assertThat(EntityChange.inserted(entity))
				 .as("변경 종류를 잃으면 규칙이 등록과 삭제를 구분할 수 없는 같은 신호로 받는다")
				 .isNotEqualTo(EntityChange.deleted(entity));
		}
	}
}
