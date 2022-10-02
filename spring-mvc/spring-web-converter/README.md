# Spring Enum Request Param

## Enum

```java

@RequiredArgsConstructor
@Getter
public enum SearchType {

	DATE("date"),
	USER_NAME("username");

	private final String value;

	private static final Map<String, SearchType> ALL = Arrays.stream(values())
	  .collect(collectingAndThen(
		toMap(SearchType::getValue, Function.identity()),
		Collections::unmodifiableMap
	  ));

	public static SearchType from(String value) {
		return ALL.get(value);
	}
}
```

## Converter

````java
import org.springframework.core.convert.converter.Converter;

public class SearchTypeConverter implements Converter<String, SearchType> {

	@Override
	public SearchType convert(String source) {
		return SearchType.from(source);
	}
}
````

## WebMvcConfigurer

```java
import com.gmoon.springwebconverter.config.converter.SearchTypeConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new SearchTypeConverter());
	}
}
```

## Conversion Exception

```java
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

	@ExceptionHandler(ConversionFailedException.class)
	public ResponseEntity<String> handleConflict(RuntimeException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}
}
```

## ConverterFactory

### Custom binder interface

```java
public interface ValueToEnumBinder<T> {

	T getValue();
}

@RequiredArgsConstructor
@Getter
public enum PaymentType implements ValueToEnumBinder {

	KAKAO_BANK("0001"),
	NAVER_PAY("0002"),
	TOSS("0003");

	private final String value;
}

```

### Custom Converter Factory

```java
public class EnumConverterFactory implements ConverterFactory<Object, Enum<? extends ValueToEnumBinder>> {

	private static final Map<Class, Converter> CACHE = new ConcurrentHashMap<>();

	@Override
	public <T extends Enum<? extends ValueToEnumBinder>> Converter<Object, T> getConverter(Class<T> targetClass) {
		if (CACHE.get(targetClass) == null) {
			CACHE.put(targetClass, new EnumConverter(targetClass));
		}

		return CACHE.get(targetClass);
	}

	private static class EnumConverter implements Converter<Object, Enum<? extends ValueToEnumBinder>> {

		private final Map<Object, Enum<? extends ValueToEnumBinder>> ALL;

		private EnumConverter(Class<? extends Enum<? extends ValueToEnumBinder>> targetClass) {
			ALL = stream(targetClass.getEnumConstants())
				.collect(collectingAndThen(
					toMap(o -> ((ValueToEnumBinder)o).getValue(), Function.identity()),
					Collections::unmodifiableMap
				));
		}

		@Override
		public Enum<? extends ValueToEnumBinder> convert(Object source) {
			Enum<? extends ValueToEnumBinder> result = ALL.get(source);
			if (result == null) {
				throw new ConversionFailedException();
			}

			return result;
		}
	}

	public int size() {
		return CACHE.size();
	}
}
```

### Web Config

````java
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gmoon.springwebconverter.config.converter.EnumConverterFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		ApplicationConversionService.configure(registry);
		registry.removeConvertible(String.class, Enum.class);
		registry.addConverterFactory(new EnumConverterFactory());
	}
}
````

## Reference

- [baeldung - Spring Enum Request Param](https://www.baeldung.com/spring-enum-request-param)
- [baeldung - Spring Type Conversions](https://www.baeldung.com/spring-type-conversions)
