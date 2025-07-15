package com.gmoon.commons.commonsapachepoi.common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

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

	public static <T, A extends Annotation> Map<Integer, T> getDeclaredAnnotationFieldMap(
		 Class<?> target,
		 Class<A> annotationClass,
		 Function<Field, T> mapper
	) {
		Map<Integer, T> result = new LinkedHashMap<>();
		int index = 0;
		Field[] fields = target.getDeclaredFields();
		for (Field field : fields) {
			A annotation = field.getAnnotation(annotationClass);
			if (annotation != null) {
				field.setAccessible(true);
				result.put(index++, mapper.apply(field));
			}
		}
		return result;
	}

	public static <T extends Annotation> T findAnnotation(Class<?> target, Class<T> annotationClass) {
		return AnnotationUtils.findAnnotation(target, annotationClass);
	}
}
