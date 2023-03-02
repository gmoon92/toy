package com.gmoon.core.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemProperties {

	private final Environment env;
	private final PropertiesConfiguration runtimeProperties;

	public String getModuleName() {
		return runtimeProperties.getString("module-name", env.getProperty("module-name"));
	}
}
