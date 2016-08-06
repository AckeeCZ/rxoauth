package cz.ackee.wrapper.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import cz.ackee.wrapper.WrapAnnotatedClass;
import cz.ackee.wrapper.annotations.NoCompose;
import cz.ackee.wrapper.annotations.WrappedService;

@AutoService(Processor.class)
public class ApiProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private  Messager messager;

    List<WrapAnnotatedClass> annotatedClasses;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(WrappedService.class.getCanonicalName());
        annotataions.add(NoCompose.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotatedClasses = new ArrayList<>();
        for (Element elem : roundEnv.getElementsAnnotatedWith(WrappedService.class)) {
            if (elem.getKind() != ElementKind.INTERFACE && elem.getKind() != ElementKind.CLASS) {
                error(elem, "Only classes or interfaces can be annotated with @%s",
                        WrappedService.class.getSimpleName());
                return true;
            }
            TypeElement element = (TypeElement) elem;
            annotatedClasses.add(new WrapAnnotatedClass(element));
        }
        generateCode();
        return true; // no further processing of this annotation type
    }

    private void generateCode() {
        for(WrapAnnotatedClass clz : annotatedClasses) {
            String packageName = getPackageName(clz);
            try {
                JavaFile.builder(packageName, clz.generateCode()).build().writeTo(filer);
            }catch (Exception e) {
                error(clz.getElement(), e.getMessage());
            }
        }
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    /**
     * Get the package name of a certain clazz
     *
     * @param clazz The class you want the packagename for
     * @return The package name
     */
    private String getPackageName(WrapAnnotatedClass clazz) {
        PackageElement pkg = elementUtils.getPackageOf(clazz.getElement());
        return pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();
    }


}
