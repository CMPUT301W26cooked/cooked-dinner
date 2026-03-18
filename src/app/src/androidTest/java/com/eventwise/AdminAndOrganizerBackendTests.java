package com.eventwise;

import androidx.test.core.app.ApplicationProvider;

import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Testing fir organizer backend additions
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-09
 */
public class AdminAndOrganizerBackendTests extends DatabaseManagerTests {

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
    public void adminGetAllEntrantsIncludesCreatedEntrant() throws Exception {
        String entrantId = UUID.randomUUID().toString();
        profileIdsToDelete.add(entrantId);

        Entrant entrant = new Entrant("Entrant One", "entrant@test.com", "7805550101", true,
                ApplicationProvider.getApplicationContext());
        entrant.setProfileId(entrantId);
        entrant.setDeviceId(entrantId);

        EntrantDatabaseManager entrantDb = new EntrantDatabaseManager(testDb);
        AdminDatabaseManager adminDb = new AdminDatabaseManager(testDb);

        Tasks.await(entrantDb.addEntrant(entrant));

        ArrayList<Entrant> entrants = Tasks.await(adminDb.getAllEntrants());

        boolean found = false;
        for (Entrant returnedEntrant : entrants) {
            if (returnedEntrant != null && entrantId.equals(returnedEntrant.getProfileId())) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);
    }

    @Test
    public void adminGetAllOrganizersIncludesCreatedOrganizer() throws Exception {
        String organizerId = UUID.randomUUID().toString();
        profileIdsToDelete.add(organizerId);

        Organizer organizer = new Organizer("Organizer One", "organizer@test.com", "7805550202", true);
        organizer.setProfileId(organizerId);

        Tasks.await(testDb.collection("profiles").document(organizerId).set(organizer));

        AdminDatabaseManager adminDb = new AdminDatabaseManager(testDb);
        ArrayList<Organizer> organizers = Tasks.await(adminDb.getAllOrganizers());

        boolean found = false;
        for (Organizer returnedOrganizer : organizers) {
            if (returnedOrganizer != null && organizerId.equals(returnedOrganizer.getProfileId())) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);
    }

    @Test
    public void adminRemoveProfileByIdDeletesProfileDocument() throws Exception {
        String organizerId = UUID.randomUUID().toString();

        Organizer organizer = new Organizer("Delete Organizer", "delete@test.com", "7805550303", true);
        organizer.setProfileId(organizerId);

        Tasks.await(testDb.collection("profiles").document(organizerId).set(organizer));

        AdminDatabaseManager adminDb = new AdminDatabaseManager(testDb);
        Tasks.await(adminDb.removeProfileById(organizerId));

        Assert.assertFalse(
                Tasks.await(testDb.collection("profiles").document(organizerId).get()).exists()
        );
    }

    @Test
    public void organizerStatusListMethodsReturnCorrectEntrantIds() throws Exception {
        String eventId = UUID.randomUUID().toString();
        eventIdsToDelete.add(eventId);

        String waitlistedId = UUID.randomUUID().toString();
        String invitedId = UUID.randomUUID().toString();
        String cancelledId = UUID.randomUUID().toString();
        String enrolledId = UUID.randomUUID().toString();

        long ts = System.currentTimeMillis() / 1000L;

        Event event = buildEvent("ORG_LISTS", eventId, "Status List Event");
        event.addOrUpdateEntrantStatus(waitlistedId, EventEntrantStatus.WAITLISTED, ts);
        event.addOrUpdateEntrantStatus(invitedId, EventEntrantStatus.INVITED, ts);
        event.addOrUpdateEntrantStatus(cancelledId, EventEntrantStatus.CANCELLED, ts);
        event.addOrUpdateEntrantStatus(enrolledId, EventEntrantStatus.ENROLLED, ts);

        OrganizerDatabaseManager organizerDb = new OrganizerDatabaseManager(testDb);
        Tasks.await(organizerDb.addEvent(event));

        ArrayList<String> waitlisted = Tasks.await(organizerDb.getEntrantsIdsInWaitingListFromEventId(eventId));
        ArrayList<String> invited = Tasks.await(organizerDb.getEntrantsIdsInChosenList(eventId));
        ArrayList<String> cancelled = Tasks.await(organizerDb.getEntrantsIdsInCancelledListFromEventId(eventId));
        ArrayList<String> enrolled = Tasks.await(organizerDb.getEntrantsIdsInConfirmedListFromEventId(eventId));

        Assert.assertTrue(waitlisted.contains(waitlistedId));
        Assert.assertTrue(invited.contains(invitedId));
        Assert.assertTrue(cancelled.contains(cancelledId));
        Assert.assertTrue(enrolled.contains(enrolledId));

        Assert.assertEquals(1, waitlisted.size());
        Assert.assertEquals(1, invited.size());
        Assert.assertEquals(1, cancelled.size());
        Assert.assertEquals(1, enrolled.size());
    }

    private Event buildEvent(String organizerId, String eventId, String name) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Testing", "Lists"));

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
}
