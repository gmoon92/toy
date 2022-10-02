package com.gmoon.springwebconverter.config;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gmoon.springwebconverter.config.converter.EnumConverterFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		ApplicationConversionService.configure(registry);
		// registry.addConverter(new SearchTypeConverter());
		registry.addConverterFactory(new EnumConverterFactory());
	}
}
