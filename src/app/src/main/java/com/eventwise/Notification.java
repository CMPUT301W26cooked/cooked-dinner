package com.eventwise;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a notification within the EventWise system.
 * This class stores details about messages sent to various participants (Entrants, Organizers, or Admins)
 * regarding event updates, invitation statuses, and waiting list changes.
 *
 * <p>It supports Firebase synchronization by maintaining redundant fields for message bodies
 * and providing both enum and string representations for types and roles.</p>
 *
 */
public class Notification {

    // TODO:
    // - Add other enum types
    public enum NotificationType {
        WAITING_LIST,
        INVITED,
        CANCELLED,
        OTHER,
        CHOSEN,
        NOT_CHOSEN
    }

    private String notificationId;
    private ArrayList<String> entrantIds;
    private String organizerId;
    private String eventId;
    private String type;

    // Keep both for compatibility with old and incoming code/Firebase docs
    private String message;
    private String messageBody;
    private String messageTitle;

    private Long timestamp;

    private String recipientRole;

    public Notification(ArrayList<String> entrantIds, String organizerId, String eventId,
                        NotificationType type, String message) {
        this.notificationId = UUID.randomUUID().toString();
        this.entrantIds = entrantIds;
        this.organizerId = organizerId;
        this.eventId = eventId;
        this.type = type.name();
        this.message = message;
        this.messageBody = message;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public Notification() {
        this.notificationId = UUID.randomUUID().toString();
    }
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public ArrayList<String> getEntrantIds() {
        return entrantIds;
    }

    public void setEntrantIds(ArrayList<String> entrantIds) {
        this.entrantIds = entrantIds;
    }

    // Keep typo-version too because other code may already call it
    public ArrayList<String> getEntrantsIds() {
        return entrantIds;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public NotificationType getType() {
        return NotificationType.valueOf(type);
    }

    public void setType(NotificationType type) {
        this.type = type.name();
    }

    public String getTypeString() {
        return type;
    }

    public void setTypeString(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message != null ? message : messageBody;
    }

    public String setMessage(String message) {
        this.message = message;
        this.messageBody = message;
        return this.message;
    }

    public String getMessageBody() {
        return messageBody != null ? messageBody : message;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
        this.message = messageBody;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public RecipientRole getRecipientRole() {
        if (recipientRole == null) {
            return null;
        }

        try {
            return RecipientRole.valueOf(recipientRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setRecipientRole(RecipientRole recipientRole) {
        this.recipientRole = recipientRole != null ? recipientRole.name() : null;
    }

    public String getRecipientRoleString() {
        return recipientRole;
    }

    public void setRecipientRoleString(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationId, that.notificationId)
                && Objects.equals(entrantIds, that.entrantIds)
                && Objects.equals(organizerId, that.organizerId)
                && Objects.equals(eventId, that.eventId)
                && Objects.equals(type, that.type)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(getMessage(), that.getMessage())
                && Objects.equals(messageTitle, that.messageTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                notificationId,
                entrantIds,
                organizerId,
                eventId,
                type,
                timestamp,
                getMessage(),
                messageTitle
        );
    }
    public enum RecipientRole {
        ENTRANT,
        ORGANIZER,
        ADMIN
    }
}
