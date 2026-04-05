package com.eventwise;

/**
 * Represents a comment within the EventWise system.
 * This class stores the content of the comment, the identity of the author,
 * the time of creation, and the type of profile associated with the author.
 */
public class Comment {
    public String text;
    public String authorId;
    public long timestamp;


    public ProfileType profileType;


    /**
     * Constructs a new Comment with the specified text, author, timestamp, and profile type.
     *
     * @param text the content of the comment
     * @param authorId the unique identifier of the user who created the comment
     * @param timestamp the time at which the comment was posted
     * @param profileType the type of profile (e.g., Entrant, Organizer) associated with the author
     */
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
