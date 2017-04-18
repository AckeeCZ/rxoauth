package cz.ackee.sample.login;

import cz.ackee.sample.interactor.ApiInteractorImpl;
import cz.ackee.sample.model.LoginResponse;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Presenter for login screen
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class LoginPresenter {
    public static final String TAG = LoginPresenter.class.getName();
    private final ApiInteractorImpl apiInteractor;
    private ILoginView view;

    public LoginPresenter() {
        this.apiInteractor = new ApiInteractorImpl();
    }

    public void login(String name, String password) {
        this.apiInteractor.login(name, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoggedIn, this::onErrorHappened);
    }

    public void onViewAttached(ILoginView view) {
        this.view = view;
    }

    public void onViewDetached() {
        this.view = null;
    }

    private void onLoggedIn(LoginResponse loginResponse) {
        if(view != null) {
            view.openDetail();
        }
    }

    private void onErrorHappened(Throwable throwable) {
        throwable.printStackTrace();
    }

}
