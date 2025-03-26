package com.rsupport.rv5x.springaccesslog.http.aspect;

import com.rsupport.rv5x.springaccesslog.http.annotation.ApiRequestField;
import com.rsupport.rv5x.springaccesslog.http.annotation.ApiRequestModel;
import com.rsupport.rv5x.springaccesslog.http.constant.ApiRequestFieldName;
import com.rsupport.rv5x.springaccesslog.http.context.HttpRequestPayload;
import com.rsupport.rv5x.springaccesslog.http.context.HttpRequestPayloadContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class ApiEventTrackingAspect {

	private static final Map<Class<?>, Map<ApiRequestFieldName, Field>> cachedFields = new ConcurrentHashMap<>();

	@AfterReturning("@annotation(com.rsupport.rv5x.springaccesslog.http.annotation.ApiEventTracking)")
	public void monitoring(JoinPoint jp) {
		Object[] args = jp.getArgs();
		for (Object arg : args) {
			Class<?> parameterClass = arg.getClass();
			if (parameterClass.isAnnotationPresent(ApiRequestModel.class)) {
				Map<ApiRequestFieldName, Field> fields = cachedFields.computeIfAbsent(parameterClass, this::initializeFieldCache);

				String username = findFieldValue(fields, arg, ApiRequestFieldName.USERNAME);
				Integer age = findFieldValue(fields, arg, ApiRequestFieldName.AGE);

				String targetClass = jp.getTarget().getClass().getSimpleName();
				String methodName = jp.getSignature().toShortString();
				HttpRequestPayload httpRequestPayload = HttpRequestPayloadContextHolder.getContext();
				log.info("{}.{} username: {}, age: {}, payload: {}", targetClass, methodName, username, age, httpRequestPayload.getRequestBody());
				return;
			}
		}
	}

	private Map<ApiRequestFieldName, Field> initializeFieldCache(Class<?> clazz) {
		Map<ApiRequestFieldName, Field> fieldMap = new ConcurrentHashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(ApiRequestField.class)) {
				ApiRequestField annotation = field.getAnnotation(ApiRequestField.class);
				field.setAccessible(true);
				fieldMap.put(annotation.value(), field);
			}
		}
		return fieldMap;
	}

	@SuppressWarnings("unchecked")
	private <T> T findFieldValue(Map<ApiRequestFieldName, Field> fields, Object arg, ApiRequestFieldName fieldName) {
		Field field = fields.get(fieldName);
		if (field == null) {
			return null;
		}
		try {
			Object value = field.get(arg);
			return (T) value;
		} catch (Exception e) {
			log.error("Error accessing field value for {}: {}", field.getName(), e.getMessage());
			return null;
		}
	}
}
