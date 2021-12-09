package com.gmoon.springschedulingquartz.config;

import com.gmoon.springschedulingquartz.properties.QuartzProperties;
import com.gmoon.springschedulingquartz.properties.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(QuartzProperties.class)
@PropertySource(value = "classpath:quartz.yml", factory = YamlPropertySourceFactory.class)
public class PropertiesConfig {
}
