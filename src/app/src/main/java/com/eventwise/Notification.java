package com.eventwise;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class Notification {


    //TODO:
    // - Add other enum types
    public enum NotificationType {
        WAITING_LIST,
        INVITED,
        CANCELLED,
        OTHER
    }



    String notificationID;
    //List of entrants that were notified
    ArrayList<String> entrantIDs;
    //Sent from
    String OrganizerID;
    //Relating to which event
    String EventID;

    String Type;

    //Any message to be sent with the Notification
    String message;

    public Long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.Timestamp = timestamp;
    }

    Long Timestamp;

    Notification(ArrayList<String> entrantIDs, String organizerID, String eventID, NotificationType type, String message){
        this.notificationID = UUID.randomUUID().toString();
        this.entrantIDs = entrantIDs;
        this.OrganizerID = organizerID;
        this.EventID = eventID;
        this.Type = type.name();
        this.message = message;
        this.Timestamp = System.currentTimeMillis()/1000;
    }

    Notification(){};


    public String getNotificationID() {
        return notificationID;
    }

    public ArrayList<String> getEntrantIDs() {
        return entrantIDs;
    }


    public String getOrganizerID() {
        return OrganizerID;
    }

    public void setOrganizerID(String organizerID) {
        OrganizerID = organizerID;
    }

    public void setEventID(String eventID){
        EventID = eventID;
    }

    public String getEventID() {
        return EventID;
    }

    public ArrayList<String> getEntrantsIDs() {
        return entrantIDs;
    }

    public String getMessage() {
        return message;
    }

    public String setMessage(String message) {
        return this.message = message;
    }


    // Getter/setter convert between String and Enum
    public NotificationType getType() {
        return NotificationType.valueOf(Type);
    }

    public void setType(NotificationType type) {
        this.Type = type.name(); // stores "CHOSEN", "CANCELLED" etc.
    }

    //Used to check if notifications are equal, notably for Firebase
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationID, that.notificationID)
                && Objects.equals(OrganizerID, that.OrganizerID)
                && Objects.equals(entrantIDs, that.entrantIDs)
                && Objects.equals(EventID, that.EventID)
                && Objects.equals(Type, that.Type)
                && Objects.equals(Timestamp, that.Timestamp)
                && Objects.equals(message, that.message);
    }





}
