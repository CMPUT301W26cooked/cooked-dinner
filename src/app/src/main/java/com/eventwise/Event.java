package com.eventwise;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

/**
 * Event for Entrants to join, Organizers to create and Admin's to mod.
 *
 * @author Becca Irving
 * @since 2026-03-04
 * Updated By Becca Irving on 2026-03-09
 */
public class Event {

    /**
     * TODO (Event.java)
     * - Event IDs will be Firestore auto generated so add eventId creation inside DatabaseManager when creating events.
     * - Decide if Location should stay a string or become an object/ID.
     * - Add validation rules! (e.g., end after start, registration open before close).
     * - Add DatabaseManager methods to join/leave waiting list.
     * - Add logic to handle cancelled/declined entrants triggering redraws.
     * - Add unit tests!!!
     */

    /** Firestore ID for this event (auto gen by Firestore). */
    private String eventId;

    /** Profile ID of the organizer who created the event. */
    private String organizerProfileId;

    /** Event name. */
    private String name;

    /** Event description. */
    private String description;

    /** Event price. */
    private double price;

    /** Event location/venue. */
    private String locationName;

    /** Event tags. */
    private ArrayList<Tag> tags = new ArrayList<>();

    /**
     * Event start time in epoch seconds.
     * Epoch seconds includes full date (year/month/day) and time (hour/min/sec).
     */
    private long eventStartEpochSec;

    /** Event end time in epoch seconds. */
    private long eventEndEpochSec;

    /**
     * Poster reference aka Storage path.
     * Can be null or empty if no poster.
     */
    private String posterPath;

    /** Registration open time in epoch seconds. */
    private long registrationOpenEpochSec;

    /** Registration close time in epoch seconds. */
    private long registrationCloseEpochSec;

    /** If geolocation is required. */
    private boolean geolocationRequired;

    /**
     * Optional max entrants on waiting list.
     * Null means unlimited.
     */
    private Integer maxWaitingListSize;

    /** Max number of entrants to sample. */
    private int maxWinnersToSample;

    /** Unique QR code identifier linking to this event. */
    private String qrCodeId;

    /** One list of entrants and their current state for this event. */
    private ArrayList<EntrantStatusEntry> entrantStatuses = new ArrayList<>();

    /**
     * Required for Firestore.
     */
    public Event() {}

    /**
     * Constructor for an Event with required fields.
     *
     * @param organizerProfileId organizer profile ID
     * @param name event name
     * @param description event description
     * @param price event price
     * @param locationName location/venue
     * @param tags event tags
     * @param eventStartEpochSec event start time
     * @param eventEndEpochSec event end time
     * @param registrationOpenEpochSec registration open time
     * @param registrationCloseEpochSec registration close time
     * @param geolocationRequired whether geolocation is required
     * @param maxWaitingListSize optional max waiting list size
     * @param maxWinnersToSample max winners to sample
     * @param posterPath optional poster path
     * @param qrCodeId optional QR code id
     */
    public Event(
            String organizerProfileId,
            String name,
            String description,
            double price,
            String locationName,
            ArrayList<Tag> tags,
            long eventStartEpochSec,
            long eventEndEpochSec,
            long registrationOpenEpochSec,
            long registrationCloseEpochSec,
            boolean geolocationRequired,
            Integer maxWaitingListSize,
            int maxWinnersToSample,
            String posterPath,
            String qrCodeId
    ) {
        this.organizerProfileId = organizerProfileId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.locationName = locationName;
        if (tags != null) {
            this.tags = tags;
        }
        this.eventStartEpochSec = eventStartEpochSec;
        this.eventEndEpochSec = eventEndEpochSec;
        this.registrationOpenEpochSec = registrationOpenEpochSec;
        this.registrationCloseEpochSec = registrationCloseEpochSec;
        this.geolocationRequired = geolocationRequired;
        this.maxWaitingListSize = maxWaitingListSize;
        this.maxWinnersToSample = maxWinnersToSample;
        this.posterPath = posterPath;
        this.qrCodeId = qrCodeId;
    }

