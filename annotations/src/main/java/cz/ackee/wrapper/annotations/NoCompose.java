package cz.ackee.wrapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that we dont want to compose result of method call
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
@Target(ElementType.METHOD) @Retention(RetentionPolicy.CLASS)
public @interface NoCompose {
}
