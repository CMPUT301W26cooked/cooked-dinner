package com.eventwise.items;

import android.graphics.Bitmap;

/**
 * Reference Link: https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
 */
public class ImageListItem {

    private Bitmap image;
    private String storagePath;
    private String eventId;

    /**
     * Returns the Bitmap image associated with this ImageListItem.
     *
     * @return The Bitmap image.
     */
    public Bitmap getImage(){
        return image;
    }

    /**
     * Sets the Bitmap image for this ImageListItem.
     *
     * @param bitmapImage The Bitmap image to set.
     */
    public void setImage(Bitmap bitmapImage){
        this.image = bitmapImage;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
