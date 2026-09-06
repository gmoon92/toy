package com.gmoon.cacheinvalidation.core.invalidation;

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
			 new Object(), 1L, new Object[] {"before", "before@mail.com"}, PROPERTY_NAMES);

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

		private final EntityChange change = EntityChange.inserted(new Object(), 1L);

		@Test
		@DisplayName("어떤 속성을 물어도 빈 값을 반환한다")
		void hasNoPreviousValue() {
			assertThat(change.previousValueOf("username")).isEmpty();
		}

		@Test
		@DisplayName("변경 유형은 INSERT 이다")
		void isInsertType() {
			assertThat(change.type()).isEqualTo(ChangeType.INSERT);
		}
	}

	@Nested
	@DisplayName("삭제 이벤트")
	class WhenDeleted {

		@Test
		@DisplayName("변경 유형은 DELETE 이다")
		void isDeleteType() {
			assertThat(EntityChange.deleted(new Object(), 1L).type()).isEqualTo(ChangeType.DELETE);
		}
	}
}
