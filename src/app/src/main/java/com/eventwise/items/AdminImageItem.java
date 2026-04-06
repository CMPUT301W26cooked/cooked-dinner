package com.eventwise.items;

public class AdminImageItem{

    private final String eventId;

    private final String filepath;

    private final String imageUrl;

    private final String imageTitle;

    private final String imgUploadingOrg;


    public AdminImageItem(String eventId, String filepath, String imageUrl, String imageTitle, String imgUploadingOrg) {
        this.eventId = eventId;
        this.filepath = filepath;
        this.imageUrl = imageUrl;
        this.imageTitle = imageTitle;
        this.imgUploadingOrg = imgUploadingOrg;
    }

    public String getEventId() {
        return eventId;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public String getImgUploadingOrg() {
        return imgUploadingOrg;
    }

}