/*
 * Copyright (C) 2016 Jake Wharton
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

import cz.ackee.rxoauth.RxOAuthManager
import cz.ackee.rxoauth.wrapWithOAuthHandlingObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class RxOauthCallAdapter<R>(private val responseType: Type,
                                     private val isBody: Boolean,
                                     private val isFlowable: Boolean,
                                     private val isSingle: Boolean,
                                     private val isMaybe: Boolean,
                                     private val isCompletable: Boolean,
                                     private val ignoreAuth: Boolean,
                                     private val rxOAuthManager: RxOAuthManager) : CallAdapter<R, Any> {

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<R>): Any {
        val responseObservable = CallExecuteObservable(call)

        var observable: Observable<*>
        observable = if (isBody) {
            BodyObservable(responseObservable, isCompletable)
        } else {
            responseObservable
        }

        if (!ignoreAuth) {
            observable = observable.wrapWithOAuthHandlingObservable(rxOAuthManager)
        }

        if (isFlowable) {
            return observable.toFlowable(BackpressureStrategy.LATEST)
        }
        if (isSingle) {
            return observable.singleOrError()
        }
        if (isMaybe) {
            return observable.singleElement()
        }
        return if (isCompletable) {
            observable.ignoreElements()
        } else {
            RxJavaPlugins.onAssembly(observable)
        }
    }
}
