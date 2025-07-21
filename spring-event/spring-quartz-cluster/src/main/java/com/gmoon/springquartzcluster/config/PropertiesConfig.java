package com.gmoon.springquartzcluster.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.gmoon.springquartzcluster.properties.QuartzProperties;

@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
// @PropertySource(value = "classpath:quartz.yml", factory = YamlPropertySourceFactory.class)
public class PropertiesConfig {
}
