package com.gmoon.core.event;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConfigurationPropertyLogger {

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		final Environment env = context.getEnvironment();
		log.trace("====== Environment property ======");
		log.trace("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
		final MutablePropertySources sources = ((AbstractEnvironment)env).getPropertySources();
		StreamSupport.stream(sources.spliterator(), false)
			.filter(this::isTargetPropertySource)
			.filter(ps -> ps instanceof EnumerablePropertySource)
			.map(ps -> ((EnumerablePropertySource<?>)ps).getPropertyNames())
			.flatMap(Arrays::stream)
			.distinct()
			.forEach(prop -> log.trace("{}: {}", prop, env.getProperty(prop)));
		log.trace("==================================");
	}

	private boolean isTargetPropertySource(PropertySource<?> propertySource) {
		return Stream.of("runtime.properties", "application-core.properties")
			.anyMatch(name -> name.contains(propertySource.getName()));
	}
}
