package com.gmoon.javacore.util;

import static com.gmoon.javacore.util.ReflectionUtils.*;
import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.stereotype.Repository;

import com.gmoon.javacore.persistence.EmbeddedId;
import com.gmoon.javacore.persistence.Entity;
import com.gmoon.javacore.persistence.Id;
import com.gmoon.javacore.test.domain.Favorites;
import com.gmoon.javacore.test.domain.User;
import com.gmoon.javacore.test.repository.UserRepository;
import com.gmoon.javacore.util.exception.NotFoundGenericTypeClassException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ReflectionUtilsTest {

	@DisplayName("private 접근제한자 인스턴스 생성")
	@Test
	void newInstance() {
		assertThat(ReflectionUtils.newInstance(ModifierPrivateClass.class))
			.isNotNull();
	}

	static class ModifierPrivateClass {

		private ModifierPrivateClass() {
		}
	}

	@DisplayName("어노테이션이 선언된 클래스 목록 조회")
	@Test
	void getDeclaredAnnotationClasses() {
		Set<Class<?>> declaredAnnotationClasses = ReflectionUtils.getDeclaredAnnotationClasses(
			Entity.class,
			Repository.class
		);

		log.info("declared: {}", declaredAnnotationClasses);
		assertThat(declaredAnnotationClasses)
			.isInstanceOf(Set.class)
			.contains(User.class, Favorites.class, UserRepository.class);
	}

	@DisplayName("지정된 어노테이션이 선언된 필드 목록 조회")
	@Test
	void getFieldsByDeclaredAnnotation() {
		// given
		Class<?> entity = Favorites.class;
		Class<? extends Annotation>[] annotations = new Class[] {Id.class, EmbeddedId.class};

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
		Class<? extends Annotation>[] annotations = new Class[] {Id.class, EmbeddedId.class};

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
		Class<? extends Annotation>[] annotations = new Class[] {Id.class, EmbeddedId.class};

		List<Field> fields = ReflectionUtils.getDeclaredAnnotationFieldsByUsingRecursive(entity, annotations);

		// when
		UUID id = UUID.randomUUID();
		User user = new User(id);
		List<Object> values = ReflectionUtils.getFieldValues(user, fields);

		// then
		assertThat(values).contains(id);
	}

	@DisplayName("제네릭 타입 추출")
	@Nested
	class ExtractGenericTypeTest {

		@DisplayName("지정된 제네릭 타입 추출")
		@ParameterizedTest
		@MethodSource("com.gmoon.javacore.util.ReflectionUtilsTest#extractGenericTypeProvider")
		void success(Class<?> target, Class<?> findGenericClass, Class<?> expected) {
			Class<?> genericTypeClass = extractGenericType(target, findGenericClass, 0);

			assertThat(genericTypeClass).isEqualTo(expected);
		}

		@DisplayName("제네릭 클래스를 찾을 수 없을 경우 "
			+ "예외가 발생한다.")
		@Test
		void error1() {
			assertThatExceptionOfType(NotFoundGenericTypeClassException.class)
				.isThrownBy(() -> extractGenericType(NonSuperClassAndImplementClass.class, GenericInterface.class, 0));
		}

		@DisplayName("제네릭 타입 인덱스가 잘못된 경우 "
			+ "예외가 발생한다.")
		@Test
		void error2() {
			assertThatExceptionOfType(IndexOutOfBoundsException.class)
				.isThrownBy(() -> extractGenericType(SuperClass.class, AbstractGeneric.class, 1));
		}
	}

	class NonSuperClassAndImplementClass {
	}

	static Stream<Arguments> extractGenericTypeProvider() {
		return Stream.of(
			Arguments.of(SuperClass.class, AbstractGeneric.class, UUID.class),
			Arguments.of(ImplementClass.class, GenericInterface.class, Integer.class),
			Arguments.of(EnumClass.class, GenericInterface.class, String.class)
		);
	}

	class SuperClass extends AbstractGeneric<UUID> {
	}

	class ImplementClass implements GenericInterface<Integer> {
	}

	enum EnumClass implements GenericInterface<String> {
	}

	abstract class AbstractGeneric<T> {
	}

	interface GenericInterface<T> {
	}
}

