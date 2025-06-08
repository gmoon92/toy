# Spring Data Binding

## PropertyEditor vs Converter vs Formatter

| 구분            | PropertyEditor                                       | Converter                              | Formatter             |
|---------------|------------------------------------------------------|----------------------------------------|-----------------------|
| **등장 시기**     | 자바 빈즈(옛날), 스프링 3 이전                                  | 스프링 3~                                 | 스프링 3~                |
| **등록 위치**     | DataBinder                                           | ConversionService                      | ConversionService     |
| **변환 방향**     | 문자(String) <-> 객체                                    | 범용(object <-> object)                  | 문자(String) <-> 객체(특화) |
| **Locale 지원** | X                                                    | X                                      | O                     |
| **스레드 안정성**   | stateful(비안전)                                        | stateless(안전)                          | stateless(안전)         |
| **설정 방식**     | 수동 등록, 코드 기반                                         | 타입별 등록, 선언적                            | 타입별, Locale-aware     |
| **대표 용도**     | 구식 폼 value 처리<br/>매우 제한적(사용 자제)                      | 기본 파라미터 변환 도입부<br/>전체 타입 변환 (id→엔티티 등) | 날짜/숫자/포맷팅(문자 <-> 객체)  |
| **활용 예시**     | 거의 쓰지 않음, 레거시 호환<br/>폼 입력 문자열 변환 (String <-> Date 등) | @RequestParam, @ModelAttribute 등       | 포매팅/국제화 필요할 때         |

- PropertyEditor: **실제로 쓸 일은 거의 없음.**
    - 레거시 유지보수 등에 한정, 새로 프로젝트 도입시 고려 대상에서 제외.
- Converter: type conversion
    - **문자열 <-> 객체 <-> 객체 <-> 객체 변환 전반**
    - StringToLocalDateTimeConverter.class
    - LocalDateTimeToBytesConverter.class
- Formatter: field formatting
    - **날짜·숫자 등** _**문자 포매팅/파싱/국제화가 필요**_
    - NumberStyleFormatter
    - YearMonthFormatter

## PropertyEditor Thread safe

```java
import org.springframework.web.bind.annotation.InitBinder;

class Controller {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		// 요청마다 CustomDateEditor 인스턴스 새로 생성하여 등록 
		// stateful이므로 빈으로 등록 금지!
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);

		// CustomDateEditor는 내부 상태(state)를 가짐! (setAsText 등)
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}
}
```

- PropertyEditor은 싱글톤 빈으로 등록하지 말고 반드시 인스턴스 생성하여 사용 권장.
- stateful 하게 설계되었기 때문에 싱글톤 객체로 활용시 멀티 쓰레드 환경에서 문제가 됨.
- 반드시 위처럼 요청마다 새로운 인스턴스를 생성해서 등록해야 `thread-safe` 보장됨.

> Spring MVC 환경에서는 `PropertyEditor`를 싱글톤 빈으로 등록하지 않습니다.<br/>
> 반드시 `@InitBinder`에서 매 요청마다 새 인스턴스로 등록해야 합니다. <br/>
> `CustomDateEditor`와 대부분의 `PropertyEditor`는 내부에 입력값을 상태로 저장하기 때문에, 여러 스레드(요청)가 동시에 사용할 경우 값이 섞일 수 있습니다.

## Samples

```java
// Converter
class IdToUserConverter
  implements org.springframework.core.convert.converter.Converter<String, User> {

	@Override
	public User convert(String source) {
		// 문자 <-> 객체, 객체 <-> 객체 등 모든 변환, 
		// 국제화/포맷 불필요시
	}
}

// Formatter
class MoneyFormatter
  implements org.springframework.format.Formatter<String> {

	// 문자 <-> 객체 + 포매팅/국제화 필수시
	@Override
	public String parse(String text, Locale locale) throws ParseException {
		return "";
	}

	@Override
	public String print(String object, Locale locale) {
		return "";
	}
}
```

- Converter/Formatter 등록은 ConversionService 단일 레지스트리에서 통합 관리
- **메시지 컨버터(@RequestBody 등, JSON 변환)는 Jackson 등 별도 처리, ConversionService 미적용**

## 결론

- **Converter:** "모든 타입/객체 변환" 필요하면 사용 (90% 상황)
- **Formatter:** "포맷·로케일 등 문자 변환" 필요하면 사용
- **PropertyEditor:** "웬만하면 쓰지 X", 레거시 핫픽스/유지보수용

> 복잡하게 기억 말고, > **대부분은 Converter(범용) ≫ Formatter(문자포맷), PropertyEditor(굳이X)**

## Reference

- https://docs.spring.io/spring-framework/reference/core/validation/beans-beans.html
