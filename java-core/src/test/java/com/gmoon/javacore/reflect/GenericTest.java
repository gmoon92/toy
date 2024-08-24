package com.gmoon.javacore.reflect;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class GenericTest {

	static final int GENERIC_TARGET = 0;
	static final int GENERIC_ID = 1;

	@Test
	@DisplayName("superclass 의 generic type 가져오기")
	void reflect() throws Exception {
		// given
		Class<?> clazz = MemberRepository.class;
		ParameterizedType superClassGenericType = (ParameterizedType)clazz.getGenericSuperclass();

		// when then
		verifyTargetGenericType(superClassGenericType);
		verifyIdGenericType(superClassGenericType);

		MemberRepository repository = new MemberRepository();
		Class<Member> persistentClass = repository.getPersistentClass();
		assertThat(persistentClass.getTypeName())
			 .isEqualTo(Member.class.getName());
	}

	private void verifyTargetGenericType(ParameterizedType superClassGenericType) throws Exception {
		String id = UUID.randomUUID().toString();
		Class<?> domainClass = (Class<?>)superClassGenericType.getActualTypeArguments()[GENERIC_TARGET];
		Constructor<?> constructor = domainClass.getDeclaredConstructor(String.class);
		Member member = (Member)constructor.newInstance(id);

		assertThat(member)
			 .isEqualTo(new Member(id));
	}

	private void verifyIdGenericType(ParameterizedType superClassGenericType) {
		Class<?> idClass = (Class<?>)superClassGenericType.getActualTypeArguments()[GENERIC_ID];
		assertThat(idClass.getTypeName())
			 .isEqualTo(String.class.getName());
	}

	public static class MemberRepository extends JpaRepository<Member, String> {
	}

	@Getter
	public static abstract class JpaRepository<T, ID> {
		private final Class<T> persistentClass;

		@SuppressWarnings("unchecked")
		public JpaRepository() {
			ParameterizedType superClassGenericType = (ParameterizedType)this.getClass().getGenericSuperclass();
			this.persistentClass = (Class<T>)superClassGenericType.getActualTypeArguments()[GENERIC_TARGET];
		}
	}

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	public static class Member {
		@EqualsAndHashCode.Include
		private String id;

		public Member(String id) {
			this.id = id;
		}
	}
}
