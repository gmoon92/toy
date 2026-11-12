package com.gmoon.writeinvalidate.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.writeinvalidate.config.JpaEntityEventListener;

@DisplayName("하이버네이트 엔티티 이벤트 수신")
class JpaEntityEventListenerTest {

	private final RecordingListener listener = new RecordingListener();

	@Nested
	@DisplayName("하위 클래스가 타입 인자를 선언하면")
	class WhenSubclassDeclaresType {

		@Test
		@DisplayName("그 타입의 엔티티만 넘긴다")
		void forwardsOnlyDeclaredType() {
			listener.onPostUpdate(updateOf("text"));
			listener.onPostUpdate(updateOf(1L));

			assertThat(listener.received)
				 .as("선언하지 않은 엔티티까지 넘기면 구현체가 다시 타입을 확인해야 한다")
				 .containsExactly("text");
		}
	}

	@Nested
	@DisplayName("하위 클래스도 제네릭이면")
	class WhenSubclassIsGeneric {

		@Test
		@DisplayName("생성을 막는다")
		void rejectsConstruction() {
			assertThatIllegalStateException()
				 .as("읽을 타입이 없는 채로 뜨면 모든 이벤트가 조용히 버려진다")
				 .isThrownBy(GenericListener::new)
				 .withMessageContaining("must declare the supported entity type");
		}
	}

	@Nested
	@DisplayName("하이버네이트가 이벤트를 보내면")
	class WhenHibernatePublishes {

		@Test
		@DisplayName("저장된 엔티티를 넘긴다")
		void forwardsInserted() {
			PostInsertEvent event = mock(PostInsertEvent.class);
			given(event.getEntity()).willReturn("inserted");

			listener.onPostInsert(event);

			assertThat(listener.received).containsExactly("inserted");
		}

		@Test
		@DisplayName("수정된 엔티티를 넘긴다")
		void forwardsUpdated() {
			listener.onPostUpdate(updateOf("updated"));

			assertThat(listener.received).containsExactly("updated");
		}

		@Test
		@DisplayName("삭제된 엔티티를 넘긴다")
		void forwardsDeleted() {
			PostDeleteEvent event = mock(PostDeleteEvent.class);
			given(event.getEntity()).willReturn("deleted");

			listener.onPostDelete(event);

			assertThat(listener.received).containsExactly("deleted");
		}
	}

	private static PostUpdateEvent updateOf(Object entity) {
		PostUpdateEvent event = mock(PostUpdateEvent.class);
		given(event.getEntity()).willReturn(entity);
		return event;
	}

	static class RecordingListener extends JpaEntityEventListener<CharSequence> {

		private final List<CharSequence> received = new ArrayList<>();

		@Override
		protected void onEntityChanged(CharSequence entity) {
			received.add(entity);
		}
	}

	static class GenericListener<T> extends JpaEntityEventListener<T> {

		@Override
		protected void onEntityChanged(T entity) {
		}
	}
}
