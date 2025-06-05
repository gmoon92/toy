package com.gmoon.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

// EnvironmentPostProcessorApplicationListener
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private final PropertySourceFactory propertySourceFactory;
	private final DeferredLog log;

	private CustomEnvironmentPostProcessor() {
		log = new DeferredLog();
		propertySourceFactory = new DefaultPropertySourceFactory();
	}

	@Override
	public void postProcessEnvironment(
		 ConfigurableEnvironment environment,
		 SpringApplication application
	) {
		setDefaultCoreProperties(environment);
		overridePropertySource(environment, "runtime");

		application.addInitializers(ctx -> log.replayTo(CustomEnvironmentPostProcessor.class));
	}

	private void overridePropertySource(ConfigurableEnvironment environment, String relativePropertySourceName) {
		MutablePropertySources propertySources = environment.getPropertySources();

		PropertySource<?> runtimeProperties = createPropertySource(relativePropertySourceName);
		propertySources.addFirst(runtimeProperties);
	}

	private PropertySource<?> createPropertySource(String relativePropertySourceName) {
		try {
			return propertySourceFactory.createPropertySource(
				 relativePropertySourceName,
				 new EncodedResource(
					  new ClassPathResource(relativePropertySourceName + ".properties"),
					  StandardCharsets.UTF_8
				 )
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setDefaultCoreProperties(ConfigurableEnvironment environment) {
		String profile = "core";
		boolean activeCoreProfile = environment.acceptsProfiles(Profiles.of(profile));
		if (!activeCoreProfile) {
			addApplicationPropertySource(environment, profile);
		}
	}

	private void addApplicationPropertySource(ConfigurableEnvironment environment, String profile) {
		environment.addActiveProfile(profile);

		MutablePropertySources propertySources = environment.getPropertySources();
		String relativePropertySourceName = "application-" + profile;
		PropertySource<?> propertySource = createPropertySource(relativePropertySourceName);
		propertySources.addLast(propertySource);
		log.debug(String.format("add application profile: %s.properties", relativePropertySourceName));
	}
}
