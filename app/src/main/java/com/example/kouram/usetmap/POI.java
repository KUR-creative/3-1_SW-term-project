package com.example.kouram.usetmap;

import com.skp.Tmap.TMapPOIItem;

/**
 * Created by Ko U Ram on 2017-05-24.
 */

public class POI {
    TMapPOIItem item;

    public POI(TMapPOIItem item){
        this.item = item;
    }

    @Override
    public String toString() {
        return item.getPOIName();
    }
}

