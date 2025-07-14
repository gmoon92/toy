package com.gmoon.commons.commonsapachepoi.common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
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

	public static Map<Integer, Field> getDeclaredAnnotationFieldMap(
		 Class<?> target,
		 Class<? extends Annotation> annotation
	) {
		return getDeclaredAnnotationFieldMap(target, annotation, field -> field);
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
