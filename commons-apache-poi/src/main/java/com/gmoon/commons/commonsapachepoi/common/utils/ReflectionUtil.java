package com.gmoon.commons.commonsapachepoi.common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

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

	@SafeVarargs
	public static Map<Integer, Field> getDeclaredAnnotationFieldMap(Class<?> target,
		 Class<? extends Annotation>... annotation) {
		Map<Integer, Field> result = new LinkedHashMap<>();
		int index = 0;
		Field[] fields = target.getDeclaredFields();
		for (Field field : fields) {
			if (existsDeclaredAnnotation(field, annotation)) {
				field.setAccessible(true);
				result.put(index++, field);
			}
		}
		return result;
	}

	public static <T extends Annotation> T findAnnotation(Field field, Class<T> annotationClass) {
		if (existsDeclaredAnnotation(field, annotationClass)) {
			return field.getAnnotation(annotationClass);
		}

		return null;
	}

	@SafeVarargs
	public static boolean existsDeclaredAnnotation(Field field, Class<? extends Annotation>... annotations) {
		Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
		if (ArrayUtils.isEmpty(declaredAnnotations)) {
			return false;
		}

		return Stream.of(declaredAnnotations)
			 .map(Annotation::annotationType)
			 .anyMatch(annType -> {
				 for (Class<?> ann : annotations) {
					 if (annType == ann) {
						 return true;
					 }
				 }
				 return false;
			 });
	}
}
