package com.gmoon.javacore.util;

import static java.util.stream.Collectors.toList;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

	private static class ReflectionsHolder {

		private static final Reflections INSTANCE;

		static {
			ConfigurationBuilder config = new ConfigurationBuilder()
				.forPackage("com.gmoon")
//				.filterInputsBy(
//					new FilterBuilder()
//						.includePackage("com.my.project.include")
////						.excludePackage("com.my.project.exclude")
//				)
				.setScanners(Scanners.values());
			INSTANCE = new Reflections(config);
		}
	}

	public static List<Object> getFieldValues(Object object, List<Field> fields) {
		return fields.stream()
			.map(field -> getFieldValue(object, field))
			.collect(toList());
	}

	private static Object getFieldValue(Object object, Field field) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Set<Class<?>> getDeclaredAnnotationClasses(final Class<? extends Annotation> annotation) {
		return ReflectionsHolder.INSTANCE.getTypesAnnotatedWith(annotation);
	}

	public static List<Field> getDeclaredAnnotationFields(final Class<?> clazz,
														  final Class<? extends Annotation>... annotation) {
		return Stream.of(clazz.getDeclaredFields())
			.filter(field -> existsDeclaredAnnotation(field, annotation))
			.collect(toList());
	}

	public static List<Field> getDeclaredAnnotationFieldsByUsingRecursive(final Class<?> clazz,
																		  final Class<? extends Annotation>... annotation) {
		List<Field> fields = getDeclaredAnnotationFields(clazz, annotation);
		if (CollectionUtils.isEmpty(fields)) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getDeclaredAnnotationFields(superclass, annotation);
			}
		}

		return fields;
	}

	public static boolean existsDeclaredAnnotation(final Field field,
												   final Class<? extends Annotation>... annotations) {
		return Stream.of(annotations)
			.anyMatch(field::isAnnotationPresent);
	}
}
