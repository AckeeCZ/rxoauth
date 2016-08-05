package cz.ackee.sample.detail;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.List;

import cz.ackee.sample.model.SampleItem;

/**
 * Activity with some detail
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class DetailActivity extends ListActivity implements IDetailView {
    public static final String TAG = DetailActivity.class.getName();
    private DetailPresenter presenter;

    @Override
    public void showData(List<SampleItem> items) {
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new DetailPresenter();
        presenter.refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onViewAttached(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onViewDetached();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Refresh")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        presenter.refresh();
                        return false;
                    }
                });
        return super.onCreateOptionsMenu(menu);

    }
}


