package com.gmoon.springwebconverter.config.interceptor;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.gmoon.springwebconverter.config.annotation.LocalEndDate;
import com.gmoon.springwebconverter.config.annotation.LocalStartDate;

public class LocalDateAnnotationIntrospector extends NopAnnotationIntrospector {

	@Override
	public Object findDeserializer(Annotated annotated) {
		if (annotated.hasAnnotation(LocalStartDate.class)) {
			return LocalStartDateJsonDeserializer.class;
		} else if (annotated.hasAnnotation(LocalEndDate.class)) {
			return LocalEndDateJsonDeserializer.class;
		}
		return super.findDeserializer(annotated);
	}
}
