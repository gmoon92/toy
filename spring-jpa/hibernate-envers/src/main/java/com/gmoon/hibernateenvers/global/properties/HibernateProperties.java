package com.gmoon.hibernateenvers.global.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "spring.jpa.properties")
@Getter
@Setter
public class HibernateProperties {

	Map<String, String> hibernate;

}
