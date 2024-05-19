package com.gmoon.localstack.aws.config;

import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
		Resource resource = encodedResource.getResource();

		return new PropertiesPropertySource(
			 resource.getFilename(),
			 getProperties(resource)
		);
	}

	private Properties getProperties(Resource resource) {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(resource);
		return factory.getObject();
	}
}
