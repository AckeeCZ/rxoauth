package cz.ackee.sample.model.rest

import cz.ackee.sample.model.SampleItem
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Api description simulating Retrofit description
 */
interface ApiDescription {

    @GET("items")
    fun getData(): Single<List<SampleItem>>
}
