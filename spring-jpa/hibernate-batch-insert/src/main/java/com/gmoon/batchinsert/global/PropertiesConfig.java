package com.gmoon.batchinsert.global;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.gmoon.batchinsert.global.prop.JooqProperties;
import com.gmoon.batchinsert.global.prop.StorageProperties;

@Configuration
@EnableConfigurationProperties({
	 StorageProperties.class,
	 JooqProperties.class
})
public class PropertiesConfig {

}
