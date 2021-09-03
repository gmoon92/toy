package com.gmoon.springframework.config;

import com.gmoon.springframework.book.BookApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = BookApplication.class)
public class ComponentScanBeanInjectionConfig {
}
