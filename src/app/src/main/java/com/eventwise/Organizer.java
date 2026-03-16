package com.eventwise;

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

    /** List of event Ids created by this organizer. */
    private ArrayList<String> createdEventIds = new ArrayList<>();

    public Organizer() {
        super();
    }

    public Organizer(String name, String email, String phone, boolean notificationsEnabled) {
        super(name, email, phone, notificationsEnabled, ProfileType.ORGANIZER);
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
}
