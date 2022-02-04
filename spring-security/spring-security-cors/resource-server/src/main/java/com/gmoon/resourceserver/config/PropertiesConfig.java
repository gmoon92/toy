package com.gmoon.resourceserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.gmoon.resourceserver.properties.CorsProperties;

@Configuration
@EnableConfigurationProperties({CorsProperties.class})
@PropertySource(value = "classpath:cors.yml", factory = YamlPropertySourceFactory.class)
public class PropertiesConfig {
}
