# RxOauth2 Android Library

Simple reactive extension, that adds support to Retrofit2 based projects which uses OAuth2 authentication. 



### Description
Library will handle posting only one refresh token request in cases where multiple simultaneous requests are fired to server and 401 is returned to all of them.

When refreshing token fails, client is notified via Event listener that is passed in constructor. 

### Dependencies
```groovy
compile 'cz.ackee.rxoauth:rxoauth:1.0.2'

```

### Usage
Working sample is provided in `app` module
#### Initialization
This should be present in some interactor with Api (in our cases, ApiInteractorImpl constructor)
```java
this.rxOauth = new RxOauthManaging(ctx, //context for creating shared preferneces with oauth info
                    this, // service that will call refresh token request
                    () -> { // listener for logout
        // lead user to logout screen somehow
        // (this is a bad example, it does not handle all edge cases
        App.getInstance().startActivity(new Intent(App.getInstance(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
});
```
#### Storing credentials
You can save oauth credentials via method `saveOauthCredentials(String accessToken, String refreshToken)` or via `saveOauthCredentials(ICredentialsModel credentials)` method. ICredentials method requires only two methods - `getAccessToken()` and `getRefreshToken()`
```java
    this.apiDescription.login(name, password)
        .doOnNext(response -> this.rxOauth.saveCredentials(response));
```
### Logout
When you want to clear data, just call `logout()` method on rxOauth
```java
this.apiDescription.logout()
    .doOnNext(response -> this.rxOauth.logout())
```

### Check for access/refresh token expiration
On requests you want to check for tokens expiration you have to call our transformer of Rx observers
```java
    this.apiDescription.getData()
        .compose(this.rxOauth.wrapWithOauthHandling());
```