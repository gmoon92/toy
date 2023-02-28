package com.gmoon.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
	basePackages = {"com.gmoon"},
	excludeFilters = {
		@ComponentScan.Filter(Configuration.class) ,
		@ComponentScan.Filter(type = FilterType.CUSTOM, classes = OverrideComponentExcludeFilter.class)

	}
)
public class MainConfig {
}
