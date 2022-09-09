package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.javacore.persistence.EmbeddedId;
import com.gmoon.javacore.persistence.Entity;
import com.gmoon.javacore.persistence.Id;
import com.gmoon.javacore.test.domain.Favorites;
import com.gmoon.javacore.test.domain.User;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReflectionUtilsTest {

	@DisplayName("어노테이션이 선언된 클래스 목록 조회")
	@Test
	void getDeclaredAnnotationClasses() {
		// given
		Class<Entity> annotation = Entity.class;

		// when then
		assertThat(ReflectionUtils.getDeclaredAnnotationClasses(annotation))
			.isInstanceOf(Set.class)
			.contains(User.class, Favorites.class);
	}

	@DisplayName("지정된 어노테이션이 선언된 필드 목록 조회")
	@Test
	void getFieldsByDeclaredAnnotation() {
		// given
		Class<?> entity = Favorites.class;
		Class<? extends Annotation>[] annotations = new Class[] { Id.class, EmbeddedId.class };

		// when
		List<Field> ids = ReflectionUtils.getDeclaredAnnotationFields(entity, annotations);

		// then
		assertThat(ids)
			.isNotEmpty()
			.allMatch(
				field -> Stream.of(annotations)
					.anyMatch(clazz -> field.getAnnotation(clazz) != null)
			);
	}

	@DisplayName("현 클래스에 선언된 애노테이션 필드가 없다면" +
		"재귀 방식으로 상위 클래스 필드 조회")
	@Test
	void getDeclaredAnnotationFieldsByUsingRecursive() {
		// given
		Class<?> entity = User.class;
		Class<? extends Annotation>[] annotations = new Class[] { Id.class, EmbeddedId.class };

		// when
		List<Field> fields = ReflectionUtils.getDeclaredAnnotationFieldsByUsingRecursive(entity, annotations);

		// then
		assertThat(fields)
			.isNotEmpty()
			.map(field -> ReflectionUtils.existsDeclaredAnnotation(field, annotations))
			.containsExactly(Boolean.TRUE);
	}

	@DisplayName("지정된 private 필드 값 검증")
	@Test
	void getFieldValues() {
		// given
		Class<?> entity = User.class;
		Class<? extends Annotation>[] annotations = new Class[] { Id.class, EmbeddedId.class };

		List<Field> fields = ReflectionUtils.getDeclaredAnnotationFieldsByUsingRecursive(entity, annotations);

		// when
		UUID id = UUID.randomUUID();
		User user = new User(id);
		List<Object> values = ReflectionUtils.getFieldValues(user, fields);

		// then
		assertThat(values).contains(id);
	}
}
