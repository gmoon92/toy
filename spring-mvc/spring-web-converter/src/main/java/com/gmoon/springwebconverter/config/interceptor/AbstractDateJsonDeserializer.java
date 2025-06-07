package com.gmoon.springwebconverter.config.interceptor;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Supplier;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.gmoon.javacore.util.DateUtils;
import com.gmoon.springwebconverter.config.constants.HttpSessionAttributeKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public abstract class AbstractDateJsonDeserializer extends DateDeserializers.DateDeserializer {
	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String text = p.getText();
		String pattern = getDatePattern();
		ZoneId userZoneId = getZoneId();
		try {
			log.info("Deserializing date. text='{}', pattern='{}', userZoneId='{}'", text, pattern, userZoneId);
			Date wallTime = DateUtils.toDate(pattern, text);
			Date result = getDate(wallTime, userZoneId);
			log.info("Converted date='{}' (type={})", result, result != null ? result.getClass().getName() : "null");
			return result;
		} catch (Exception e) {
			log.error(
				 "Date deserialization failed. text='{}', pattern='{}', userZoneId='{}'. Falling back to super.deserialize. Cause: {}",
				 text, pattern, userZoneId, e.toString());
			return super.deserialize(p, ctxt);
		}
	}

	public abstract Date getDate(Date wallTime, ZoneId userZoneId);

	private String getDatePattern() {
		return getSessionAttributeOrDefault(HttpSessionAttributeKeys.USER_DATE_PATTERN, () -> "yyyyMMdd");
	}

	private ZoneId getZoneId() {
		String timezone = getSessionAttributeOrDefault(HttpSessionAttributeKeys.USER_TIME_ZONE, () -> "UTC");
		try {
			return ZoneId.of(timezone);
		} catch (Exception e) {
			log.warn("Invalid zoneId '{}' in session. Falling back to UTC. Cause: {}", timezone, e.toString());
			return ZoneId.of("UTC");
		}
	}

	private <T> T getSessionAttributeOrDefault(String attributeName, Supplier<T> defaultValue) {
		HttpSession httpSession = getHttpSession();
		if (httpSession == null) {
			log.info("No HttpSession found when getting '{}', using default.", attributeName);
			return defaultValue.get();
		}
		Object attribute = httpSession.getAttribute(attributeName);
		if (attribute == null) {
			log.info("'{}' not found in session, using default.", attributeName);
			return defaultValue.get();
		}
		log.info("Found session attribute '{}': '{}'", attributeName, attribute);
		return (T)attribute;
	}

	private HttpSession getHttpSession() {
		ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			HttpServletRequest req = attrs.getRequest();
			return req.getSession(false);
		}
		return null;
	}
}
