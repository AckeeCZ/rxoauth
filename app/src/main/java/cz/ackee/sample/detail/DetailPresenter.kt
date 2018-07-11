package cz.ackee.sample.detail

import cz.ackee.sample.interactor.ApiInteractorImpl
import cz.ackee.sample.model.SampleItem
import io.reactivex.functions.Consumer

/**
 * Presenter for detail screen
 */
class DetailPresenter {

    private val apiInteractor: ApiInteractorImpl = ApiInteractorImpl()
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
        apiInteractor
                .data
                .subscribe(Consumer<List<SampleItem>> { this.onDataLoaded(it) }, Consumer<Throwable> { this.onErrorHappened(it) })
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun onDataLoaded(sampleItems: List<SampleItem>) {
        this.items = sampleItems
        if (view != null) {
            view!!.showData(sampleItems)
        }
    }
}
