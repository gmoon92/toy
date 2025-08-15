package com.gmoon.springpoi.common.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import com.gmoon.springpoi.SpringPoiApplication;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = SpringPoiApplication.class)
public class PropertiesConfig {
}
