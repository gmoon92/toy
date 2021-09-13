package com.gmoon.springframework.config;

import com.gmoon.springframework.SpringFrameworkApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = SpringFrameworkApplication.class)
public class ComponentScanBeanInjectionConfig {
}
