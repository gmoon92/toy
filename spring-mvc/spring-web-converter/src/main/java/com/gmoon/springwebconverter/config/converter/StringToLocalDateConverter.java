package com.gmoon.springwebconverter.config.converter;

import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.stereotype.Component;

import com.gmoon.javacore.util.DateUtils;
import com.gmoon.springwebconverter.config.annotation.LocalEndDate;
import com.gmoon.springwebconverter.config.annotation.LocalStartDate;
import com.gmoon.springwebconverter.config.constants.HttpSessionAttributeKeys;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StringToLocalDateConverter implements ConditionalGenericConverter {

	private final HttpSession session;

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Set.of(new ConvertiblePair(String.class, Date.class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return targetType.hasAnnotation(LocalStartDate.class)
			 || targetType.hasAnnotation(LocalEndDate.class);
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		log.info("Converting source [{}] (type: {}) to target {}",
			 source, source == null ? "null" : source.getClass(), targetType);
		if (source == null) {
			return null;
		}
		String pattern = getDatePattern();
		Date wallTime = DateUtils.toDate(pattern, (String)source);
		ZoneId userZoneId = getUserZoneId();

		if (targetType.hasAnnotation(LocalStartDate.class)) {
			Object result = DateUtils.truncateAndAdjust(wallTime, userZoneId);
			log.info("@LocalStartDate, result value: [{}], type: {}", result, result.getClass());
			return result;
		}

		if (targetType.hasAnnotation(LocalEndDate.class)) {
			Object result = DateUtils.truncateAndAdjustEndDt(wallTime, userZoneId);
			log.info("@LocalEndDate, result value: [{}], type: {}", result, result.getClass());
			return result;
		}
		log.warn("No matching annotation, returning raw source [{}]", source);
		return source;
	}

	private ZoneId getUserZoneId() {
		String userZoneId = (String)session.getAttribute(HttpSessionAttributeKeys.USER_TIME_ZONE);
		if (userZoneId == null) {
			log.info("user zone id is null");
			return ZoneId.systemDefault();
		} else {
			log.info("user zone id is {}", userZoneId);
			return ZoneId.of(userZoneId);
		}
	}

	private String getDatePattern() {
		String pattern = (String)session.getAttribute(HttpSessionAttributeKeys.USER_DATE_PATTERN);
		if (pattern == null) {
			log.debug("user date pattern is null");
			return "yyyyMMdd";
		}
		return pattern;
	}
}
