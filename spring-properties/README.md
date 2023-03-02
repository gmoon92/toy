# Spring Properties

- jasypt encrypt/decrypt
- runtime.properties

## Custom Environment PropertySource

- ApplicationContextInitializer
- EnvironmentPostProcessor

```text
## META-INF/spring.factories

# ApplicationContextInitializer
org.springframework.context.ApplicationContextInitializer=\
com.gmoon.core.config.DecryptEnvironmentInitializer

# Environment Post Processors
org.springframework.boot.env.EnvironmentPostProcessor=\
com.gmoon.core.config.CustomEnvironmentPostProcessor
```

## EnvironmentPostProcessor

## [External config](https://docs.spring.io/spring-boot/docs/2.7.0/reference/htmlsingle/#features.external-config)

1. Default properties (specified by setting SpringApplication.setDefaultProperties).
2. `@PropertySource` annotations on your `@Configuration` classes. Please note that such property sources are not added to the Environment until the application context is being refreshed. This is too late to configure certain properties such as `logging.*` and `spring.main.*` which are read before refresh begins.
3. Config data (such as `application.properties` files).
4. A `RandomValuePropertySource` that has properties only in `random.*.`
5. OS environment variables.
6. Java System properties (`System.getProperties()`).
7. JNDI attributes from `java:comp/env`.
8. `ServletContext` init parameters.
9. `ServletConfig` init parameters.
10. Properties from `SPRING_APPLICATION_JSON` (inline JSON embedded in an environment variable or system property).
11. Command line arguments.
12. `properties` attribute on your tests. Available on `@SpringBootTest` and the `test annotations for testing a particular slice of your application`. 
13. `@TestPropertySource` annotations on your tests.
14. `Devtools global settings properties` in the $HOME/.config/spring-boot directory when devtools is active.

### Config data files are considered in the following order:

1. `Application properties` packaged inside your jar (application.properties and YAML variants).
2. `Profile-specific application` properties packaged inside your jar (application-{profile}.properties and YAML variants).
3. `Application properties` outside of your packaged jar (application.properties and YAML variants).
4. `Profile-specific application properties` outside of your packaged jar (application-{profile}.properties and YAML variants).

## Reference

- [Spring boot - Using @Value](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-value-annotations)
- [Spring boot - External Config](https://docs.spring.io/spring-boot/docs/2.7.0/reference/htmlsingle/#features.external-config)
- [Spring boot - Using the @SpringBootApplication Annotation](https://docs.spring.io/spring-boot/docs/2.0.x/reference/html/using-boot-using-springbootapplication-annotation.html#using-boot-using-springbootapplication-annotation)
- [baeldung - Spring boot jasypt](https://www.baeldung.com/spring-boot-jasypt)
- [baeldung - Properties with Spring and Spring Boot](https://www.baeldung.com/properties-with-spring)
- [baeldung - EnvironmentPostProcessor in Spring Boot](https://www.baeldung.com/spring-boot-environmentpostprocessor)
- [stackoverflow - How can I change a property in spring environment?](https://stackoverflow.com/questions/34886567/how-can-i-change-a-property-in-spring-environment)
- [kingbbode - Spring Boot 와 Properties(or Yaml) Environment](https://blog.kingbbode.com/39)
