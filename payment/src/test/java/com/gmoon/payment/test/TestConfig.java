package com.gmoon.payment.test;

import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import com.gmoon.payment.PaymentApplication;

@TestConfiguration
@ConfigurationPropertiesScan(basePackageClasses = PaymentApplication.class)
@PropertySources({
	 @PropertySource(value = "file:.env", ignoreResourceNotFound = true),
	 @PropertySource(
		  value = "classpath:application-test.yml",
		  factory = TestConfig.YamlPropertySourceFactory.class
	 )
})
public class TestConfig {

	static class YamlPropertySourceFactory implements PropertySourceFactory {

		@Override
		public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
			Resource resource = encodedResource.getResource();

			return new PropertiesPropertySource(
				 resource.getFilename(),
				 getProperties(resource)
			);
		}

		private Properties getProperties(Resource resource) {
			YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
			factory.setResources(resource);
			factory.afterPropertiesSet();
			return factory.getObject();
		}
	}
}
