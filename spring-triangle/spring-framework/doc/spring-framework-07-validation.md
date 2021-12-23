## 14. Validation 추상화

[org.springframework.validation.Validator](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/validation/Validator.html)

애플리케이션에서 사용하는 객체 검증용 인터페이스. 

### 특징
- 어떤한 계층과도 관계가 없다. => 모든 계층(웹, 서비스, 데이터)에서 사용해도 좋다.
- 구현체 중 하나로, JSR-303(Bean Validation 1.0)과 JSR-349(Bean Validation 1.1)을 지원한다. ([LocalValidatorFactoryBean](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/beanvalidation/LocalValidatorFactoryBean.html))
- DataBinder에 들어가 바인딩 할 때 같이 사용되기도 한다. 

### 인터페이스
- boolean supports(Class clazz): 어떤 타입의 객체를 검증할 때 사용할 것인지 결정함
- void validate(Object obj, Errors e): 실제 검증 로직을 이 안에서 구현
  - 구현할 때 ValidationUtils 사용하며 편리 함.

## 스프링 부트 2.0.5 이상 버전을 사용할 때
- LocalValidatorFactoryBean 빈으로 자동 등록
- JSR-380(Bean Validation 2.0.1) 구현체로 hibernate-validator 사용.
- https://beanvalidation.org/
