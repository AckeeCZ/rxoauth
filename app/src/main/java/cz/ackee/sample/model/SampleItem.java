package cz.ackee.sample.model;

/**
 * Sample item
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class SampleItem {
    public static final String TAG = SampleItem.class.getName();
    String data;

    public SampleItem(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
