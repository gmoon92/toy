package com.gmoon.batchinsert.global;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

import jakarta.persistence.Entity;

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
		messager.printMessage(Diagnostic.Kind.NOTE, "==== EntityMetaProcessor process running! ====");
		Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(Entity.class);
		for (Element entity : entities) {
			Entity annotation = entity.getAnnotation(Entity.class);
			// try {
			// 	// This will throw a MirroredTypesException
			// 	// which is intentional throwing for using MirroredTypesException.getTypeMirrors()
			// 	annotation.name();
			// } catch (MirroredTypesException e) {
			// }
			generator(annotations, entity);
		}
		return PROCESS_HERE_ONLY;
	}

	private void generator(Set<? extends TypeElement> annotations, Element elem) {
		String className = "M" + elem.getSimpleName();
		String targetPackage = ((PackageElement) elem.getEnclosingElement())
			 .getQualifiedName().toString();

		messager.printMessage(
			 Diagnostic.Kind.NOTE,
			 String.format("Processing %s.%s", targetPackage, className)
		);

		List<? extends Element> fields = elem.getEnclosedElements().stream()
			 .filter(e -> e.getKind() == ElementKind.FIELD)
			 .toList();
		try {
			JavaFileObject jfo = processingEnv.getFiler()
				 .createSourceFile(targetPackage + "." + className);
			try (PrintWriter out = new PrintWriter(jfo.openWriter())) {
				out.println("package " + targetPackage + ";");
				out.println("public class " + className + " {");
				for (Element field : fields) {
					String fieldName = field.getSimpleName().toString();
					out.println("    public static final String " + fieldName + " = \"" + fieldName + "\";");
				}
				out.println("}");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
