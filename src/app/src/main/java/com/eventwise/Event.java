package com.eventwise;

import java.util.ArrayList;

/**
 * Represents an Event
 *
 * Stores all event configuration and participant lists :
 * name/description/price, location, topic, poster, registration period,
 * geolocation requirement, waiting list limit, max winners, and participant lists.
 */
public class Event {

    /**
     * TODO (Event.java)
     * - Event IDs will be Firestore auto generated so add eventId creation inside DatabaseManager when creating events.
     * - Decide if Location /Topic should stay strings or become obj IDs
     * - Add validation rules! (e.g., end after start, registration open before close)!!
     * - Add DatabaseManager methods to join/leave waiting list
     * - Add logic to handle a cancelled /declined entrant triggering a redraw (replacement them)
     * - Add unit tests!!!
     */

    /** Firestore ID for this event (auto gen by Firestore) */
    private String eventId;

    /** Profile ID of the organizer who created the event */
    private String organizerProfileId;

    /** Event name */
    private String name;

    /** Event description */
    private String description;

    /** Event price */
    private double price;

    /** Location*/
    private String locationName;

    /** Topic/category */
    private String topicName;

    /**
     * Event start time in epoch seconds.
     * Epoch seconds includes full date (year/month/day) and time (hour/min/sec).
     */
    private long eventStartEpochSec;

    /**
     * Event end time in epoch seconds.
     */
    private long eventEndEpochSec;

    /**
     * Poster reference aka Storage path
     * Can be null or empty if no poster
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
     * Null means unlimited
     */
    private Integer maxWaitingListSize;

    /** Max number of entrants to sample. */
    private int maxWinnersToSample;

    /** Unique QR code identifier linking to this event. */
    private String qrCodeId;

    /** List of entrant profile IDs on the waiting list. */
    private ArrayList<String> waitingListEntrantIds = new ArrayList<>();

    /** List of entrant profile IDs chosen/invited in lottery. */
    private ArrayList<String> chosenEntrantIds = new ArrayList<>();

    /** List of entrant profile IDs confirmed/enrolled. */
    private ArrayList<String> confirmedEntrantIds = new ArrayList<>();

    /** List of entrant profile IDs cancelled/declined/not signed up. */
    private ArrayList<String> cancelledEntrantIds = new ArrayList<>();

    /**
     * Required for Firestore (check)
     */
    public Event() {}

    /**
     * Constructor for an Event with required fields.
     *
     * Optional fields can be passed as null/empty:
     * - posterPath can be null/empty
     * - maxWaitingListSize can be null (inf)
     * - qrCodeId can be null until generated
     *
     * @param organizerProfileId organizer profile ID (required)
     * @param name event name (required)
     * @param description event description (required)
     * @param price event price (required)
     * @param locationName location/venue (required)
     * @param topicName topic/category (required)
     * @param eventStartEpochSec event start time (epoch seconds) (required)
     * @param eventEndEpochSec event end time (epoch seconds) (required)
     * @param registrationOpenEpochSec registration open time (epoch seconds) (required)
     * @param registrationCloseEpochSec registration close time (epoch seconds) (required)
     * @param geolocationRequired whether geolocation is required (required)
     * @param maxWaitingListSize optional max waiting list size (null means inf)
     * @param maxWinnersToSample max winners to sample (required)
     * @param posterPath optional poster path (nullable)
     * @param qrCodeId optional QR code id (nullable)
     */
    public Event(
            String organizerProfileId,
            String name,
            String description,
            double price,
            String locationName,
            String topicName,
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
        this.topicName = topicName;
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

    /** @return topic name */
    public String getTopicName() { return topicName; }

    /** @param topicName topic name */
    public void setTopicName(String topicName) { this.topicName = topicName; }

    /** @return start time in epoch seconds */
    public long getEventStartEpochSec() { return eventStartEpochSec; }

    /** @param eventStartEpochSec start time in epoch seconds */
    public void setEventStartEpochSec(long eventStartEpochSec) { this.eventStartEpochSec = eventStartEpochSec; }

    /** @return end time in epoch seconds */
    public long getEventEndEpochSec() { return eventEndEpochSec; }

    /** @param eventEndEpochSec end time in epoch seconds */
    public void setEventEndEpochSec(long eventEndEpochSec) { this.eventEndEpochSec = eventEndEpochSec; }

    /** @return poster path (nullable) */
    public String getPosterPath() { return posterPath; }

    /** @param posterPath poster path (nullable) */
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

    /** @return max waiting list size (nullable => unlimited) */
    public Integer getMaxWaitingListSize() { return maxWaitingListSize; }

    /** @param maxWaitingListSize max waiting list size (nullable => unlimited) */
    public void setMaxWaitingListSize(Integer maxWaitingListSize) { this.maxWaitingListSize = maxWaitingListSize; }

    /** @return max winners to sample */
    public int getMaxWinnersToSample() { return maxWinnersToSample; }

    /** @param maxWinnersToSample max winners */
    public void setMaxWinnersToSample(int maxWinnersToSample) { this.maxWinnersToSample = maxWinnersToSample; }

    /** @return qr code id (nullable until generated) */
    public String getQrCodeId() { return qrCodeId; }

    /** @param qrCodeId qr code id (nullable) */
    public void setQrCodeId(String qrCodeId) { this.qrCodeId = qrCodeId; }

    /** @return waiting list entrant ids */
    public ArrayList<String> getWaitingListEntrantIds() { return waitingListEntrantIds; }

    /** @param waitingListEntrantIds waiting list entrant ids */
    public void setWaitingListEntrantIds(ArrayList<String> waitingListEntrantIds) { this.waitingListEntrantIds = waitingListEntrantIds; }

    /** @return chosen entrant ids */
    public ArrayList<String> getChosenEntrantIds() { return chosenEntrantIds; }

    /** @param chosenEntrantIds chosen entrant ids */
    public void setChosenEntrantIds(ArrayList<String> chosenEntrantIds) { this.chosenEntrantIds = chosenEntrantIds; }

    /** @return confirmed entrant ids */
    public ArrayList<String> getConfirmedEntrantIds() { return confirmedEntrantIds; }

    /** @param confirmedEntrantIds confirmed entrant ids */
    public void setConfirmedEntrantIds(ArrayList<String> confirmedEntrantIds) { this.confirmedEntrantIds = confirmedEntrantIds; }

    /** @return cancelled entrant ids */
    public ArrayList<String> getCancelledEntrantIds() { return cancelledEntrantIds; }

    /** @param cancelledEntrantIds cancelled entrant ids */
    public void setCancelledEntrantIds(ArrayList<String> cancelledEntrantIds) { this.cancelledEntrantIds = cancelledEntrantIds; }

    /**
     * Checks whether registration is currently open
     *
     * @return true if now is between open and close times (inclusive!!!)
     */
    public boolean isRegistrationOpenNow() {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= registrationOpenEpochSec && nowEpochSec <= registrationCloseEpochSec;
    }

    /**
     * Returns the current waiting list count
     *
     * @return waiting list count
     */
    public int getWaitingListCount() {
        return waitingListEntrantIds == null ? 0 : waitingListEntrantIds.size();
    }

    /**
     * Checks whether the waiting list is full (if maxWaitingListSize is set).
     *
     * @return true if full, false o/w
     */
    public boolean isWaitingListFull() {
        if (maxWaitingListSize == null) {
            return false;
        }
        return getWaitingListCount() >= maxWaitingListSize;
    }
}
