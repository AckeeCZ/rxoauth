/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.ackee.rxoauth.adapter

import cz.ackee.rxoauth.*
import io.reactivex.*
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * A copy of Retrofit Adapter RxJava2 Adapter that is simplified and modified for Oauth purposes.
 * It wraps every call (except the ones with [IgnoreAuth] annotation) with check for expired access
 * token and if that happens, it will perform request for refreshing access token. It guarantees
 * that if simultaneous requests happen only one refresh token request is performed and original
 * requests are retried. The refresh of token is performed via [authService] parameter.
 *
 * When refresh of token fails it will notify [oauthEventListener] and app should handle that
 * (eg. logout user)
 *
 * Its possible to supply [errorChecker] that accepts [HttpException] and decides if this error represents
 * expired access token or expired refresh token.
 *
 * This is a copy of Retrofit documentation:
 * A [call adapter][CallAdapter.Factory] which uses RxJava 2 for creating observables.
 *
 *
 * Adding this class to [Retrofit] allows you to return an [Observable],
 * [Flowable], [Single], [Completable] or [Maybe] from service methods.
 * <pre>`
 * interface MyService {
 * &#64;GET("user/me")
 * Observable<User> getUser()
 * }
`</pre> *
 * There are three configurations supported for the `Observable`, `Flowable`,
 * `Single`, [Completable] and `Maybe` type parameter:
 *
 *  * Direct body (e.g., `Observable<User>`) calls `onNext` with the deserialized body
 * for 2XX responses and calls `onError` with [HttpException] for non-2XX responses and
 * [IOException] for network errors.
 *  * Response wrapped body (e.g., `Observable<Response<User>>`) calls `onNext`
 * with a [Response] object for all HTTP responses and calls `onError` with
 * [IOException] for network errors
 *  * Result wrapped body (e.g., `Observable<Result<User>>`) calls `onNext` with a
 * [Result] object for all HTTP responses and errors.
 *
 */
class RxOauthCallAdapterFactory(private val oAuthStore: OAuthStore,
                                private val authService: RefreshTokenService,
                                private val oauthEventListener: RefreshTokenFailedListener,
                                private val errorChecker: ErrorChecker
) : CallAdapter.Factory() {

    companion object {

        fun create(oAuthStore: OAuthStore, authService: RefreshTokenService, logoutEvent: RefreshTokenFailedListener, errorChecker: ErrorChecker = DefaultErrorChecker()): RxOauthCallAdapterFactory {
            return RxOauthCallAdapterFactory(oAuthStore, authService, logoutEvent, errorChecker)
        }
    }

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val rawType = CallAdapter.Factory.getRawType(returnType)
        val ignoreAuth = annotations.any { it is IgnoreAuth }
        if (rawType == Completable::class.java) {
            // Completable is not parameterized (which is what the rest of this method deals with) so it
            // can only be created with a single configuration.
            return RxOauthCallAdapter<Void>(Void::class.java,
                    isBody = true,
                    isFlowable = false,
                    isCompletable = true,
                    isSingle = false,
                    isMaybe = false,
                    ignoreAuth = ignoreAuth,
                    authService = authService,
                    oAuthStore = oAuthStore,
                    eventListener = oauthEventListener,
                    errorChecker = errorChecker
            )
        }

        val isFlowable = rawType == Flowable::class.java
        val isSingle = rawType == Single::class.java
        val isMaybe = rawType == Maybe::class.java
        if (rawType != Observable::class.java && !isFlowable && !isSingle && !isMaybe) {
            return null
        }
        var isBody = false
        val responseType: Type
        if (returnType !is ParameterizedType) {
            val name = when {
                isFlowable -> "Flowable"
                isSingle -> "Single"
                isMaybe -> "Maybe"
                else -> "Observable"
            }
            throw IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>")
        }

        val observableType = CallAdapter.Factory.getParameterUpperBound(0, returnType)
        val rawObservableType = CallAdapter.Factory.getRawType(observableType)
        if (rawObservableType == Response::class.java) {
            if (observableType !is ParameterizedType) {
                throw IllegalStateException("Response must be parameterized as Response<Foo> or Response<? extends Foo>")
            }
            responseType = CallAdapter.Factory.getParameterUpperBound(0, observableType)
        } else {
            responseType = observableType
            isBody = true
        }

        return RxOauthCallAdapter<Any>(responseType,
                isBody = isBody,
                isFlowable = isFlowable,
                isSingle = isSingle,
                isMaybe = isMaybe,
                isCompletable = false,
                ignoreAuth = ignoreAuth,
                authService = authService,
                oAuthStore = oAuthStore,
                eventListener = oauthEventListener,
                errorChecker = errorChecker
        )
    }
}
