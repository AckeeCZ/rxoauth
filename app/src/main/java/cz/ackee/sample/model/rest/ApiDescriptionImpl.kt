package cz.ackee.sample.model.rest

import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import retrofit2.HttpException
import retrofit2.Response
import java.util.Arrays
import java.util.Random

/**
 * Implementation of [ApiDescription]
 */
class ApiDescriptionImpl : ApiDescription {

    override fun login(name: String, passwd: String): Single<LoginResponse> {
        return Single.just(LoginResponse("David", "123", "456"))
    }

    override fun getData(): Single<List<SampleItem>> {
        val first = booleanArrayOf(true)
        return Single.just(Arrays.asList(SampleItem("a"), SampleItem("b"), SampleItem("c")))
                .flatMap { list ->
                    if (first[0] && Random().nextInt() % 2 == 0) {
                        first[0] = false
                        //simulate 401 response
                        Single.error(HttpException(Response.error<List<SampleItem>>(401, object : ResponseBody() {
                            override fun contentType(): MediaType? {
                                return null
                            }

                            override fun contentLength(): Long {
                                return 0
                            }

                            override fun source(): BufferedSource? {
                                return null
                            }
                        })))
                    } else {
                        Single.just(Arrays.asList(SampleItem("a"), SampleItem("b"), SampleItem("c")))
                    }
                }
    }

    override fun refreshAccessToken(refreshToken: String): Single<OauthCredentials> {
        val first = booleanArrayOf(true)
        return Single.just(LoginResponse("David", "123", "456"))
                .flatMap { data ->
                    if (first[0] && Random().nextInt() % 2 == 0) {
                        first[0] = false
                        //simulate 400 response
                        Single.just(LoginResponse("David", "123", "456"))
                                .flatMap {
                                    Single.error<OauthCredentials>(HttpException(Response.error<OauthCredentials>(400, object : ResponseBody() {
                                        override fun contentType(): MediaType? {
                                            return null
                                        }

                                        override fun contentLength(): Long {
                                            return 0
                                        }

                                        override fun source(): BufferedSource? {
                                            return null
                                        }
                                    })))
                                }
                    } else {
                        Single.just(LoginResponse("David", "123", "456"))
                                .map { data }
                    }
                }
    }

    override fun logout(): Completable {
        return Completable.complete()
    }

    override fun something(): Single<String> {
        return Single.just("")
    }
}
