package cz.ackee.wrapper;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import cz.ackee.wrapper.annotations.NoCompose;

/**
 * Method that will be wrapped iwth oauth handling
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public class WrapEnclosingMethod {
    public static final String TAG = WrapEnclosingMethod.class.getName();
    private Name methodName;
    private TypeMirror returnType;
    private boolean isPrivate;
    private List<? extends VariableElement> parameters;
    private boolean shouldWrap;

    public WrapEnclosingMethod(ExecutableElement methodElement) {
        scanElement(methodElement);

    }

    public boolean isReachable() {
        return !isPrivate;
    }

    private void scanElement(ExecutableElement methodElement) {

        this.methodName = methodElement.getSimpleName();
        this.returnType = methodElement.getReturnType();
        this.isPrivate = methodElement.getModifiers().contains(Modifier.PRIVATE) ||
                methodElement.getModifiers().contains(Modifier.PROTECTED);
        this.parameters = methodElement.getParameters();
        this.shouldWrap = methodElement.getAnnotation(NoCompose.class) == null;
        boolean foundSomeClass = false;
        if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType type = (DeclaredType) returnType;
            TypeElement clz = (TypeElement) type.asElement();
            foundSomeClass = clz.getQualifiedName().toString().equals("rx.Observable")
                    || clz.getQualifiedName().toString().equals("rx.Single")
                    || clz.getQualifiedName().toString().equals("rx.Completable");
        }
        this.shouldWrap = this.shouldWrap && foundSomeClass;
    }

    public MethodSpec generateCode() {

        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName.toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(returnType));

        String paramNames = "";
        for (VariableElement element : this.parameters) {
            if (paramNames.length() > 0) {
                paramNames += ", ";
            }
            builder.addParameter(TypeName.get(element.asType()), element.getSimpleName().toString());
            paramNames += element.getSimpleName().toString();
        }
        builder.addStatement("return this.service.$L($L)$L", methodName.toString(), paramNames, shouldWrap ? ".compose(this.rxWrapper.wrap())" : "");

        return builder.build();
    }
}
