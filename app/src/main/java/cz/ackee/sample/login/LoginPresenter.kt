package cz.ackee.sample.login

import cz.ackee.sample.App
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Presenter for login screen
 */
class LoginPresenter {

    private val apiInteractor = App.diContainer.apiInteractor
    private var view: ILoginView? = null

    fun login(name: String, password: String) {
        this.apiInteractor.login(name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onLoggedIn() }, { this.onErrorHappened(it) })
    }

    fun onViewAttached(view: ILoginView) {
        this.view = view
    }

    fun onViewDetached() {
        this.view = null
    }

    private fun onLoggedIn() {
        view?.openDetail()
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
