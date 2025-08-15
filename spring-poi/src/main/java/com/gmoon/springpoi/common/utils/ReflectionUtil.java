package com.gmoon.springpoi.common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.core.annotation.AnnotationUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> clazz) {
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);

			return (T)constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Map<Integer, T> getFieldMap(
		 Class<?> targetClass,
		 Predicate<Field> filter,
		 Function<Field, T> mapper
	) {
		Map<Integer, T> result = new LinkedHashMap<>();
		int index = 0;
		Field[] fields = targetClass.getDeclaredFields();
		for (Field field : fields) {
			if (filter.test(field)) {
				field.setAccessible(true);
				result.put(index++, mapper.apply(field));
			}
		}
		return Collections.unmodifiableMap(result);
	}

	public static <T extends Annotation> T findAnnotation(Class<?> target, Class<T> annotationClass) {
		return AnnotationUtils.findAnnotation(target, annotationClass);
	}

	public static Object getFieldValue(Object target, Field field) {
		try {
			return field.get(target);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(String.format(
				 "Failed to get field '%s' by reflection from class '%s'.",
				 field.getName(), target.getClass().getName()
			), e);
		}
	}
}
