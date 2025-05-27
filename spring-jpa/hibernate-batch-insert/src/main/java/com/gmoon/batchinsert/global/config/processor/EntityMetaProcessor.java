package com.gmoon.batchinsert.global.config.processor;

import com.gmoon.javacore.util.StringUtils;
import com.google.auto.service.AutoService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import org.springframework.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 어노테이션 프로세서 자바 컴파일(javac) 등록
 * META-INF/services/javax.annotation.processing.Processor
 * <p>
 * {@link AutoService(Processor)}를 선언시
 * 컴파일 시 관련 META-INF/services 파일을 자동으로 생성.
 * <p>
 * 단일 모듈일 경우
 * 컴파일 시점에 .class 파일 생성전 META-INF/services/javax.annotation.processing.Processor 먼저 생성되어
 * ClassNotFoundException 예외 발생. {@link #process(Set, RoundEnvironment)} 미동작될 수 있음.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("jakarta.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EntityMetaProcessor extends AbstractProcessor {
	private static final boolean PROCESS_HERE_ONLY = false;

	private Messager messager;
	private Elements elementUtils;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		messager = processingEnv.getMessager();
		elementUtils = processingEnv.getElementUtils();
	}

	/**
	 * <p><a href="http://hauchee.blogspot.com/2015/12/compile-time-annotation-processing-getting-class-value.html">Why does MirroredTypesException happen ?</a>
	 * <p>The annotation returned by this method could contain an element whose value is of type Class.
	 * This value cannot be returned directly: information necessary to locate and load a class (such as the class loader to use) is not available, and the class might not be loadable at all.
	 * Attempting to read a Class object by invoking the relevant method on the returned annotation will result in a MirroredTypeException, from which the corresponding TypeMirror may be extracted.
	 * Similarly, attempting to read a Class[]-valued element will result in a <u>MirroredTypesException</u>.
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);
		if (entities.isEmpty()) {
			return false;
		}

		messager.printMessage(Diagnostic.Kind.NOTE, "==== EntityMetaProcessor process running! ====");
		for (Element entity : entities) {
			try {
				generateMetaClass(entity);
			} catch (Exception e) {
				messager.printMessage(Diagnostic.Kind.ERROR, "Error >>" + e.getMessage());
			}
		}
		return PROCESS_HERE_ONLY;
	}

	private void generateMetaClass(Element element) {
		TypeSpec metaClassSpec = getMetaClassSpec(element);
		String metaClassName = metaClassSpec.name;

		String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
		messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s.%s", packageName, metaClassName));
		try {
			JavaFile.builder(packageName, metaClassSpec)
				 .build()
				 .writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private TypeSpec getMetaClassSpec(Element element) {
		List<FieldSpec> fields = getFieldSpecs((TypeElement) element);

		Name className = element.getSimpleName();
		String metaClassName = "M" + className;
		return TypeSpec.classBuilder(metaClassName)
			 .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			 .addFields(fields)
			 .addMethod(newToStringMethodSpec(metaClassName, fields))
			 .build();
	}

	private List<FieldSpec> getFieldSpecs(TypeElement element) {
		Set<Modifier> ignoredModifiers = Set.of(Modifier.STATIC, Modifier.TRANSIENT);
		return AnnotationProcessorUtils.findAllMembersFields(element, ignoredModifiers)
			 .stream()
			 .filter(field -> AnnotationProcessorUtils.doesNotHaveAnnotation(field, Transient.class))
			 .map(this::newFieldSpec)
			 .toList();
	}

	private FieldSpec newFieldSpec(Element field) {
		messager.printMessage(Diagnostic.Kind.NOTE,
			 "field: " + field.getSimpleName()
				  + " " + field.getKind()
				  + " mods=" + field.getModifiers()
				  + " type=" + field.asType()
		);

		String columnName = field.getSimpleName().toString();
		String annotationElementValue = AnnotationProcessorUtils.getAnnotationAttributeValue(
			 field,
			 Column.class,
			 "name",
			 String.class
		);
		if (StringUtils.isNotBlank(annotationElementValue)) {
			columnName = annotationElementValue;
		}

		return FieldSpec.builder(
				  TypeName.get(String.class),
				  columnName
			 )
			 .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			 .initializer("$S", columnName)
			 .build();
	}

	private MethodSpec newToStringMethodSpec(String className, List<FieldSpec> fields) {
		String fieldNames = fields.stream()
			 .map(f -> f.name)
			 .collect(Collectors.joining(", "));
		return MethodSpec.methodBuilder("toString")
			 .addAnnotation(Override.class)
			 .addModifiers(Modifier.PUBLIC)
			 .returns(String.class)
			 .addStatement("return $S", String.format("%s(%s)", className, fieldNames))
			 .build();
	}
}
