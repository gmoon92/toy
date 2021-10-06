package com.gmoon.hibernateenvers.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.jpa.properties")
public class HibernateProperties {

  Map<String, String> hibernate;

}
