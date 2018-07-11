package cz.ackee.sample.detail

import cz.ackee.sample.model.SampleItem

/**
 * View of detail screen
 */
interface IDetailView {

    fun showData(items: List<SampleItem>)
}
