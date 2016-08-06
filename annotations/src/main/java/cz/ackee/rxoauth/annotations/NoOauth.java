package cz.ackee.rxoauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that we dont want oauth check for this method
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
@Target(ElementType.METHOD) @Retention(RetentionPolicy.CLASS)
public @interface NoOauth {
}
