### 10. IoC 컨테이너 7부: MessageSource

국제화 (i18n) 기능을 제공하는 인터페이스.

ApplicationContext extends MessageSource

- getMessage(String code, Object[] args, String, default, Locale, loc)
- ...

스프링 부트를 사용한다면 별다른 설정 필요없이 messages.properties 사용할 수 있음

- messages.properties
- messages_ko_kr.properties
- ...

릴로딩 기능이 있는 메시지 소스 사용하기

```java
@Bean 
public MessageSource messageSource(){
  var messageSource=new ReloadableResourceBundleMessageSource();
  messageSource.setBasename("classpath:/messages");
  messageSource.setDefaultEncoding("UTF-8");
  messageSource.setCacheSeconds(3);
  return messageSource;
}
```
