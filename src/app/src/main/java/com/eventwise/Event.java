package com.eventwise;

import com.eventwise.Enum.EventEntrantStatus;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Event for Entrants to join, Organizers to create and Admin's to mod.
 *
 * @author Becca Irving
 * @since 2026-03-04
 * Updated By Becca Irving on 2026-03-09
 */
@IgnoreExtraProperties
public class Event {

    /**
     * TODO (Event.java)
     * - Event Ids will be Firestore auto generated so add eventId creation inside DatabaseManager when creating events.
     * - Decide if Location should stay a string or become an object/Id.
     * - Add validation rules! (e.g., end after start, registration open before close).
     * - Add DatabaseManager methods to join/leave waiting list.
     * - Add logic to handle cancelled/declined entrants triggering redraws.
     * - Add unit tests!!!
     */

    private String eventId;
    private String organizerProfileId;
    private String name;
    private String description;
    private double price;
    private String locationName;
    private ArrayList<Tag> tags = new ArrayList<>();
    private long eventStartEpochSec;
    private long eventEndEpochSec;
    private String posterPath;
    private long registrationOpenEpochSec;
    private long registrationCloseEpochSec;
    private boolean geolocationRequired;
    private Integer maxWaitingListSize;
    private boolean privateEvent;

    private int maxWinnersToSample;
    private String qrCodeId;
    private ArrayList<EntrantStatusEntry> entrantStatuses = new ArrayList<>();
    private  ArrayList<Comment> comments = new ArrayList<>();

    public Event() {}

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerProfileId() { return organizerProfileId; }
    public void setOrganizerProfileId(String organizerProfileId) { this.organizerProfileId = organizerProfileId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public ArrayList<Tag> getTags() { return tags; }
    public void setTags(ArrayList<Tag> tags) { this.tags = tags; }

    public long getEventStartEpochSec() { return eventStartEpochSec; }
    public void setEventStartEpochSec(long eventStartEpochSec) { this.eventStartEpochSec = eventStartEpochSec; }

    public long getEventEndEpochSec() { return eventEndEpochSec; }
    public void setEventEndEpochSec(long eventEndEpochSec) { this.eventEndEpochSec = eventEndEpochSec; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public long getRegistrationOpenEpochSec() { return registrationOpenEpochSec; }
    public void setRegistrationOpenEpochSec(long registrationOpenEpochSec) { this.registrationOpenEpochSec = registrationOpenEpochSec; }

    public long getRegistrationCloseEpochSec() { return registrationCloseEpochSec; }
    public void setRegistrationCloseEpochSec(long registrationCloseEpochSec) { this.registrationCloseEpochSec = registrationCloseEpochSec; }

    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    public Integer getMaxWaitingListSize() { return maxWaitingListSize; }
    public void setMaxWaitingListSize(Integer maxWaitingListSize) { this.maxWaitingListSize = maxWaitingListSize; }

    public boolean isPrivateEvent() { return privateEvent; }
    public void setPrivateEvent(boolean privateEvent) { this.privateEvent = privateEvent; }

    public int getMaxWinnersToSample() { return maxWinnersToSample; }
    public void setMaxWinnersToSample(int maxWinnersToSample) { this.maxWinnersToSample = maxWinnersToSample; }

    public String getQrCodeId() { return qrCodeId; }
    public void setQrCodeId(String qrCodeId) { this.qrCodeId = qrCodeId; }

    public ArrayList<Comment> getComments() { return comments; }

    public void setComments(ArrayList<Comment> comments) { this.comments = comments; }


    public ArrayList<EntrantStatusEntry> getEntrantStatuses() { return entrantStatuses; }
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

    public void addOrUpdateEntrantStatus(String entrantProfileId,
                                         EventEntrantStatus status,
                                         long timestamp) {
        addOrUpdateEntrantStatus(entrantProfileId, status, timestamp, null);
    }
    public void addOrUpdateEntrantStatus(String entrantProfileId, EventEntrantStatus status, long timestamp, Location joinLocation) {
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
                entry.setJoinLocation(joinLocation);
                return;
            }
        }

        entrantStatuses.add(new EntrantStatusEntry(entrantProfileId, status, timestamp, joinLocation));
    }

    /**
     * Removes one entrant state from this event entirely.
     *
     * @param entrantProfileId entrant profile id
     */
    public void removeEntrantStatus(String entrantProfileId) {
        if (entrantStatuses == null || entrantProfileId == null) {
            return;
        }

        for (int i = entrantStatuses.size() - 1; i >= 0; i--) {
            EntrantStatusEntry entry = entrantStatuses.get(i);
            if (entry != null
                    && entry.getEntrantProfileId() != null
                    && entrantProfileId.equals(entry.getEntrantProfileId())) {
                entrantStatuses.remove(i);
            }
        }
    }

    /**
     * Returns all entrant Ids matching a given status.
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



    public static class EntrantStatusEntry {
        private String entrantProfileId;
        private EventEntrantStatus status;
        private long timestampEpochSec;
        private Location joinLocation;

        public EntrantStatusEntry() {}

        public EntrantStatusEntry(String entrantProfileId, EventEntrantStatus status, long timestampEpochSec) {
            this.entrantProfileId = entrantProfileId;
            this.status = status;
            this.timestampEpochSec = timestampEpochSec;
        }

        public EntrantStatusEntry(String entrantProfileId, EventEntrantStatus status, long timestampEpochSec, Location joinLocation) {
            this.entrantProfileId = entrantProfileId;
            this.status = status;
            this.timestampEpochSec = timestampEpochSec;
            this.joinLocation = joinLocation;
        }

        public String getEntrantProfileId() { return entrantProfileId; }
        public void setEntrantProfileId(String entrantProfileId) { this.entrantProfileId = entrantProfileId; }

        public EventEntrantStatus getStatus() { return status; }
        public void setStatus(EventEntrantStatus status) { this.status = status; }

        public long getTimestampEpochSec() { return timestampEpochSec; }
        public void setTimestampEpochSec(long timestampEpochSec) { this.timestampEpochSec = timestampEpochSec; }
        public Location getJoinLocation() { return joinLocation; }
        public void setJoinLocation(Location joinLocation) { this.joinLocation = joinLocation; }
    }
}
