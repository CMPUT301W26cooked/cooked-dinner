package com.eventwise;

import com.eventwise.database.EventFilter;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FilterTests extends DatabaseManagerTests{

    private final long ONE_DAY_IN_SECONDS = 86400;
    private final long JAN_1_2026 = 1767225600;

    private ArrayList<Event> randomEvents = new ArrayList<>();
    private ArrayList<String> randomEventIds = new ArrayList<>();
    private String randomOrganizerId;

    @Before
    public void TestCaseSetup() throws InterruptedException, ExecutionException {

        OrganizerDatabaseManager organizerDbManager = new OrganizerDatabaseManager(testDb);


        randomOrganizerId = UUID.randomUUID().toString();

        //Create fake events
        for (int i = 0; i < 10; ++i) {
            Event event = new Event(
                    randomOrganizerId,
                    "Test Event: " + i,
                    "Test Description",
                    10.0,
                    "Test Location",
                    new ArrayList<Tag>(),
                    JAN_1_2026 + (ONE_DAY_IN_SECONDS * i), //start time, i days from jan 1 2026
                    JAN_1_2026 + ONE_DAY_IN_SECONDS + ((ONE_DAY_IN_SECONDS * i) -1), //end time, 1 day later (minus 1 second)
                    JAN_1_2026, //registration open time, Jan 1 2026
                    JAN_1_2026, //registration close time, Jan 1, 2027
                    false, //Geolocation required
                    100, //Max waiting list size
                    100, //Max winners to sample
                    null, //Poster path
                    null //QR Code Id
            );
            Tasks.await(organizerDbManager.addEvent(event));
            randomEvents.add(event);

        }
        //Get Ids
        ArrayList<Event> events = Tasks.await(organizerDbManager.getOrganizersCreatedEventsFromOrganizerId(randomOrganizerId));
        for (Event e : events){
            randomEventIds.add(e.getEventId());
        }

        Thread.sleep(2000);




    };


    @Test
    public void checkStartTimeStamp() throws ExecutionException, InterruptedException {
        EventSearcherDatabaseManager eventDbManager = new EventSearcherDatabaseManager(testDb);

        //Get all events that start on Jan 4 or later
        EventFilter eventFilter = new EventFilter(JAN_1_2026 + (ONE_DAY_IN_SECONDS * 3), null, null, null, null);
        ArrayList<Event> returnedEvents = Tasks.await(eventDbManager.getFilteredEvents(eventFilter));
        //Only return events that this test created
        returnedEvents.retainAll(randomEvents);
        ArrayList<Event> expectedEvents = new ArrayList<>(randomEvents.subList(3, randomEvents.size()));

        Assert.assertEquals(returnedEvents, expectedEvents);

    }

    @Test
    public void checkEndTimeStamp() throws ExecutionException, InterruptedException {
        EventSearcherDatabaseManager eventDbManager = new EventSearcherDatabaseManager(testDb);

        //Get all events that end on Jan 6 at 23:59:59 or earlier
        EventFilter eventFilter = new EventFilter(null, JAN_1_2026 + (ONE_DAY_IN_SECONDS * 6), null, null, null);
        ArrayList<Event> returnedEvents = Tasks.await(eventDbManager.getFilteredEvents(eventFilter));
        //Only return events that this test created
        returnedEvents.retainAll(randomEvents);

        ArrayList<Event> expectedEvents = new ArrayList<>(randomEvents.subList(0, randomEvents.size() - 4));



        Assert.assertEquals(returnedEvents, expectedEvents);
    }

    @Test
    public void checkEndAndStartTimeStamp() throws ExecutionException, InterruptedException {
        EventSearcherDatabaseManager eventDbManager = new EventSearcherDatabaseManager(testDb);

        //Get all events that start on Jan 4 or later and end on Jan 6 at 23:59:59 or earlier
        EventFilter eventFilter = new EventFilter(JAN_1_2026 + (ONE_DAY_IN_SECONDS * 3), JAN_1_2026 + (ONE_DAY_IN_SECONDS * 6), null, null, null);
        ArrayList<Event> returnedEvents = Tasks.await(eventDbManager.getFilteredEvents(eventFilter));
        //Only return events that this test created
        returnedEvents.retainAll(randomEvents);
        ArrayList<Event> expectedEvents = new ArrayList<>(randomEvents.subList(3, randomEvents.size() - 4));

        Assert.assertEquals(returnedEvents, expectedEvents);
    }


    @Test
    public void checkKeywords() throws ExecutionException, InterruptedException {
        EventSearcherDatabaseManager eventDbManager = new EventSearcherDatabaseManager(testDb);
        EventFilter eventFilter = new EventFilter(null, null, null, new ArrayList<String>(List.of("3")), null);
        ArrayList<Event> returnedEvents = Tasks.await(eventDbManager.getFilteredEvents(eventFilter));
        //Only return events that this test created
        returnedEvents.retainAll(randomEvents);
        ArrayList<Event> expectedEvents = new ArrayList<>(List.of(randomEvents.get(3)));

        Assert.assertEquals(returnedEvents, expectedEvents);
    }

    @After
    public void deleteTestData() throws ExecutionException, InterruptedException {
        for (String eventId : randomEventIds){
            Tasks.await(testDb.collection("events").document(eventId).delete());
        }
        Tasks.await(testDb.collection("profiles").document(randomOrganizerId).delete());
    }


}