    /** @return event id */
    public String getEventId() { return eventId; }

    /** @param eventId event id */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return organizer profile id */
    public String getOrganizerProfileId() { return organizerProfileId; }

    /** @param organizerProfileId organizer profile id */
    public void setOrganizerProfileId(String organizerProfileId) { this.organizerProfileId = organizerProfileId; }

    /** @return event name */
    public String getName() { return name; }

    /** @param name event name */
    public void setName(String name) { this.name = name; }

    /** @return description */
    public String getDescription() { return description; }

    /** @param description description */
    public void setDescription(String description) { this.description = description; }

    /** @return price */
    public double getPrice() { return price; }

    /** @param price price */
    public void setPrice(double price) { this.price = price; }

    /** @return location name */
    public String getLocationName() { return locationName; }

    /** @param locationName location name */
    public void setLocationName(String locationName) { this.locationName = locationName; }

    /** @return tags */
    public ArrayList<Tag> getTags() { return tags; }

    /** @param tags tags */
    public void setTags(ArrayList<Tag> tags) { this.tags = tags; }

    /** @return start time in epoch seconds */
    public long getEventStartEpochSec() { return eventStartEpochSec; }

    /** @param eventStartEpochSec start time in epoch seconds */
    public void setEventStartEpochSec(long eventStartEpochSec) { this.eventStartEpochSec = eventStartEpochSec; }

    /** @return end time in epoch seconds */
    public long getEventEndEpochSec() { return eventEndEpochSec; }

    /** @param eventEndEpochSec end time in epoch seconds */
    public void setEventEndEpochSec(long eventEndEpochSec) { this.eventEndEpochSec = eventEndEpochSec; }

    /** @return poster path */
    public String getPosterPath() { return posterPath; }

    /** @param posterPath poster path */
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    /** @return registration open time in epoch seconds */
    public long getRegistrationOpenEpochSec() { return registrationOpenEpochSec; }

    /** @param registrationOpenEpochSec registration open time in epoch seconds */
    public void setRegistrationOpenEpochSec(long registrationOpenEpochSec) { this.registrationOpenEpochSec = registrationOpenEpochSec; }

    /** @return registration close time in epoch seconds */
    public long getRegistrationCloseEpochSec() { return registrationCloseEpochSec; }

    /** @param registrationCloseEpochSec registration close time in epoch seconds */
    public void setRegistrationCloseEpochSec(long registrationCloseEpochSec) { this.registrationCloseEpochSec = registrationCloseEpochSec; }

    /** @return true if geolocation required */
    public boolean isGeolocationRequired() { return geolocationRequired; }

    /** @param geolocationRequired geolocation required */
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    /** @return max waiting list size */
    public Integer getMaxWaitingListSize() { return maxWaitingListSize; }

    /** @param maxWaitingListSize max waiting list size */
    public void setMaxWaitingListSize(Integer maxWaitingListSize) { this.maxWaitingListSize = maxWaitingListSize; }

    /** @return max winners to sample */
    public int getMaxWinnersToSample() { return maxWinnersToSample; }

    /** @param maxWinnersToSample max winners */
    public void setMaxWinnersToSample(int maxWinnersToSample) { this.maxWinnersToSample = maxWinnersToSample; }

    /** @return qr code id */
    public String getQrCodeId() { return qrCodeId; }

    /** @param qrCodeId qr code id */
    public void setQrCodeId(String qrCodeId) { this.qrCodeId = qrCodeId; }

    /** @return entrant statuses */
    public ArrayList<EntrantStatusEntry> getEntrantStatuses() { return entrantStatuses; }

    /** @param entrantStatuses entrant statuses */
    public void setEntrantStatuses(ArrayList<EntrantStatusEntry> entrantStatuses) {
        this.entrantStatuses = entrantStatuses;
    }

