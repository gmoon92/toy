package com.gmoon.localstack.aws.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(AwsProperty.class)
@PropertySource(value = "aws.yml", factory = YamlPropertySourceFactory.class)
public class AwsPropertyConfig {

}
