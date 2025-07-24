package com.gmoon.springpoi.common.utils;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.gmoon.springpoi.common.exception.JsonConvertException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtil {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static String toString(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new JsonConvertException("Failed to serialize object to JSON: " + obj.getClass().getName(), e);
		}
	}

	public static <K, V> Map<K, V> toMap(String jsonStr) {
		try {
			return MAPPER.readValue(
				 jsonStr,
				 new TypeReference<>() {
				 }
			);
		} catch (Exception e) {
			throw new JsonConvertException("Failed to deserialize JSON to Map. Input: ", jsonStr, e);
		}
	}

	public static <T> T fromJson(String jsonStr, Class<T> clazz) {
		try {
			return MAPPER.readValue(jsonStr, clazz);
		} catch (Exception e) {
			throw new JsonConvertException("Failed to deserialize JSON to " + clazz.getName() + ". Input: " + jsonStr,
				 jsonStr, e);
		}
	}
}
