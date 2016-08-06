package cz.ackee.wrapper.annotations;

import rx.Observable;

/**
 * Wrapper that is composed with every method call in {@link WrappedService} annotated classes
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public interface IComposeWrapper {

    <T> Observable.Transformer<T, T> wrap();
}
