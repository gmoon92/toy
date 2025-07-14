package com.gmoon.commons.commonsapachepoi.excel.vo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gmoon.commons.commonsapachepoi.common.utils.ReflectionUtil;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelComponent;
import com.gmoon.commons.commonsapachepoi.excel.annotation.ExcelProperty;
import com.gmoon.commons.commonsapachepoi.excel.converter.ExcelConverter;
import com.gmoon.commons.commonsapachepoi.excel.predicate.ExcelValidator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

@Getter
public class ExcelFields {
	private final Map<Integer, ExcelField> value;

	public ExcelFields(Class<?> excelModelClass, HttpServletRequest request) {
		ServletContext servletContext = request.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

		Map<Integer, ExcelField> result = ReflectionUtil.getDeclaredAnnotationFieldMap(
			 excelModelClass,
			 ExcelProperty.class,
			 field -> new ExcelField(field, field.getAnnotation(ExcelProperty.class), ctx)
		);

		if (CollectionUtils.isEmpty(result)) {
			throw new UnsupportedOperationException(
				 String.format("@ExcelProperty annotation not found in class %s", excelModelClass.getName())
			);
		}

		this.value = Collections.unmodifiableMap(result);
	}

	public int size() {
		return value.size();
	}

	public Set<Map.Entry<Integer, ExcelField>> entrySet() {
		return value.entrySet();
	}

	@Getter
	public static class ExcelField {
		private final Field field;
		private final boolean required;
		private final List<ExcelValidator> validators;
		private final ExcelConverter<?> converter;

		private ExcelField(Field field, ExcelProperty annotation, ApplicationContext ctx) {
			this.field = field;
			this.required = annotation.required();
			this.validators = getValidators(annotation, ctx);
			this.converter = getConverter(annotation, ctx);
		}

		private List<ExcelValidator> getValidators(ExcelProperty annotation, ApplicationContext ctx) {
			Class<? extends ExcelValidator>[] validators = annotation.validators();
			if (validators == null) {
				return Collections.emptyList();
			}

			List<ExcelValidator> list = new ArrayList<>();
			for (Class<? extends ExcelValidator> clazz : validators) {
				ExcelValidator validator = AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) == null
					 ? ReflectionUtil.newInstance(clazz)
					 : ctx.getBean(clazz);
				list.add(validator);
			}
			return Collections.unmodifiableList(list);
		}

		private ExcelConverter<?> getConverter(ExcelProperty annotation, ApplicationContext ctx) {
			Class<? extends ExcelConverter<?>> clazz = annotation.converter();
			return AnnotationUtils.findAnnotation(clazz, ExcelComponent.class) == null
				 ? ReflectionUtil.newInstance(clazz)
				 : ctx.getBean(clazz);
		}
	}
}
