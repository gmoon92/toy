package com.gmoon.batchinsert.global.config.properties;

import com.gmoon.batchinsert.global.prop.JooqProperties;
import com.gmoon.batchinsert.global.prop.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
	 StorageProperties.class,
	 JooqProperties.class
})
public class PropertiesConfig {

}
