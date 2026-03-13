package com.eventwise;

/**
 * Stores shared entrant status values for events.
 *
 * @author Becca Irving
 * @since 2026-03-09
 */
public enum EventEntrantStatus {
    /** Entrant is currently on the waiting list. */
    WAITLISTED,

    /** Entrant voluntarily left the waiting list. */
    LEFT_WAITLIST,

    /** Entrant was selected/invited from the waiting list. */
    INVITED,

    /** Entrant has accepted the invitation. */
    ACCEPTED,

    /** Entrant accepted and is fully enrolled in the event. */
    ENROLLED,

    /** Entrant declined their invitation. */
    DECLINED,

    /** Entrant cancelled participation. */
    CANCELLED,

    /** Entrant was removed by organizer/admin/system. */
    REMOVED,

    /** Entrant was not selected in the lottery. */
    LOST_LOTTERY
}
