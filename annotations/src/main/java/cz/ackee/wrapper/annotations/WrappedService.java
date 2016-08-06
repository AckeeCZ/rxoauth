package cz.ackee.wrapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks class or interface. Wrapper will be
 * generated and encapsulated every method of this class.
 *
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
@Target(ElementType.TYPE) @Retention(RetentionPolicy.CLASS)
public @interface WrappedService {
}