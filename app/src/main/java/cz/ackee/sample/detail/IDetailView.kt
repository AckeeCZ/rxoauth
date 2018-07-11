package cz.ackee.sample.detail

import cz.ackee.sample.model.SampleItem

/**
 * View of detail screen
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 */
interface IDetailView {

    fun showData(items: List<SampleItem>)
}
