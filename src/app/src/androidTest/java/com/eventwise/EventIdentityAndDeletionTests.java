package com.eventwise;

import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Tests for event identity and for deletion
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-09
 */

public class EventIdentityAndDeletionTests extends DatabaseManagerTests {

    private final ArrayList<String> eventIdsToDelete = new ArrayList<>();

    @After
    public void cleanup() throws ExecutionException, InterruptedException {
        for (String eventId : eventIdsToDelete) {
            Tasks.await(testDb.collection("events").document(eventId).delete());
        }
    }

    @Test
    public void addEventWithoutIdAssignsIdAndPersistsDocument() throws Exception {
        OrganizerDatabaseManager organizerDb = new OrganizerDatabaseManager(testDb);

        Event event = buildEvent("ORG_AUTO", "Auto Id Event");

        Tasks.await(organizerDb.addEvent(event));

        Assert.assertNotNull(event.getEventId());
        Assert.assertFalse(event.getEventId().trim().isEmpty());

        eventIdsToDelete.add(event.getEventId());

        Assert.assertTrue(
                Tasks.await(testDb.collection("events").document(event.getEventId()).get()).exists()
        );
    }

    @Test
    public void getEventsRestoresMissingEventIdFromDocumentId() throws Exception {
        String docId = UUID.randomUUID().toString();
        String uniqueName = "Raw Event " + docId;
        eventIdsToDelete.add(docId);

        Map<String, Object> rawEvent = new HashMap<>();
        rawEvent.put("organizerProfileId", "ORG_RAW");
        rawEvent.put("name", uniqueName);
        rawEvent.put("description", "Missing eventId field");
        rawEvent.put("price", 0.0);
        rawEvent.put("locationName", "Raw Location");
        rawEvent.put("tags", new ArrayList<>());
        rawEvent.put("eventStartEpochSec", 1000L);
        rawEvent.put("eventEndEpochSec", 2000L);
        rawEvent.put("posterPath", null);
        rawEvent.put("registrationOpenEpochSec", 500L);
        rawEvent.put("registrationCloseEpochSec", 900L);
        rawEvent.put("geolocationRequired", false);
        rawEvent.put("maxWaitingListSize", 10);
        rawEvent.put("maxWinnersToSample", 5);
        rawEvent.put("qrCodeId", null);
        rawEvent.put("entrantStatuses", new ArrayList<>());

        Tasks.await(testDb.collection("events").document(docId).set(rawEvent));

        EventSearcherDatabaseManager eventSearcherDb = new EventSearcherDatabaseManager(testDb);
        ArrayList<Event> returnedEvents = Tasks.await(eventSearcherDb.getEvents());

        Event matchingEvent = null;
        for (Event event : returnedEvents) {
            if (event != null && uniqueName.equals(event.getName())) {
                matchingEvent = event;
                break;
            }
        }

        Assert.assertNotNull(matchingEvent);
        Assert.assertEquals(docId, matchingEvent.getEventId());
    }

    @Test
    public void deleteEventRemovesDocument() throws Exception {
        OrganizerDatabaseManager organizerDb = new OrganizerDatabaseManager(testDb);
        EventSearcherDatabaseManager eventSearcherDb = new EventSearcherDatabaseManager(testDb);

        Event event = buildEvent("ORG_DELETE", "Delete Event");
        Tasks.await(organizerDb.addEvent(event));

        Assert.assertNotNull(event.getEventId());

        Tasks.await(eventSearcherDb.deleteEvent(event));

        Assert.assertFalse(
                Tasks.await(testDb.collection("events").document(event.getEventId()).get()).exists()
        );
    }

    private Event buildEvent(String organizerId, String name) {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("Testing", "Event"));

        return new Event(
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
    }
}
