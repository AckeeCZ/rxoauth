package cz.ackee.sample.detail;

import java.util.List;

import cz.ackee.sample.interactor.ApiInteractorImpl;
import cz.ackee.sample.login.ILoginView;
import cz.ackee.sample.model.SampleItem;

/**
 * TODO add class description
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class DetailPresenter {
    public static final String TAG = DetailPresenter.class.getName();
    private final ApiInteractorImpl apiInteractor;
    private IDetailView view;
    private List<SampleItem> items;

    public DetailPresenter() {
        this.apiInteractor = new ApiInteractorImpl();
    }

    public void onViewAttached(IDetailView view) {
        this.view = view;
        if(items != null) {
            view.showData(items);
        }
    }

    public void onViewDetached() {
        this.view = null;
    }

    public void refresh() {
        apiInteractor
                .getData()
                .subscribe(this::onDataLoaded, this::onErrorHappened);
    }

    private void onErrorHappened(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void onDataLoaded(List<SampleItem> sampleItems) {
        this.items = sampleItems;
        if (view != null) {
            view.showData(sampleItems);
        }
    }
}
