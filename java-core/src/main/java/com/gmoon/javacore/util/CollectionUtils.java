package com.gmoon.javacore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtils {
	public static boolean isEmpty(Collection<?> collection) {
		return org.apache.commons.collections4.CollectionUtils.isEmpty(collection);
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static boolean isEmptyAny(Collection<?>... collections) {
		for (Collection<?> collection : collections) {
			if (isEmpty(collection)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsAny(Collection<?> collection, Collection<?> collection2) {
		return org.apache.commons.collections4.CollectionUtils.containsAny(collection, collection2);
	}

	public static int size(Collection<?> collection) {
		return org.apache.commons.collections4.CollectionUtils.size(collection);
	}

	public static void reverseArray(String[] result) {
		org.apache.commons.collections4.CollectionUtils.reverseArray(result);
	}

	public static <T> List<T> removeDuplicates(List<T> list) {
		if (isEmpty(list)) {
			return new ArrayList<>();
		}

		return list.stream()
			 .distinct()
			 .collect(Collectors.toList());
	}

	public static int minSize(Collection<?>... collections) {
		if (ArrayUtils.isEmpty(collections)) {
			return 0;
		}

		return Stream.of(collections)
			 .mapToInt(CollectionUtils::size)
			 .min()
			 .orElse(0);
	}
}
