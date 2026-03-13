package com.eventwise;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Represents an invitation sent from an Organizer to an Entrant for a specific event.
 *
 * This model is intended to be stored in Firestore under:
 *   events/{eventId}/invitations/{entrantId}
 *
 * Optional read-optimized mirror:
 *   users/{entrantId}/invitations/{eventId}
 *
 * Key Responsibilities:
 * - Track the invitation state (INVITED / ACCEPTED / WAITLISTED / DECLINED)
 * - Track timestamps for creation, updates, and expiration
 * - Provide helper methods to check validity and handle state changes
 *
 * Firestore requires:
 * - A public no‑arg constructor
 * - Getter/Setter methods for all serializable fields
 */
public class Invitation {

    /** ID of the event to which this invitation belongs. */
    private String eventId;

    /** Profile ID of the entrant who receives the invitation. */
    private String entrantId;

    /** Current invitation status. */
    private EventEntrantStatus status;

    /** The timestamp (epoch seconds) when this invitation was created. */
    private long createdAtEpochSec;

    /** The timestamp (epoch seconds) when this invitation was last updated. */
    private long updatedAtEpochSec;


    /**
     * Optional expiration time (epoch seconds).
     * - If null → invitation does not expire automatically.
     * - If not null → system checks if expired before allowing actions (e.g., decline).
     */
    @Nullable
    private Long expiresAtEpochSec;

    /** Optional note or message attached to this invitation. */
    @Nullable
    private String note;

    /** Required by Firestore for automatic deserialization. */
    public Invitation() {}

    /**
     * Constructor for creating a new invitation.
     *
     * @param eventId ID of the event
     * @param entrantId ID of the entrant receiving the invitation
     * @param expiresAtEpochSec optional expiration time (epoch seconds)
     * @param note optional custom message or note
     */
    public Invitation(
            @NonNull String eventId,
            @NonNull String entrantId,
            @Nullable Long expiresAtEpochSec,
            @Nullable String note
    ) {
        long now = nowEpochSec();
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = EventEntrantStatus.INVITED;
        this.createdAtEpochSec = now;
        this.updatedAtEpochSec = now;
        this.expiresAtEpochSec = expiresAtEpochSec;
        this.note = note;
    }


    /** @return Current time in epoch seconds. */
    public static long nowEpochSec() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * @return true if invitation has expired (only checked when expiration time exists).
     */
    public boolean isExpired() {
        return expiresAtEpochSec != null && nowEpochSec() > expiresAtEpochSec;
    }

    /**
     * Checks whether the entrant can decline this invitation.
     * The business rules can be adjusted here.
     *
     * @return true if the invitation is in a state that can be declined.
     */
    public boolean canDecline() {
        if (isExpired() || status == null) return false;
        switch (status) {
            case INVITED:
            case WAITLISTED:
            case ACCEPTED:     // If your business allows "decline after accept"
                return true;
            default:
                return false;
        }
    }


    /**
     * Updates the status to DECLINED and updates the timestamp.
     * This modifies ONLY the local object — database write must be handled separately.
     */
    public void toDeclined() {
        this.status = EventEntrantStatus.DECLINED;
        this.updatedAtEpochSec = nowEpochSec();
    }

    /**
     * Updates the status to the given value and updates the timestamp.
     *
     * @param newStatus new invitation status
     */
    public void toStatus(@NonNull EventEntrantStatus newStatus) {
        this.status = newStatus;
        this.updatedAtEpochSec = nowEpochSec();
    }


    @NonNull
    public String getEventId() { return eventId; }

    public void setEventId(@NonNull String eventId) { this.eventId = eventId; }

    @NonNull
    public String getEntrantId() { return entrantId; }

    public void setEntrantId(@NonNull String entrantId) { this.entrantId = entrantId; }

    @NonNull
    public EventEntrantStatus getStatus() { return status; }

    public void setStatus(@NonNull EventEntrantStatus status) { this.status = status; }

    public long getCreatedAtEpochSec() { return createdAtEpochSec; }

    public void setCreatedAtEpochSec(long createdAtEpochSec) {
        this.createdAtEpochSec = createdAtEpochSec;
    }

    public long getUpdatedAtEpochSec() { return updatedAtEpochSec; }

    public void setUpdatedAtEpochSec(long updatedAtEpochSec) {
        this.updatedAtEpochSec = updatedAtEpochSec;
    }

    @Nullable
    public Long getExpiresAtEpochSec() { return expiresAtEpochSec; }

    public void setExpiresAtEpochSec(@Nullable Long expiresAtEpochSec) {
        this.expiresAtEpochSec = expiresAtEpochSec;
    }


    @Nullable
    public String getNote() { return note; }

    public void setNote(@Nullable String note) { this.note = note; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invitation)) return false;
        Invitation that = (Invitation) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(entrantId, that.entrantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, entrantId);
    }

    @NonNull
    @Override
    public String toString() {
        return "Invitation{" +
                "eventId='" + eventId + '\'' +
                ", entrantId='" + entrantId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAtEpochSec +
                ", updatedAt=" + updatedAtEpochSec +
                ", expiresAt=" + expiresAtEpochSec +
                '}';
    }
}





