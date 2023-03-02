package com.gmoon.core.config;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

// EnvironmentPostProcessorApplicationListener
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private final DeferredLog log;

	private CustomEnvironmentPostProcessor() {
		log = new DeferredLog();
	}

	@Override
	public void postProcessEnvironment(
		ConfigurableEnvironment environment,
		SpringApplication application
	) {
		setProperties(environment, "runtime");
		setDefaultCoreProperties(environment);

		application.addInitializers(ctx -> log.replayTo(CustomEnvironmentPostProcessor.class));
	}

	private void setDefaultCoreProperties(ConfigurableEnvironment environment) {
		String profile = "core";
		boolean activeCoreProfile = environment.acceptsProfiles(Profiles.of(profile));
		if (!activeCoreProfile) {
			environment.addActiveProfile(profile);
			setProperties(environment, "application-" + profile);
		}
	}

	private void setProperties(ConfigurableEnvironment environment, String propertiesFileName) {
		Resource path = new ClassPathResource(propertiesFileName + ".properties");
		if (path.exists()) {
			log.debug(String.format("setProperties: %s.properties", propertiesFileName));
			try {
				PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();
				List<PropertySource<?>> load = loader.load(propertiesFileName, path);
				MutablePropertySources propertySources = environment.getPropertySources();
				propertySources.addLast(load.get(0));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
