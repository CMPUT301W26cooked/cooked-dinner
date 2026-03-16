package com.eventwise;

import android.util.Log;

import com.eventwise.database.SessionStore;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entrant type for user who enrolls and participates in events.
 *
 * @author Becca Irving
 * @since 2026-03-04
 * Updated By Becca Irving on 2026-03-09
 */
public class Entrant extends Profile {

    /**
     * TODO (Entrant.java)
     * - Confirm how we're getting deviceId (Android ID?, Firebase ID?, etc...)
     * - Add DatabaseManager methods to update entrant event states async and atomically.
     * - Add unit tests!!!!
     * - Figure out the deal with empty constructor for Firestore.
     * - Remove specific event getters later if redundant with getEventIdsForStatus.
     */

    /** Unique identifier used for all entrant identification (US 01.07.01). */
    private String deviceId;

    /** List of all historical and current event states. */
    private ArrayList<EventStateEntry> eventStates = new ArrayList<>();

    /** Notification IDs associated with this entrant. */
    private ArrayList<String> notificationIDs = new ArrayList<>();

    /**
     * Required for Firestore.
     */
    public Entrant() {
        super();
    }

    /**
     * Makes an Entrant. The entrant identity is the deviceId, and profileId is set to deviceId.
     *
     * @param name entrant name
     * @param email entrant email
     * @param phone optional phone
     * @param notificationsEnabled notifications preference
     */
    public Entrant(String name, String email, String phone, boolean notificationsEnabled, android.content.Context context) {
        super(name, email, phone, notificationsEnabled, ProfileType.ENTRANT);

        SessionStore session = new SessionStore(context);
        this.deviceId = session.getOrCreateDeviceId();
        setProfileId(this.deviceId);
        Log.d("Entrant", "DeviceId/ProfileId: " + this.deviceId);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ArrayList<EventStateEntry> getEventStates() {
        return eventStates;
    }

    public void setEventStates(ArrayList<EventStateEntry> eventStates) {
        this.eventStates = eventStates;
    }

    public ArrayList<String> getNotificationIDs() {
        return notificationIDs;
    }

    public void setNotificationIDs(ArrayList<String> notificationIDs) {
        this.notificationIDs = notificationIDs;
    }

    @Exclude
    public boolean hasCompletedProfile() {
        return getName() != null && !getName().trim().isEmpty()
                && getEmail() != null && !getEmail().trim().isEmpty();
    }

    /**
     * Adds a new event state or updates an existing one.
     *
     * One event should only appear once in this list.
     *
     * @param eventId event ID
     * @param status entrant state for that event
     */
    public void addOrUpdateEventState(String eventId, EventEntrantStatus status, long timestamp) {
        if (eventStates == null) {
            eventStates = new ArrayList<>();
        }

        for (EventStateEntry entry : eventStates) {
            if (entry != null && eventId != null && eventId.equals(entry.getEventId())) {
                entry.setStatus(status);
                entry.setTimestampEpochSec(timestamp);
                return;
            }
        }

        eventStates.add(new EventStateEntry(eventId, status, timestamp));
    }

    /**
     * Gets event IDs matching a specific status.
     *
     * @param status status to filter by
     * @return set of event IDs
     */
    @Exclude
    public Set<String> getEventIdsForStatus(EventEntrantStatus status) {
        Set<String> result = new HashSet<>();
        if (eventStates == null || status == null) {
            return result;
        }

        for (EventStateEntry entry : eventStates) {
            if (entry == null || entry.getEventId() == null) {
                continue;
            }
            if (entry.getStatus() == status) {
                result.add(entry.getEventId());
            }
        }
        return result;
    }

    /**
     * Returns all waitlisted event IDs.
     *
     * @return set of event IDs
     */
    @Exclude
    public Set<String> getWaitlistedEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.WAITLISTED);
    }

    /**
     * Returns all invited event IDs.
     *
     * @return set of event IDs
     */
    @Exclude
    public Set<String> getInvitedEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.INVITED);
    }

    /**
     * Returns all enrolled event IDs.
     *
     * @return set of event IDs
     */
    @Exclude
    public Set<String> getEnrolledEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.ENROLLED);
    }

    /**
     * Returns all cancelled event IDs.
     *
     * @return set of event IDs
     */
    @Exclude
    public Set<String> getCancelledEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.CANCELLED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;
        Entrant that = (Entrant) o;
        return Objects.equals(deviceId, that.deviceId)
                && Objects.equals(eventStates, that.eventStates)
                && Objects.equals(notificationIDs, that.notificationIDs)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, eventStates, notificationIDs, super.hashCode());
    }



    /**
     * Represents one event state for an entrant.
     */
    public static class EventStateEntry {

        /** Event ID associated with this state. */
        private String eventId;

        /** Status for the entrant in this event. */
        private EventEntrantStatus status;

        /** Timestamp in epoch seconds when the state was recorded. */
        private long timestampEpochSec;

        /**
         * Required for Firestore.
         */
        public EventStateEntry() {}

        /**
         * Constructs an event state entry.
         *
         * @param eventId event ID
         * @param status status
         * @param timestampEpochSec timestamp
         */
        public EventStateEntry(String eventId, EventEntrantStatus status, long timestampEpochSec) {
            this.eventId = eventId;
            this.status = status;
            this.timestampEpochSec = timestampEpochSec;
        }

        /** @return event id */
        public String getEventId() { return eventId; }

        /** @param eventId event id */
        public void setEventId(String eventId) { this.eventId = eventId; }

        /** @return status */
        public EventEntrantStatus getStatus() { return status; }

        /** @param status status */
        public void setStatus(EventEntrantStatus status) { this.status = status; }

        /** @return timestamp */
        public long getTimestampEpochSec() { return timestampEpochSec; }

        /** @param timestampEpochSec timestamp */
        public void setTimestampEpochSec(long timestampEpochSec) { this.timestampEpochSec = timestampEpochSec; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entrant.EventStateEntry)) return false;
            EventStateEntry that = (EventStateEntry) o;
            return Objects.equals(eventId, that.eventId)
                    && Objects.equals(status, that.status);
                    //Do not check for timestamp
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, status, timestampEpochSec);
        }

    }
}
