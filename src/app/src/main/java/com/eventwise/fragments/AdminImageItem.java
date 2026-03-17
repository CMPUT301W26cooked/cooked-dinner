package com.eventwise.fragments;

import java.io.File;

public class AdminImageItem{

    private final String eventId;

    private final String filepath;

    private final File imageFile;

    private final String imageTitle;

    private final String imgUploadingOrg;

    private final String timestampString;


    public AdminImageItem(String eventId, String filepath, File imageFile, String imageTitle, String imgUploadingOrg, String timestampString) {
        this.eventId = eventId;
        this.filepath = filepath;
        this.imageFile = imageFile;
        this.imageTitle = imageTitle;
        this.imgUploadingOrg = imgUploadingOrg;
        this.timestampString = timestampString;

    }

    public String getEventId() {
        return eventId;
    }

    public String getFilepath() {
        return filepath;
    }

    public File getImageFile() {
        return imageFile;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public String getImgUploadingOrg() {
        return imgUploadingOrg;
    }

    public String getTimestampString() {
        return timestampString;
    }
}