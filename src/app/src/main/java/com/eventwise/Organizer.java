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

    /** List of event IDs created by this organizer. */
    private ArrayList<String> createdEventIds = new ArrayList<>();

    /**
     * Required for Firestore.
     */
    public Organizer() {
        super();
    }

    /**
     * Creates an Organizer.
     *
     * @param profileID organizer profile ID
     * @param name organizer name
     * @param email organizer email
     * @param phone organizer phone
     * @param notificationsEnabled notification preference
     */
    public Organizer(String profileID, String name, String email, String phone, boolean notificationsEnabled) {
        super(profileID, name, email, phone, notificationsEnabled, ProfileType.ORGANIZER);
    }

    /** @return created event ids */
    public ArrayList<String> getCreatedEventIds() {
        return createdEventIds;
    }

    /** @param createdEventIds created event ids */
    public void setCreatedEventIds(ArrayList<String> createdEventIds) {
        this.createdEventIds = createdEventIds;
    }

    /**
     * Adds an event to this organizer's created list if not already present.
     *
     * @param eventId event id
     */
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
