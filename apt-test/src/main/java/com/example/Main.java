package com.example;

import cz.ackee.rxoauth.annotations.IRxWrapper;
import rx.Observable;
import rx.functions.Func1;

/**
 * TODO add class description
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class Main {
    public static final String TAG = Main.class.getName();

    public static void main(String[] args) {
        System.out.println("Ahoj");
        TestInterface ti = new TestInterface() {
            @Override
            public Observable<String> login(String name, String password) {
                return Observable.just("name");
            }

            @Override
            public int getInt() {
                return 1;
            }

            @Override
            public boolean getBoolean() {
                return true;
            }
        };
        IRxWrapper wrapper = new IRxWrapper() {
            @Override
            public <T> Observable.Transformer<T, T> wrap() {
                return new Observable.Transformer<T, T>() {
                    @Override
                    public Observable<T> call(final Observable<T> observable) {
                        System.out.println("COMPOSING!");
                        return observable;
                    }
                };
            }
        };
        TestInterfaceWrapped wrapped = new TestInterfaceWrapped(ti, wrapper);
        wrapped.login("name", "password")
                .subscribe(x -> System.out.println(x), err->err.printStackTrace());

    }
}
