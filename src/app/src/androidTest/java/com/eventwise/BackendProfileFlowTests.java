package com.eventwise;

import androidx.test.core.app.ApplicationProvider;

import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Additional Entrant Back end testing with some event
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-09
 */

public class BackendProfileFlowTests extends DatabaseManagerTests {

    private final ArrayList<String> profileIdsToDelete = new ArrayList<>();
    private final ArrayList<String> eventIdsToDelete = new ArrayList<>();

    @After
    public void cleanup() throws ExecutionException, InterruptedException {
        for (String eventId : eventIdsToDelete) {
            Tasks.await(testDb.collection("events").document(eventId).delete());
        }

        for (String profileId : profileIdsToDelete) {
            Tasks.await(testDb.collection("profiles").document(profileId).delete());
        }
    }

    @Test
    public void stubEntrantCanExistWithoutCompletedProfile() throws Exception {
        String entrantId = UUID.randomUUID().toString();
        profileIdsToDelete.add(entrantId);

        Entrant entrant = new Entrant("", "", "", true, ApplicationProvider.getApplicationContext());
        entrant.setProfileId(entrantId);
        entrant.setDeviceId(entrantId);

        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        Tasks.await(dbManager.addEntrant(entrant));

        Entrant returnedEntrant = Tasks.await(dbManager.getEntrantFromId(entrantId));

        Assert.assertNotNull(returnedEntrant);
        Assert.assertEquals(entrantId, returnedEntrant.getProfileId());
        Assert.assertEquals(entrantId, returnedEntrant.getDeviceId());
        Assert.assertTrue(returnedEntrant.getNotificationsEnabled());
        Assert.assertFalse(returnedEntrant.hasCompletedProfile());
    }

    @Test
    public void clearEntrantProfilePreservesIdentityAndNotificationsAndCancelsEnrollment() throws Exception {
        String entrantId = UUID.randomUUID().toString();
        String enrolledEventId = UUID.randomUUID().toString();
        String waitlistedEventId = UUID.randomUUID().toString();

        profileIdsToDelete.add(entrantId);
        eventIdsToDelete.add(enrolledEventId);
        eventIdsToDelete.add(waitlistedEventId);

        long ts = System.currentTimeMillis() / 1000L;

        Entrant entrant = new Entrant("Becca", "becca@test.com", "7805550101", false,
                ApplicationProvider.getApplicationContext());
        entrant.setProfileId(entrantId);
        entrant.setDeviceId(entrantId);
        entrant.addOrUpdateEventState(enrolledEventId, EventEntrantStatus.ENROLLED, ts);
        entrant.addOrUpdateEventState(waitlistedEventId, EventEntrantStatus.WAITLISTED, ts);

        Event enrolledEvent = buildEvent("ORG_TEST", enrolledEventId, "Enrolled Event");
        enrolledEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.ENROLLED, ts);

        Event waitlistedEvent = buildEvent("ORG_TEST", waitlistedEventId, "Waitlisted Event");
        waitlistedEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, ts);

        EntrantDatabaseManager entrantDb = new EntrantDatabaseManager(testDb);
        OrganizerDatabaseManager organizerDb = new OrganizerDatabaseManager(testDb);

        Tasks.await(entrantDb.addEntrant(entrant));
        Tasks.await(organizerDb.addEvent(enrolledEvent));
        Tasks.await(organizerDb.addEvent(waitlistedEvent));

        Tasks.await(entrantDb.clearEntrantProfile(entrantId));

        Entrant clearedEntrant = Tasks.await(entrantDb.getEntrantFromId(entrantId));

        Assert.assertEquals(entrantId, clearedEntrant.getProfileId());
        Assert.assertEquals(entrantId, clearedEntrant.getDeviceId());
        Assert.assertEquals("", clearedEntrant.getName());
        Assert.assertEquals("", clearedEntrant.getEmail());
        Assert.assertEquals("", clearedEntrant.getPhone());
        Assert.assertFalse(clearedEntrant.getNotificationsEnabled());

        Assert.assertEquals(
                EventEntrantStatus.CANCELLED,
                getEntrantEventStatus(clearedEntrant, enrolledEventId)
        );
        Assert.assertEquals(
                EventEntrantStatus.WAITLISTED,
                getEntrantEventStatus(clearedEntrant, waitlistedEventId)
        );

        DocumentSnapshot enrolledSnapshot = Tasks.await(testDb.collection("events").document(enrolledEventId).get());
        Event returnedEnrolledEvent = enrolledSnapshot.toObject(Event.class);

        DocumentSnapshot waitlistedSnapshot = Tasks.await(testDb.collection("events").document(waitlistedEventId).get());
        Event returnedWaitlistedEvent = waitlistedSnapshot.toObject(Event.class);

        Assert.assertTrue(returnedEnrolledEvent.getEntrantIdsByStatus(EventEntrantStatus.CANCELLED).contains(entrantId));
        Assert.assertTrue(returnedWaitlistedEvent.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId));
    }

    private Event buildEvent(String organizerId, String eventId, String name) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Testing", "Backend"));

        Event event = new Event(
                organizerId,
                name,
                "Description",
                0.0,
                "Test Location",
                tags,
                1000L,
                2000L,
                500L,
                900L,
                false,
                10,
                3,
                null,
                null
        );
        event.setEventId(eventId);
        return event;
    }

    private EventEntrantStatus getEntrantEventStatus(Entrant entrant, String eventId) {
        for (Entrant.EventStateEntry entry : entrant.getEventStates()) {
            if (entry != null && eventId.equals(entry.getEventId())) {
                return entry.getStatus();
            }
        }
        return null;
    }
}
