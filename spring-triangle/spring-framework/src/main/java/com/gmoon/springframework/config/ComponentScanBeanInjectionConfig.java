package com.gmoon.springframework.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.gmoon.springframework.SpringFrameworkApplication;

@Configuration
@ComponentScan(basePackageClasses = SpringFrameworkApplication.class)
public class ComponentScanBeanInjectionConfig {
}
