package cz.ackee.rxoauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies Retrofit service description, that should be wrapped with rx oauth code
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
@Target(ElementType.TYPE) @Retention(RetentionPolicy.CLASS)
public @interface OauthService {
}