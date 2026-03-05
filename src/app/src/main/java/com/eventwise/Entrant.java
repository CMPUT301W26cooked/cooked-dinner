package com.eventwise;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User type Entrant
 *
 * Entrants inherit from Profile.
 * Entrants store a unique deviceId for all identification and track event history/ outcomes for that user.
 */
public class Entrant extends Profile {

    /**
     * TODO (Entrant.java)
     * - Confirm how we're getting deviceId (Android ID?, Firebase ID?, etc...)
     * - Add DatabaseManager methods to update entrant history async and atomicly
     * - Add unit tests!!!!
     * - Figure out the deal with empty constructor for Firestore
     * - Remove specific event getters if redundant with getEventIdsForOutcome
     * - Probably move waitlisted ids to just history for tracking and delete this. (CRC card change)
     */

    /** Unique identifier used for all entrant identification (US 01.07.01). */
    private String deviceId;

    /** List of event IDs for waiting lists entrant is currently in */
    private ArrayList<String> joinedWaitingListEventIds = new ArrayList<>();

    /** List of all historical and current events */
    private ArrayList<EventHistoryEntry> eventHistory = new ArrayList<>();

    /**
     * Required for Firestore (not sure yet).
     */
    public Entrant() {
        super();
    }

    /**
     * Makes an Entrant. The entrant identity is the deviceId, and profileID is set to deviceId.
     *
     * @param deviceId device identifier
     * @param name entrant name
     * @param email entrant email
     * @param phone optional phone (optional)
     * @param notificationsEnabled notifications preference
     */
    public Entrant(String deviceId, String name, String email, String phone, boolean notificationsEnabled) {
        super(deviceId, name, email, phone, notificationsEnabled, ProfileType.ENTRANT);
        this.deviceId = deviceId;
    }

    /** @return device id */
    public String getDeviceId() { return deviceId; }

    /** @param deviceId device id */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return list of waiting list event ids */
    public ArrayList<String> getJoinedWaitingListEventIds() { return joinedWaitingListEventIds; }

    /** @param joinedWaitingListEventIds list of waiting list event ids */
    public void setJoinedWaitingListEventIds(ArrayList<String> joinedWaitingListEventIds) {
        this.joinedWaitingListEventIds = joinedWaitingListEventIds;
    }

    /** @return event history entries */
    public ArrayList<EventHistoryEntry> getEventHistory() { return eventHistory; }

    /** @param eventHistory event history entries */
    public void setEventHistory(ArrayList<EventHistoryEntry> eventHistory) { this.eventHistory = eventHistory; }

    /**
     * Adds an event to history with its info or updates a current history with current status
     *
     * @param eventId event ID
     * @param outcome outcome that occurred
     */
    public void addHistory(String eventId, EventOutcome outcome) {
        if (eventHistory == null) {
            eventHistory = new ArrayList<>();
        }

        long nowEpochSec = System.currentTimeMillis() / 1000L;

        // Update existing entry if present since were doing 1 event id in history
        for (EventHistoryEntry entry : eventHistory) {
            if (entry != null && eventId != null && eventId.equals(entry.getEventId())) {
                entry.setOutcome(outcome);
                entry.setTimestampEpochSec(nowEpochSec);
                return;
            }
        }

        // Otherwise add new entry
        eventHistory.add(new EventHistoryEntry(eventId, outcome, nowEpochSec));
    }

    /**
     * Returns all event IDs where the entrant is currently waitlisted (from history).
     *
     * @return set of event IDs
     */
    public Set<String> getWaitlistedEventIds() {
        return getEventIdsForOutcome(EventOutcome.JOINED_WAITLIST);
    }

    /**
     * Returns all event IDs where the entrant has been invited (from history).
     *
     * @return set of event IDs
     */
    public Set<String> getInvitedEventIds() {
        return getEventIdsForOutcome(EventOutcome.INVITED);
    }

    /**
     * Returns all event IDs where the entrant has accepted (from history).
     *
     * @return set of event IDs
     */
    public Set<String> getAcceptedEventIds() {
        return getEventIdsForOutcome(EventOutcome.ACCEPTED);
    }

    /**
     * Returns all event IDs where the entrant has been or has cancelled (from history).
     *
     * @return set of event IDs
     */
    public Set<String> getCancelledEventIds() {
        return getEventIdsForOutcome(EventOutcome.CANCELLED);
    }

    /**
     * Gets event IDs matching a specific outcome.
     *
     * @param outcome outcome to filter by
     * @return set of event IDs
     */
    private Set<String> getEventIdsForOutcome(EventOutcome outcome) {
        Set<String> result = new HashSet<>();
        if (eventHistory == null || outcome == null) return result;

        for (EventHistoryEntry entry : eventHistory) {
            if (entry == null) continue;
            if (entry.getEventId() == null) continue;
            if (entry.getOutcome() == outcome) {
                result.add(entry.getEventId());
            }
        }
        return result;
    }

    /**
     * Represents one event for an entrant
     */
    public static class EventHistoryEntry {

        /** Event ID associated with this event */
        private String eventId;

        /** Outcome for the event. */
        private EventOutcome outcome;

        /** Timestamp in epoch seconds when the outcome was recorded. */
        private long timestampEpochSec;

        /**
         * Required for Firestore (check in).
         */
        public EventHistoryEntry() {}

        /**
         * Constructs an event history entry.
         *
         * @param eventId event ID
         * @param outcome outcome
         * @param timestampEpochSec epoch seconds timestamp
         */
        public EventHistoryEntry(String eventId, EventOutcome outcome, long timestampEpochSec) {
            this.eventId = eventId;
            this.outcome = outcome;
            this.timestampEpochSec = timestampEpochSec;
        }

        /** @return event id */
        public String getEventId() { return eventId; }

        /** @param eventId event id */
        public void setEventId(String eventId) { this.eventId = eventId; }

        /** @return outcome */
        public EventOutcome getOutcome() { return outcome; }

        /** @param outcome outcome */
        public void setOutcome(EventOutcome outcome) { this.outcome = outcome; }

        /** @return timestamp in epoch seconds */
        public long getTimestampEpochSec() { return timestampEpochSec; }

        /** @param timestampEpochSec timestamp in epoch seconds */
        public void setTimestampEpochSec(long timestampEpochSec) { this.timestampEpochSec = timestampEpochSec; }
    }

    /**
     * Possible outcomes an entrant can experience with an event.
     */
    public enum EventOutcome {
        /** Entrant joined the waiting list. */
        JOINED_WAITLIST,
        /** Entrant left the waiting list. */
        LEFT_WAITLIST,
        /** Entrant was invited/selected. */
        INVITED,
        /** Entrant accepted invitation and confirmed enrollment. */
        ACCEPTED,
        /** Entrant declined invitation. */
        DECLINED,
        /** Entrant was cancelled or otherwise removed. */
        CANCELLED,
        /** Entrant lost the lottery selection. */
        LOST_LOTTERY
    }
}
