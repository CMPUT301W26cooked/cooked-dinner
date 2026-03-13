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



    String notificationId;
    //List of entrants that were notified
    ArrayList<String> entrantIds;
    //Sent from
    String OrganizerId;
    //Relating to which event
    String EventId;

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



    Notification(ArrayList<String> entrantIds, String organizerId, String eventId, NotificationType type, String message){
        this.notificationId = UUID.randomUUID().toString();
        this.entrantIds = entrantIds;
        this.OrganizerId = organizerId;
        this.EventId = eventId;
        this.Type = type.name();
        this.message = message;
        this.Timestamp = System.currentTimeMillis()/1000;
    }

    Notification(){};


    public String getNotificationId() {
        return notificationId;
    }

    public ArrayList<String> getEntrantIds() {
        return entrantIds;
    }



    public String getOrganizerId() {
        return OrganizerId;
    }

    public void setOrganizerId(String organizerId) {
        OrganizerId = organizerId;
    }

    public void setEventId(String eventId){
        EventId = eventId;
    }

    public String getEventId() {
        return EventId;
    }

    public ArrayList<String> getEntrantsIds() {
        return entrantIds;
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
        return Objects.equals(notificationId, that.notificationId)
                && Objects.equals(OrganizerId, that.OrganizerId)
                && Objects.equals(entrantIds, that.entrantIds)
                && Objects.equals(EventId, that.EventId)
                && Objects.equals(Type, that.Type)
                && Objects.equals(Timestamp, that.Timestamp)
                && Objects.equals(message, that.message);
    }





}
