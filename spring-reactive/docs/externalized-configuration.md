# [Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)

## Durations

```java
@ConfigurationProperties("my")
public class MyProperties {

	@DefaultValue("30s")
	@DurationUnit(ChronoUnit.SECONDS)
	private Duration sessionTimeout = Duration.ofSeconds(30);

	private Duration readTimeout = Duration.ofMillis(1000);

	// getters / setters...
}

@ConfigurationProperties("my")
public class MyProperties {

	// fields...
	private final Duration sessionTimeout;
	private final Duration readTimeout;

	public MyProperties(
	  @DurationUnit(ChronoUnit.SECONDS) @DefaultValue("30s") Duration sessionTimeout,
	  @DefaultValue("1000ms") Duration readTimeout
	) {
		this.sessionTimeout = sessionTimeout;
		this.readTimeout = readTimeout;
	}

	// getters...
}
```

- `ns` for nanoseconds
- `us` for microseconds
- `ms` for milliseconds
- `s` for seconds
- `m` for minutes
- `h` for hours
- `d` for days

## Period

In addition to durations, Spring Boot can also work with Period type. The following formats can be used in application properties:

- An regular int representation (using days as the default unit unless a [@PeriodUnit](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/convert/PeriodUnit.html) has been specified)
- The standard ISO-8601 format [used by](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Period.html#parse(java.lang.CharSequence)) [Period](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Period.html)
- A simpler format where the value and the unit pairs are coupled (1y3d means 1 year and 3 days)

The following units are supported with the simple format:

- `y` for years
- `m` for months
- `w` for weeks
- `d` for days

## [DataSize](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.conversion.data-sizes)

```java
@ConfigurationProperties("my")
public class MyProperties {

	@DataSizeUnit(DataUnit.MEGABYTES)
	private DataSize bufferSize = DataSize.ofMegabytes(2);

	private DataSize sizeThreshold = DataSize.ofBytes(512);

	// getters/setters...
}
```

- `B` for bytes
- `KB` for kilobytes
- `MB` for megabytes
- `GB` for gigabytes
- `TB` for terabytes
