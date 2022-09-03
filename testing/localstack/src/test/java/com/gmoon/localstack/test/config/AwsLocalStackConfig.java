package com.gmoon.localstack.test.config;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.gmoon.localstack.test.annotation.EnableAwsLocalStack;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class AwsLocalStackConfig implements ImportAware {

	private LocalStackContainer.Service[] services;
	private String imageFullName;

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
			importMetadata.getAnnotationAttributes(
				EnableAwsLocalStack.class.getName(),
				false
			)
		);

		Assert.notNull(
			annotationAttributes,
			"@EnableAwsLocalStack is not present on importing class " + importMetadata.getClassName()
		);

		services = (LocalStackContainer.Service[]) annotationAttributes.get("value");
		Assert.notEmpty(services, "LocalStack 을 사용할 서비스를 지정해주세요.");

		imageFullName = (String) annotationAttributes.get("imageFullName");

	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public LocalStackContainer localStackContainer() {
		return new LocalStackContainer(
			DockerImageName.parse(imageFullName)
		).withServices(services);
	}

	@Bean
	public Map<LocalStackContainer.Service, AwsClientBuilder.EndpointConfiguration> localStackEndpointConfigurations(LocalStackContainer localstack) {
		return Stream.of(LocalStackContainer.Service.values())
			.collect(collectingAndThen(
				toMap(
					Function.identity(),
					service -> {
						URI endpoint = localstack.getEndpointOverride(service);
						return new AwsClientBuilder.EndpointConfiguration(
							endpoint.toString(),
							localstack.getRegion()
						);
					}),
				Collections::unmodifiableMap
			));
	}
}
