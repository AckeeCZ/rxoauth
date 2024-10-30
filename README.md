# DEPRECATED
This library is no longer maintained and repository is archived.

[ ![Download](https://api.bintray.com/packages/ackeecz/rxoauth2/core/images/download.svg)](https://bintray.com/ackeecz/rxoauth2/core/_latestVersion)
# RxOAuth2 Android Library
Simple reactive extension, that adds support to Retrofit2 based projects which uses OAuth2 authentication.

This library is based on RxJava2.
## Core
### Description
- `RxOAuthManager` provides wrapping for RxJava2 streams, which automatically handles access token expiration and performs token refresh. In case of success, new credentials are stored in `SharedPreferences`. When refresh token is invalid, the optional logic provided in `onRefreshTokenFailed` is performed. With custom `ErrorChecker`, the user may customize access and refresh tokens errors validation
- `OAuthInterceptor`, which is provided by `RxOAuthManager` adds `Authorization` header with access token to OkHttp requests
- `DefaultOAuthCredentials` is the default implementation of `OAuthCredentials`

### Dependencies
```groovy
compile 'cz.ackee.rxoauth2:core:x.x.x'
```

### Usage
Working sample is provided in `app` module.

#### Initialization
Create `RxOAuthManager` typically in API access layer (in our case, ApiInteractorImpl):
```kotlin
class ApiInteractorImpl @Inject constructor(private val apiDescription: ApiDescription,
                                            context: Context) : ApiInteractor {

    private val rxOAuthManager: RxOAuthManager = RxOAuthManager(
            context = context,
            refreshTokenAction = { refreshToken ->
                apiDescription.refreshToken(refreshToken) // API call for token refresh
            },
            onRefreshTokenFailed = { err ->
                // Fallback, e.g. log out
            })
}
```
#### Storing credentials
You can save OAuth credentials with `saveCredentials(credentials: OAuthCredentials)` method. You may want to do this after receiving credentials from server, e.g. after login or sign in.
```kotlin
    apiDescription.login(name, password)
        .doOnNext { credentials -> rxOAuthManager.saveCredentials(credentials) }
```
### Logout
After logging out, you may want to remove credentials from the store.
```kotlin
apiDescription.logout()
    .doOnNext { response -> rxOAuthManager.clearCredentials() }
```

### Check for access/refresh token expiration
To wrap your requests with OAuth handling, just call `wrapWithOAuthHandlingObservable()`, `wrapWithOAuthHandlingSingle()` or `wrapWithOAuthHandlingCompletable()` on the stream you want. `RxOAuthManager` supports `Observable`, `Single` and `Completable`.
```kotlin
    apiDescription.getData().wrapWithOAuthHandlingSingle(rxOAuthManager)
```

### RxWrapper
To avoid boilerplate for each request, you may use another library we developed - [RxWrapper](https://github.com/AckeeCZ/rxwrapper), which wraps the whole interface functions that use RxJava2 with custom `Transformer`s using `compose` operator. Now you need to apply `wrapWithOAuthHandling...()` only once and work with generated wrapper insteado of original `apiDescription`. If you want to exclude some function from wrapping, just mark it with `@NoCompose` annotation.
Initialization in constructor of Api Interactor:
```kotlin
private var apiWrapper = ApiDescriptionWrapped(apiDescription, object : IComposeWrapper {
        override fun <T : Any?> wrapSingle(): SingleTransformer<T, T> {
            return rxOAuthManager.transformSingle()
        }

        override fun wrapCompletable(): CompletableTransformer {
            return rxOAuthManager.transformCompletable()
        }

        override fun <T : Any?> wrapObservable(): ObservableTransformer<T, T> {
            return rxOAuthManager.transformObservable()
        }
    })
```

## Retrofit2 adapter
### Description
From version 2.0.0, new module is available. Now RxOAuth2 wrapping may be provided as Retrofit2 `CallAdapter`.

### Dependencies
```groovy
compile 'cz.ackee.rxoauth2:retrofit-adapter:x.x.x'
```

### Usage
When creating your API service, just provide `RxOAuthCallAdapterFactory` to Retrofit builder. If you want to exclude some function from wrapping, just mark it with `@IgnoreAuth` annotation.
```kotlin
    val apiDescription: ApiDescription = retrofitBuilder
            .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(rxOAuthManager.provideAuthInterceptor())
                    .build())
            .addCallAdapterFactory(RxOAuthCallAdapterFactory(rxOAuthManager))
            .build()
            .create(ApiDescription::class.java)
```

## CHANGELOG
### 1.0.0
- Release of lib with RxJava2 support
### 1.0.3
- Abstract checker for errors to give user ability to change behavior when to refresh token
### 2.0.0
- Artifact naming change: `cz.ackee.rxoauth2:rxOAuth` -> `cz.ackee.rxoauth2:core`
- New module `Retrofit adapter` is created with artifact `cz.ackee.rxoauth2:retrofit-adapter`, more on this in section [Retrofit2 adapter](#retrofit2-adapter)
- `RxOAuthManager` now accepts `Context` or custom `SharedPreferences` in the constructor instead of `OAuthStore`
- `RxOAuthManager` now uses lambda action `refreshTokenAction: (String) -> Single<OAuthCredentials>` instead of `RefreshTokenService` class
- `RxOAuthManager` now uses lambda action `onRefreshTokenFailed: (Throwable) -> Unit` instead of `RefreshTokenFailedListener` class.
- New property `expiresIn` is added to `OAuthCredentials`. Now `OAuthStore` stores token expiration time and `RxOAuthManager` controls it locally before each request to avoid redundant API calls if access token is expired
- `OAuthStore` is now an internal class. The only way to store credentials is `saveCredentials(credentials: OAuthCredentials)` on `RxOAuthManager`. The same is with `clearCredentials()`
- `AuthInterceptor` is renamed to `OAuthInterceptor` an has now only internal constructor. The only way to get an instance is `provideAuthInterceptor()` function on `RxOAuthManager`
- `DefaultOAuthCredentials` class is added as default implementation of `OAuthCredentials`
- `DefaultErrorChecker` functions were renamed
### 2.1.0
- Rename `wrapWithOAuthHandlingObservable()`, `wrapWithOAuthHandlingSingle()` and `wrapWithOAuthHandlingCompletable()` functions, returning transformers to `transformObservable()`, `transformSingle()` and `transformCompletable()` respectively
- Add Kotlin extensions for RxJava entities: `wrapWithOAuthHandlingObservable()`, `wrapWithOAuthHandlingSingle()` and `wrapWithOAuthHandlingCompletable()`
### 2.1.1
- Fix `NullPointerException` on `Completable` handling due to null body
