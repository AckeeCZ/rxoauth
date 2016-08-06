package cz.ackee.rxoauth;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import cz.ackee.rxoauth.annotations.IRxWrapper;
import cz.ackee.rxoauth.annotations.NoOauth;
import cz.ackee.rxoauth.processor.ApiProcessor;

/**
 * Class representing one interface/class that was annotated by {@link  cz.ackee.rxoauth.annotations.OauthService}
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public class OauthAnnotatedClass {
    public static final String TAG = OauthAnnotatedClass.class.getName();
    private static final String SUFFIX = "Wrapped";
    private final TypeElement element;

    private List<OauthEnclosingMethod> methodsToGenerate;
    private final String name;

    public OauthAnnotatedClass(TypeElement element) {
        this.element = element;
        methodsToGenerate = new ArrayList<>();
        name = element.getSimpleName().toString();
        checkForMethods();

    }

    private void checkForMethods() {
        for (Element elem : element.getEnclosedElements()) {
            if (elem.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) elem;
            OauthEnclosingMethod method = new OauthEnclosingMethod(methodElement);
            if (!method.isReachable()) {
                continue;
            }
            methodsToGenerate.add(method);
        }
    }

    public TypeElement getElement() {
        return element;
    }

    public TypeSpec generateCode() {
        // Generate the mapper class
        String mapperClassName = name + SUFFIX;
        TypeSpec.Builder mapperClass = TypeSpec.classBuilder(mapperClassName)
                .addField(TypeName.get(this.element.asType()), "service")
                .addField(TypeName.get(IRxWrapper.class), "rxWrapper")
                .addJavadoc("Generated class that encapsulates method of $T with RxOauth handling\n",
                        ClassName.get(getElement()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(generateConstructor());

        for(OauthEnclosingMethod method : methodsToGenerate) {
            mapperClass.addMethod(method.generateCode());
        }
        return mapperClass.build();

    }

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(this.element.asType()), "service")
                .addParameter(TypeName.get(IRxWrapper.class), "rxWrapper")
                .addStatement("this.service = service")
                .addStatement("this.rxWrapper = rxWrapper")
                .build();
    }
}
