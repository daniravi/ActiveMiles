package org.imperial.activemilespro.gui;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

class ItemPhotoForMap implements ClusterItem {
    public final String title;
    public final String photoUrl;
    private final LatLng mPosition;

    public ItemPhotoForMap(LatLng position, String title, String pictureResource) {
        this.title = title;
        photoUrl = pictureResource;
        mPosition = position;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
