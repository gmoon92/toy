package com.gmoon.localstack.amazon.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(AmazonProperty.class)
@PropertySource(value = "amazon.yml", factory = YamlPropertySourceFactory.class)
public class AmazonPropertyConfig {

}
