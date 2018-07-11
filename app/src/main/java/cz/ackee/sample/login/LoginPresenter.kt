package cz.ackee.sample.login

import cz.ackee.sample.interactor.ApiInteractorImpl
import cz.ackee.sample.model.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

/**
 * Presenter for login screen
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 */
class LoginPresenter {

    private val apiInteractor: ApiInteractorImpl = ApiInteractorImpl()
    private var view: ILoginView? = null

    fun login(name: String, password: String) {
        this.apiInteractor.login(name, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<LoginResponse> { this.onLoggedIn(it) }, Consumer<Throwable> { this.onErrorHappened(it) })
    }

    fun onViewAttached(view: ILoginView) {
        this.view = view
    }

    fun onViewDetached() {
        this.view = null
    }

    private fun onLoggedIn(loginResponse: LoginResponse) {
        if (view != null) {
            view!!.openDetail()
        }
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }

    companion object {
        val TAG = LoginPresenter::class.java.name
    }
}
