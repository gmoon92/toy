package com.gmoon.web.config;

import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.javacore.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OverrideComponentExcludeFilter implements TypeFilter {

	private final Set<String> componentExcludeClassNames;

	public OverrideComponentExcludeFilter() {
		componentExcludeClassNames = getExcludeClassNames();
	}

	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
		ClassMetadata classMetadata = metadataReader.getClassMetadata();
		return componentExcludeClassNames.contains(classMetadata.getClassName());
	}

	private Set<String> getExcludeClassNames() {
		try {
			return scanPackage().stream()
				 .map(Class::getSuperclass)
				 .map(Class::getName)
				 .peek(name -> log.info("super class name of custom bean: {}", name))
				 .collect(Collectors.toSet());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Set<Class<?>> scanPackage() {
		Reflections reflections = new Reflections(
			 new ConfigurationBuilder()
				  .forPackage("com.gmoon.custom")
				  .setExpandSuperTypes(false)
		);
		return ReflectionUtils.getDeclaredAnnotationClasses(
			 reflections,
			 Controller.class,
			 RestController.class,
			 Service.class,
			 Repository.class,
			 Component.class
		);
	}
}
