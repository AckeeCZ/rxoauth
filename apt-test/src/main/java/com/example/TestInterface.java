package com.example;

import cz.ackee.rxoauth.annotations.NoOauth;
import cz.ackee.rxoauth.annotations.OauthService;
import rx.Observable;

@OauthService
public interface TestInterface {
     Observable<String> login(String name, String password);

    public int getInt();

    @NoOauth
    public boolean getBoolean();
}
