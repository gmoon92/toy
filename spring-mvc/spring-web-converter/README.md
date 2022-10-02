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

## WebMvcConfigurer Config

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
public interface StringToEnumBinder {

	String getValue();
}

@RequiredArgsConstructor
@Getter
public enum PaymentType implements StringToEnumBinder {

	KAKAO_BANK("0001"),
	NAVER_PAY("0002"),
	TOSS("0003");

	private final String value;
}

```

### Custom Converter Factory

```java
@Slf4j
public class EnumConverterFactory implements ConverterFactory<String, Enum<?>> {

	private static final Map<Class, Converter> CACHE = new ConcurrentHashMap<>();

	@Override
	public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetClass) {
		if (CACHE.get(targetClass) == null) {
			log.info("targetClass: {}", targetClass);
			CACHE.put(targetClass, StringToEnumFactory.create(targetClass));
		}

		return CACHE.get(targetClass);
	}

	private static class StringToEnumFactory {

		static Converter create(Class<? extends Enum<?>> targetClass) {
			boolean isCustomBinder = stream(targetClass.getInterfaces())
			  .anyMatch(i -> i == StringToEnumBinder.class);
			if (isCustomBinder) {
				return new CustomStringToEnum(targetClass);
			}

			return new DefaultStringToEnum(targetClass);
		}

		@RequiredArgsConstructor
		static class DefaultStringToEnum implements Converter<String, Enum> {

			private final Class<? extends Enum> targetClass;

			@Override
			public Enum convert(String source) {
				return Enum.valueOf(targetClass, source);
			}
		}

		static class CustomStringToEnum implements Converter<String, Enum<? extends StringToEnumBinder>> {

			private final Map<String, Enum> binder;

			private CustomStringToEnum(Class<? extends Enum> targetClass) {
				binder = stream(targetClass.getEnumConstants())
				  .collect(collectingAndThen(
					toMap(o -> ((StringToEnumBinder)o).getValue(), Function.identity()),
					Collections::unmodifiableMap
				  ));
			}

			@Override
			public Enum<? extends StringToEnumBinder> convert(String source) {
				Enum<? extends StringToEnumBinder> result = binder.get(source);
				if (result == null) {
					throw new ConversionFailedException();
				}

				return result;
			}
		}
	}

	public int size() {
		return CACHE.size();
	}
}
```

### WebMvcConfigurer Config

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
		// registry.addConverter(new SearchTypeConverter());
		registry.addConverterFactory(new EnumConverterFactory());
	}
}
````

### 동작 방식

```java
package org.springframework.core.convert.converter;

@FunctionalInterface
public interface Converter<S, T> {
}

public interface ConverterFactory<S, R> {
}
```

- 커스텀 구현한 Converter 또는 Converter Factory 를 등록한다.
    - registry.addConverter(Converter)
    - registry.addConverterFactory(ConverterFactory)
- 제네릭 `타입 토큰` 클래스로 `ConvertiblePair`를 생성한다.
    - GenericConversionService.addConverterFactory(ConverterFactory<?, ?>)
    - 첫 번째 타입: 요청 파라미터 타입
    - 두 번째 타입: 변환할 타입
- 요청이 오면 내부적으로 GenericConversionService.convert 호출
  - 내부적으로 Converter 는 캐시로 관리
  - 캐시 키는 등록할 때 생성된 `ConvertiblePair` 로 Converter 반환

```java
package org.springframework.core.convert.support;

public class GenericConversionService implements ConfigurableConversionService {

	@Override
	@Nullable
	public Object convert(
	  @Nullable Object source,
	  @Nullable TypeDescriptor sourceType,
	  TypeDescriptor targetType
	) {
		// ...

		GenericConverter converter = getConverter(sourceType, targetType);
		if (converter != null) { // LenientStringToEnumConverterFactory.class
			Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
			return handleResult(sourceType, targetType, result);
		}
		return handleConverterNotFound(source, sourceType, targetType);
	}
}
```

- GenericConversionService
    - convert(Object, TypeDescriptor, TypeDescriptor)
        - getConverter(TypeDescriptor, TypeDescriptor)
            - Converters.find(TypeDescriptor, TypeDescriptor)
            - Converters.getRegisteredConverter(TypeDescriptor, TypeDescriptor, ConvertiblePair)
        - handleConverterNotFound(Object, TypeDescriptor, TypeDescriptor)
- ApplicationConversionService
    - addApplicationConverters
        - GenericConversionService.addConverter(Converter<?, ?>)
            - GenericConversionService.addConverter(GenericConverter)
                - Converters.add(GenericConverter)
        - GenericConversionService.addConverterFactory(ConverterFactory<?, ?>)

## Reference

- [baeldung - Spring Enum Request Param](https://www.baeldung.com/spring-enum-request-param)
- [baeldung - Spring Type Conversions](https://www.baeldung.com/spring-type-conversions)
