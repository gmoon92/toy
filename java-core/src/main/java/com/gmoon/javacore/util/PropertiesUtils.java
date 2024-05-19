package com.gmoon.javacore.util;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesUtils {

	public static Properties load(String path) {
		try {
			Properties properties = new Properties();
			properties.load(
				 Objects.requireNonNull(PropertiesUtils.class.getClassLoader().getResource(path)).openStream());
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

