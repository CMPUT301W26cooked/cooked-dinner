package com.eventwise;

import android.util.Log;

import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.database.SessionStore;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
     * - Confirm how we're getting deviceId (Android Id?, Firebase Id?, etc...)
     * - Add DatabaseManager methods to update entrant event states async and atomically.
     * - Add unit tests!!!!
     * - Figure out the deal with empty constructor for Firestore.
     * - Remove specific event getters later if redundant with getEventIdsForStatus.
     */

    /** Unique identifier used for all entrant identification (US 01.07.01). */
    private String deviceId;

    /** List of all historical and current event states. */
    private ArrayList<EventStateEntry> eventStates = new ArrayList<>();

    /** Notification Ids associated with this entrant. */
    private ArrayList<String> notificationIds = new ArrayList<>();

    /** List of all the user's interests. Null means no preferences**/
    private ArrayList<String> interestsTags;


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
        setProfileId(this.deviceId + "_entrant");
        interestsTags = null;
        Log.d("Entrant", "DeviceId/ProfileId: " + this.deviceId);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public ArrayList<String> getInterestsTags() {
        return interestsTags;
    }

    public void setInterestsTags(ArrayList<String> interestsTags) {
        this.interestsTags = interestsTags;
    }


    public ArrayList<EventStateEntry> getEventStates() {
        return eventStates;
    }

    public void setEventStates(ArrayList<EventStateEntry> eventStates) {
        this.eventStates = eventStates;
    }

    public ArrayList<String> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(ArrayList<String> notificationIds) {
        this.notificationIds = notificationIds;
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
     * @param eventId event Id
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
     * Removes one event state from this entrant entirely.
     *
     * @param eventId event id
     */
    public void removeEventState(String eventId) {
        if (eventStates == null || eventId == null) {
            return;
        }

        for (int i = eventStates.size() - 1; i >= 0; i--) {
            EventStateEntry entry = eventStates.get(i);
            if (entry != null
                    && entry.getEventId() != null
                    && eventId.equals(entry.getEventId())) {
                eventStates.remove(i);
            }
        }
    }

    /**
     * Gets event Ids matching a specific status.
     *
     * @param status status to filter by
     * @return set of event Ids
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
     * Returns all waitlisted event Ids.
     *
     * @return set of event Ids
     */
    @Exclude
    public Set<String> getWaitlistedEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.WAITLISTED);
    }

    /**
     * Returns all invited event Ids.
     *
     * @return set of event Ids
     */
    @Exclude
    public Set<String> getInvitedEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.INVITED);
    }

    /**
     * Returns all enrolled event Ids.
     *
     * @return set of event Ids
     */
    @Exclude
    public Set<String> getEnrolledEventIds() {
        return getEventIdsForStatus(EventEntrantStatus.ENROLLED);
    }

    /**
     * Returns all cancelled event Ids.
     *
     * @return set of event Ids
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
                && Objects.equals(notificationIds, that.notificationIds)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, eventStates, notificationIds, super.hashCode());
    }



    /**
     * Represents one event state for an entrant.
     */
    public static class EventStateEntry {

        /** Event Id associated with this state. */
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
         * @param eventId event Id
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
