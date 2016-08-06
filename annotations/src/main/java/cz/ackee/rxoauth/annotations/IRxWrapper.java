package cz.ackee.rxoauth.annotations;

import javax.xml.transform.TransformerFactoryConfigurationError;

import rx.Observable;

/**
 * Interface that should
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public interface IRxWrapper {
    public static final String TAG = IRxWrapper.class.getName();

    public <T> Observable.Transformer<T, T> wrap();
}
