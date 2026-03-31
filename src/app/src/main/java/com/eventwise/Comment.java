package com.eventwise;

public class Comment {
    public String text;
    public String authorId;
    public long timestamp;


    public ProfileType profileType;


    public Comment(String text, String authorId, long timestamp,  ProfileType profileType) {
        this.text = text;
        this.profileType = profileType;
        this.authorId = authorId;
        this.timestamp = timestamp;
    }
    public Comment(){}

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }



}
