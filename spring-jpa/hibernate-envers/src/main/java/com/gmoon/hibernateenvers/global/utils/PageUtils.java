package com.gmoon.hibernateenvers.global.utils;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;

public final class PageUtils {

	public static Sort sort(Sort.Direction direction, Path<?> path) {
		PathMetadata metadata = path.getMetadata();
		Path<?> rootPath = metadata.getRootPath();
		assert rootPath != null;

		String rootPathName = rootPath.toString();
		String pathName = metadata.getName();
		String fullyQualifiedName = String.format("%s.%s", rootPathName, pathName);
		return Sort.by(direction, fullyQualifiedName);
	}
}