    /**
     * Adds a new entrant state or updates the entrant's current state.
     *
     * One entrant should only appear once in this list.
     *
     * @param entrantProfileId entrant profile id
     * @param status entrant status
     */
    public void addOrUpdateEntrantStatus(String entrantProfileId, EventEntrantStatus status, long timestamp) {
        if (entrantStatuses == null) {
            entrantStatuses = new ArrayList<>();
        }

//        long nowEpochSec = System.currentTimeMillis() / 1000L;

        for (EntrantStatusEntry entry : entrantStatuses) {
            if (entry != null
                    && entrantProfileId != null
                    && entrantProfileId.equals(entry.getEntrantProfileId())) {
                entry.setStatus(status);
                entry.setTimestampEpochSec(timestamp);
                return;
            }
        }

        entrantStatuses.add(new EntrantStatusEntry(entrantProfileId, status, timestamp));
    }

    /**
     * Returns all entrant IDs matching a given status.
     *
     * @param status status to filter by
     * @return entrant ids
     */
    public ArrayList<String> getEntrantIdsByStatus(EventEntrantStatus status) {
        ArrayList<String> result = new ArrayList<>();
        if (entrantStatuses == null || status == null) {
            return result;
        }

        for (EntrantStatusEntry entry : entrantStatuses) {
            if (entry == null || entry.getEntrantProfileId() == null) {
                continue;
            }
            if (entry.getStatus() == status) {
                result.add(entry.getEntrantProfileId());
            }
        }

        return result;
    }

    /**
     * Checks whether registration is currently open.
     *
     * @return true if now is between open and close times inclusive
     */
    @Exclude
    public boolean isRegistrationOpenNow() {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= registrationOpenEpochSec && nowEpochSec <= registrationCloseEpochSec;
    }

    /**
     * Returns the current waiting list count.
     *
     * @return waiting list count
     */
    @Exclude
    public int getWaitingListCount() {
        return getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).size();
    }

    /**
     * Returns the current enrolled count.
     *
     * @return enrolled count
     */
    @Exclude
    public int getEnrolledCount() {
        return getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).size();
    }

    /**
     * Checks whether the waiting list is full.
     *
     * @return true if full, false otherwise
     */
    @Exclude
    public boolean isWaitingListFull() {
        if (maxWaitingListSize == null) {
            return false;
        }
        return getWaitingListCount() >= maxWaitingListSize;
    }

    /**
     * Represents one entrant's state for this event.
     */
    public static class EntrantStatusEntry {

        /** Entrant profile ID. */
        private String entrantProfileId;

        /** Current status for the entrant. */
        private EventEntrantStatus status;

        /** Timestamp in epoch seconds when the status was last updated. */
        private long timestampEpochSec;

        /**
         * Required for Firestore.
         */
        public EntrantStatusEntry() {}

        /**
         * Constructs an entrant status entry.
         *
         * @param entrantProfileId entrant profile id
         * @param status entrant status
         * @param timestampEpochSec timestamp
         */
        public EntrantStatusEntry(String entrantProfileId, EventEntrantStatus status, long timestampEpochSec) {
            this.entrantProfileId = entrantProfileId;
            this.status = status;
            this.timestampEpochSec = timestampEpochSec;
        }

        /** @return entrant profile id */
        public String getEntrantProfileId() { return entrantProfileId; }

        /** @param entrantProfileId entrant profile id */
        public void setEntrantProfileId(String entrantProfileId) { this.entrantProfileId = entrantProfileId; }

        /** @return status */
        public EventEntrantStatus getStatus() { return status; }

        /** @param status status */
        public void setStatus(EventEntrantStatus status) { this.status = status; }

        /** @return timestamp */
        public long getTimestampEpochSec() { return timestampEpochSec; }

        /** @param timestampEpochSec timestamp */
        public void setTimestampEpochSec(long timestampEpochSec) { this.timestampEpochSec = timestampEpochSec; }
    }
}
