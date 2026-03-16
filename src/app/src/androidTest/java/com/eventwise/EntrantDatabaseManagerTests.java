package com.eventwise;

import androidx.test.core.app.ApplicationProvider;

import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class EntrantDatabaseManagerTests extends DatabaseManagerTests {

    private String randomEventId;
    private String randomEntrantId;

    @Before
    public void TestCaseSetup() throws InterruptedException {
        randomEventId = UUID.randomUUID().toString();

        OrganizerDatabaseManager organizerDbManager = new OrganizerDatabaseManager(testDb);

        ArrayList<Tag> testTags = new ArrayList<>();
        testTags.add(new Tag("Testing", "TestKeyword"));

        Event event = new Event(
                "TestOrganizerProfileId",
                "Test Event",
                "Test Description",
                10.0,
                "Test Location",
                testTags,
                0,
                0,
                0,
                0,
                false,
                10,
                4,
                null,
                null
        );

        event.setEventId(randomEventId);
        organizerDbManager.addEvent(event);
        Thread.sleep(2000);




    }

    @After
    public void deleteTestData() throws ExecutionException, InterruptedException {
        Tasks.await(testDb.collection("events").document(randomEventId).delete());
        Tasks.await(testDb.collection("profiles").document(randomEntrantId).delete());
    }

    @Test
    public void addEntrant() throws ExecutionException, InterruptedException {
        Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        randomEntrantId = entrant.getProfileId();
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);

        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileId()).get());
        Assert.assertTrue(snapshot.exists());
    }

    @Test
    public void updateEntrant() throws ExecutionException, InterruptedException {
        Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        randomEntrantId = entrant.getProfileId();
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        Tasks.await(dbManager.addEntrant(entrant));

        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(randomEntrantId).get());
        entrant = snapshot.toObject(Entrant.class);
        entrant.setName("Patrick");
        Tasks.await(dbManager.updateEntrantInfo(entrant));
        snapshot = Tasks.await(testDb.collection("profiles").document(randomEntrantId).get());
        entrant = snapshot.toObject(Entrant.class);
        Assert.assertEquals("Patrick", entrant.getName());

    }



    @Test
    public void registerEntrantInEvent() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        randomEntrantId = entrant.getProfileId();
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);


        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileId()).get());
        Assert.assertTrue(snapshot.exists());

        //Get event
        snapshot = Tasks.await(testDb.collection("events").document(randomEventId).get());
        Event event = snapshot.toObject(Event.class);

        long localTimestamp = System.currentTimeMillis() / 1000L;

        event.addOrUpdateEntrantStatus(randomEntrantId, EventEntrantStatus.WAITLISTED, localTimestamp);
        entrant.addOrUpdateEventState(randomEventId, EventEntrantStatus.WAITLISTED, localTimestamp);

        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantId, randomEventId, localTimestamp));

        snapshot = Tasks.await(testDb.collection("events").document(randomEventId).get());
        event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIds = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIds.contains(randomEntrantId));
    }

    @Test
    public void unregisterEntrantInEvent() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        randomEntrantId = entrant.getProfileId();
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);

        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileId()).get());
        Assert.assertTrue(snapshot.exists());


        long localTimestamp = System.currentTimeMillis() / 1000L;
        //Register the entrant
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantId, randomEventId, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventId).get());
        Event event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIds = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIds.contains(randomEntrantId));

        localTimestamp = System.currentTimeMillis() / 1000L;
        //Unregister the entrant
        Tasks.await(dbManager.unregisterEntrantInEvent(randomEntrantId, randomEventId, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventId).get());
        event = snapshot.toObject(Event.class);
        waitingListEntrantIds = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertFalse(waitingListEntrantIds.contains(randomEntrantId));
    }


    @Test
    public void verifyNoDataLoss() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
        randomEntrantId = entrant.getProfileId();
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileId()).get());
        Assert.assertTrue(snapshot.exists());

        long localTimestamp = System.currentTimeMillis() / 1000L;

        //Register the entrant on Firebase
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantId, randomEventId, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventId).get());
        Event event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIds = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIds.contains(randomEntrantId));

        //Register the entrant locally
        entrant.addOrUpdateEventState(randomEventId, EventEntrantStatus.WAITLISTED, localTimestamp);

        AdminDatabaseManager adminDbManager = new AdminDatabaseManager(testDb);
        //Get the entrant
        Entrant returnedEntrant = Tasks.await(adminDbManager.getEntrantFromId(randomEntrantId));

        Assert.assertEquals(entrant, returnedEntrant);

    }













}
