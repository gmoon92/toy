package com.gmoon.springdataredis.util;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonUtils {

	public static String toString(Object obj) {
		return toString(obj, false);
	}

	public static String toString(Object obj, boolean isSafeNullString) {
		try {
			ObjectMapper mapper = getObjectMapper();
			setConfigOfBlankIfNull(mapper, isSafeNullString);

			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setConfigOfBlankIfNull(ObjectMapper mapper, boolean isSafeNullString) {
		if (isSafeNullString) {
			mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
			mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

			SerializerProvider provider = mapper.getSerializerProvider();
			provider.setNullValueSerializer(NullSerializer.INSTANCE);
		}
	}

	public static <T> T toObject(String str, Class<T> mappedType) {
		return toObject(mapper -> {
			try {
				return mapper.readValue(str, mappedType);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}, false, false);
	}

	public static <T> T toObject(String str, TypeReference<T> typeReference) {
		return toObject(mapper -> {
			try {
				return mapper.readValue(str, typeReference);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}, false, false);
	}

	private static <T> T toObject(Function<ObjectMapper, T> reader, boolean allowUnquotedControlChars,
		boolean allowBackSlashEscapeChars) {
		ObjectMapper objectMapper = getObjectMapper(allowUnquotedControlChars, allowBackSlashEscapeChars);
		return reader.apply(objectMapper);
	}

	private static ObjectMapper getObjectMapper() {
		return getObjectMapper(false, false);
	}

	private static ObjectMapper getObjectMapper(boolean allowUnquotedControlChars, boolean allowBackSlashEscapeChars) {
		ObjectMapper mapper = new ObjectMapper();

		if (allowUnquotedControlChars) {
			mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
		}

		if (allowBackSlashEscapeChars) {
			mapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
		}

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	private static class NullSerializer extends JsonSerializer<Object> {
		private static final NullSerializer INSTANCE = new NullSerializer();

		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString("");
		}
	}
}
