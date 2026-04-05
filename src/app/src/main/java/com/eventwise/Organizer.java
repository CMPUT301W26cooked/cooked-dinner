package com.eventwise;

import android.util.Log;

import com.eventwise.database.SessionStore;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

/**
 * Organizer type that can create events and monitor Entrant engagement.
 *
 * @author Becca Irving
 * @since 2026-03-09
 */
public class Organizer extends Profile {

    /**
     * TODO (Organizer.java)
     * - Connect organizer event creation flow to DatabaseManager.
     * - Add organizer-specific helper methods as organizer flow grows.
     * - Keep lottery/notification/export logic out of the model and in services later.
     * - Add unit tests.
     */
    private String deviceId;

    private ArrayList<String> notificationIds = new ArrayList<>();

    /** List of event Ids created by this organizer. */
    private ArrayList<String> createdEventIds = new ArrayList<>();

    public Organizer() {
        super();
    }

    public Organizer(String name, String email, String phone, boolean notificationsEnabled, android.content.Context context) {
        super(name, email, phone, notificationsEnabled, ProfileType.ORGANIZER);

        SessionStore session = new SessionStore(context);
        this.deviceId = session.getOrCreateDeviceId();
        setProfileId(this.deviceId);
        Log.d("Entrant", "DeviceId/ProfileId: " + this.deviceId);
    }

    public ArrayList<String> getCreatedEventIds() {
        return createdEventIds;
    }

    public void setCreatedEventIds(ArrayList<String> createdEventIds) {
        this.createdEventIds = createdEventIds;
    }

    public void addCreatedEventId(String eventId) {
        if (createdEventIds == null) {
            createdEventIds = new ArrayList<>();
        }

        if (eventId != null && !createdEventIds.contains(eventId)) {
            createdEventIds.add(eventId);
        }
    }

    /**
     * Removes an event from this organizer's created list.
     *
     * @param eventId event id
     */
    public void removeCreatedEventId(String eventId) {
        if (createdEventIds == null || eventId == null) {
            return;
        }
        createdEventIds.remove(eventId);
    }

    /**
     * Checks whether this organizer created a given event.
     *
     * @param eventId event id
     * @return true if organizer created event
     */
    public boolean hasCreatedEvent(String eventId) {
        return createdEventIds != null && eventId != null && createdEventIds.contains(eventId);
    }

    @Exclude
    public boolean hasCompletedProfile() {
        return getName() != null && !getName().trim().isEmpty()
                && getEmail() != null && !getEmail().trim().isEmpty();
    }

    public ArrayList<String> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(ArrayList<String> notificationIds) {
        this.notificationIds = notificationIds;
    }
}
