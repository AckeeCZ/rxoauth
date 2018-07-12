package cz.ackee.sample.detail

import android.app.ListActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import cz.ackee.sample.model.SampleItem

/**
 * Activity with some detail
 */
class DetailActivity : ListActivity(), IDetailView {

    private var presenter: DetailPresenter? = null

    override fun showData(items: List<SampleItem>) {
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = DetailPresenter()
        presenter!!.refresh()
    }

    override fun onResume() {
        super.onResume()
        presenter!!.onViewAttached(this)
    }

    override fun onPause() {
        super.onPause()
        presenter!!.onViewDetached()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Refresh")
                .setOnMenuItemClickListener {
                    presenter!!.refresh()
                    false
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Invalidate access token")
                .setOnMenuItemClickListener {
                    presenter!!.invalidateAccessToken()
                    false
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Invalidate refresh token")
                .setOnMenuItemClickListener {
                    presenter!!.invalidateRefreshToken()
                    false
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        menu.add("Logout")
                .setOnMenuItemClickListener {
                    presenter!!.logout()
                    false
                }
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }
}


