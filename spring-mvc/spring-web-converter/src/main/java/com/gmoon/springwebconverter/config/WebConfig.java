package com.gmoon.springwebconverter.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gmoon.springwebconverter.config.converter.EnumConverterFactory;
import com.gmoon.springwebconverter.config.converter.StringToLocalDateConverter;
import com.gmoon.springwebconverter.config.interceptor.LocalDateAnnotationIntrospector;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final StringToLocalDateConverter stringToLocalDateConverter;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		ApplicationConversionService.configure(registry);
		// registry.addConverter(new SearchTypeConverter());
		registry.addConverter(stringToLocalDateConverter);
		registry.addConverterFactory(new EnumConverterFactory());
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer customizer() {
		return builder ->
			 builder.annotationIntrospector(new LocalDateAnnotationIntrospector());
	}
}
