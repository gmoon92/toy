package com.gmoon.batchinsert.global.config.processor;

import com.google.auto.service.AutoService;
import jakarta.persistence.Entity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

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
			return true;
		}

		messager.printMessage(Diagnostic.Kind.NOTE, "==== EntityMetaProcessor process running! ====");
		for (Element entity : entities) {
			try {
				// This will throw a MirroredTypesException
				// which is intentional throwing for using MirroredTypesException.getTypeMirrors()
				generator(entity);
			} catch (MirroredTypesException e) {
				generator(entity);
			}
		}
		return PROCESS_HERE_ONLY;
	}

	private void generator(Element element) {
		String targetPackage = elementUtils.getPackageOf(element).getQualifiedName().toString();
		String className = "M" + element.getSimpleName();

		messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s.%s", targetPackage, className));
		try {
			JavaFileObject jfo = processingEnv.getFiler()
				 .createSourceFile(targetPackage + "." + className);
			try (PrintWriter out = new PrintWriter(jfo.openWriter())) {
				out.append("package " + targetPackage + ";\n\n")
					 .append("public class " + className + " {\n");

				if (element instanceof TypeElement typeElement) {
					writeFields(out, typeElement);
				}

				out.append("}");
				out.flush();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private PrintWriter writeFields(PrintWriter out, TypeElement typeElement) {
		List<? extends Element> fields = getFieldsIncludingSuper(typeElement);
		for (Element field : fields) {
			String fieldName = field.getSimpleName().toString();
			out.append("    public static final String " + fieldName + " = \"" + fieldName + "\";\n");
		}
		return out;
	}

	private List<? extends Element> getFieldsIncludingSuper(TypeElement typeElement) {
		return elementUtils.getAllMembers(typeElement)
			 .stream()
			 .filter(e -> e.getKind() == ElementKind.FIELD)
			 // .flatMap(e -> e.getEnclosedElements().stream())
			 // .filter(e -> !e.getModifiers().contains(javax.lang.model.element.Modifier.STATIC))
			 .toList();
	}
}
