package com.gmoon.batchinsert.global.config.processor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotationProcessorUtils {

	/**
	 * Annotation Processing 환경에서 해당 Element의 annotation attribute 값을 추출한다.
	 *
	 * <p><b>중요:</b>
	 * Javac에서 Element.getAnnotation(...) 은 항상 동작하지 않는다.
	 * 반드시 AnnotationMirror에서 값을 직접 파싱한다.
	 *
	 * @param element        대상 Element (예: 필드)
	 * @param annotationType 어노테이션 클래스 (예: Column.class)
	 * @param attributeName  애트리뷰트 이름 (예: "name")
	 * @return 값(없으면 null)
	 */
	public static <T> T getAnnotationAttributeValue(Element element,
													Class<? extends Annotation> annotationType,
													String attributeName,
													Class<T> resultType
	) {
		Object value = element.getAnnotationMirrors().stream()
			 .filter(am -> hasAnnotation(am, annotationType))
			 .flatMap(am -> am.getElementValues().entrySet().stream())
			 .filter(entry -> attributeName.equals(entry.getKey().getSimpleName().toString()))
			 .map(entry -> entry.getValue().getValue())
			 .findFirst()
			 .orElse(null);
		return resultType.isInstance(value) ? resultType.cast(value) : null;
	}

	public static boolean doesNotHaveAnnotation(Element element, Class<? extends Annotation> annotationType) {
		return element.getAnnotationMirrors().stream()
			 .noneMatch(am -> hasAnnotation(am, annotationType));
	}

	public static boolean hasAnnotation(Element element, Class<? extends Annotation> annotationType) {
		return element.getAnnotationMirrors().stream()
			 .anyMatch(am -> hasAnnotation(am, annotationType));
	}

	private static boolean hasAnnotation(AnnotationMirror am, Class<? extends Annotation> annotationType) {
		String mirrorFqcn = am.getAnnotationType().asElement().toString();
		return mirrorFqcn.equals(annotationType.getCanonicalName());
	}

	public static List<Element> findAllMembersFields(TypeElement typeElement, Set<Modifier> ignoredModifiers) {
		List<Element> fields = new ArrayList<>();
		collectAllFields(typeElement, fields, ignoredModifiers);
		return fields;
	}

	private static void collectAllFields(TypeElement typeElement, List<Element> fields, Set<Modifier> ignoredModifiers) {
		for (Element e : typeElement.getEnclosedElements()) {
			Set<Modifier> modifiers = e.getModifiers();
			if (e.getKind() == ElementKind.FIELD
				 && ignoredModifiers.stream().noneMatch(modifiers::contains)
			){
				fields.add(e);
			}
		}

		TypeMirror superClass = typeElement.getSuperclass();
		boolean hasSuperType = superClass != null && superClass.getKind() != TypeKind.NONE;
		if (hasSuperType) {
			Element superTypeElement = ((DeclaredType) superClass).asElement();
			if (superTypeElement instanceof TypeElement ste
				 && !ste.getQualifiedName().contentEquals("java.lang.Object")) {
				collectAllFields(ste, fields, ignoredModifiers);
			}
		}
	}

	/**
	 * JSR-269 getAllMembers 기반의 (제한적) 필드 수집 메서드.
	 * <ul>
	 *   <li>부모(private) 필드는 수집에서 제외됨 (자바 상속/ 접근제한 규칙 탓)</li>
	 *   <li>실제 필드 전부(상속, 접근제한 무관) 필요 시 {@link #findAllMembersFields(TypeElement)} 사용!</li>
	 * </ul>
	 *
	 * @deprecated using by {@link #findAllMembersFields(TypeElement)}
	 */
	private List<? extends Element> findAllMembersFields(ProcessingEnvironment processingEnv, TypeElement typeElement) {
		Elements elementUtils = processingEnv.getElementUtils();
		return elementUtils.getAllMembers(typeElement)
			 .stream()
			 .filter(e -> e.getKind() == ElementKind.FIELD)
			 .filter(e -> !e.getModifiers().contains(javax.lang.model.element.Modifier.STATIC))
//			 .flatMap(e -> e.getEnclosedElements().stream())
			 .toList();
	}
}
