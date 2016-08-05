package cz.ackee.sample.detail;

import java.util.List;

import cz.ackee.sample.model.SampleItem;

/**
 * View of detail screen
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public interface IDetailView  {
    public void showData(List<SampleItem> items);

}
