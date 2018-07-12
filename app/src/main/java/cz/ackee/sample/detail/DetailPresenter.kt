package cz.ackee.sample.detail

import cz.ackee.sample.App
import cz.ackee.sample.model.SampleItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Presenter for detail screen
 */
class DetailPresenter {

    private val apiInteractor = App.diContainer.apiInteractor
    private var view: IDetailView? = null
    private var items: List<SampleItem>? = null

    fun onViewAttached(view: IDetailView) {
        this.view = view
        if (items != null) {
            view.showData(items!!)
        }
    }

    fun onViewDetached() {
        this.view = null
    }

    fun refresh() {
        apiInteractor.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onDataLoaded(it) }, { this.onErrorHappened(it) })
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun onDataLoaded(sampleItems: List<SampleItem>) {
        this.items = sampleItems
        view?.showData(sampleItems)
    }

    fun invalidateAccessToken() {
        with(App.diContainer.oauthStore) { saveOauthCredentials("bla", refreshToken ?: "") }
    }

    fun invalidateRefreshToken() {
        with(App.diContainer.oauthStore) { saveOauthCredentials(accessToken ?: "", "bla") }
    }

    fun logout() {
        apiInteractor.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { App.diContainer.logouter.logout() }
    }
}
