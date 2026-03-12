package com.eventwise;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.database.DatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.NotificationDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class EntrantDatabaseManagerTests extends DatabaseManagerTests {

    private String randomEventID;
    private String randomEntrantID;



    @Before
    public void TestCaseSetup() throws InterruptedException {

        //Generate RandomIDs for everything to avoid collisions
        randomEventID = UUID.randomUUID().toString();
        randomEntrantID = UUID.randomUUID().toString();


        //Make a dummy event to add the entrant to
        OrganizerDatabaseManager organizerDbManager = new OrganizerDatabaseManager(testDb);

        ArrayList<Tag> test_tags = new ArrayList<Tag>();

        test_tags.add(new Tag("Testing", "TestKeyword"));

        Event event = new Event(
                "TestOrganizerProfileID",
                "Test Event",
                "Test Description",
                10.0,
                "Test Location",
                test_tags,
                0,
                0,
                0,
                0,
                false,
                10,
                4,
                null,
                null);

        event.setEventId(randomEventID);
        organizerDbManager.addEvent(event);
        Thread.sleep(2000);




    }

    @After
    public void deleteTestData() throws ExecutionException, InterruptedException {
        Tasks.await(testDb.collection("events").document(randomEventID).delete());
        Tasks.await(testDb.collection("profiles").document(randomEntrantID).delete());
    }

    @Test
    public void addEntrant() throws ExecutionException, InterruptedException {
        Entrant entrant = new Entrant(randomEntrantID, "Spongebob", "sponge@krustykrab.ca", "1234567890", true);
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);

        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileID()).get());
        Assert.assertTrue(snapshot.exists());
    }

    @Test
    public void updateEntrant() throws ExecutionException, InterruptedException {

        //Make an entrant
        Entrant entrant = new Entrant(randomEntrantID, "Spongebob", "sponge@krustykrab.ca", "1234567890", true);
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        Tasks.await(dbManager.addEntrant(entrant));

        //Update the entrant
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(randomEntrantID).get());
        entrant = snapshot.toObject(Entrant.class);
        entrant.setName("Patrick");
        Tasks.await(dbManager.updateEntrantInfo(entrant));
        snapshot = Tasks.await(testDb.collection("profiles").document(randomEntrantID).get());
        entrant = snapshot.toObject(Entrant.class);
        Assert.assertEquals("Patrick", entrant.getName());

    }



    @Test
    public void registerEntrantInEvent() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant(randomEntrantID, "Spongebob", "sponge@krustykrab.ca", "1234567890", true);
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);


        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileID()).get());
        Assert.assertTrue(snapshot.exists());

        //Get event
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        Event event = snapshot.toObject(Event.class);

        long localTimestamp = System.currentTimeMillis() / 1000L;

        //Register the entrant locally
        event.addOrUpdateEntrantStatus(randomEntrantID, EventEntrantStatus.WAITLISTED, localTimestamp);
        entrant.addOrUpdateEventState(randomEventID, EventEntrantStatus.WAITLISTED, localTimestamp);

        //Register the entrant on Firebase
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantID, randomEventID, localTimestamp));

        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIDs = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIDs.contains(randomEntrantID));
    }

    @Test
    public void unregisterEntrantInEvent() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant(randomEntrantID, "Spongebob", "sponge@krustykrab.ca", "1234567890", true);
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);

        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileID()).get());
        Assert.assertTrue(snapshot.exists());


        long localTimestamp = System.currentTimeMillis() / 1000L;
        //Register the entrant
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantID, randomEventID, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        Event event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIDs = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIDs.contains(randomEntrantID));

        localTimestamp = System.currentTimeMillis() / 1000L;
        //Unregister the entrant
        Tasks.await(dbManager.unregisterEntrantInEvent(randomEntrantID, randomEventID, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        event = snapshot.toObject(Event.class);
        waitingListEntrantIDs = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertFalse(waitingListEntrantIDs.contains(randomEntrantID));
    }


    @Test
    public void verifyNoDataLoss() throws ExecutionException, InterruptedException {
        //Make new entrant
        Entrant entrant = new Entrant(randomEntrantID, "Spongebob", "sponge@krustykrab.ca", "1234567890", true);
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        Tasks.await(dbManager.addEntrant(entrant));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("profiles").document(entrant.getProfileID()).get());
        Assert.assertTrue(snapshot.exists());

        long localTimestamp = System.currentTimeMillis() / 1000L;

        //Register the entrant on Firebase
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantID, randomEventID, localTimestamp));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        Event event = snapshot.toObject(Event.class);
        ArrayList<String> waitingListEntrantIDs = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);
        Assert.assertTrue(waitingListEntrantIDs.contains(randomEntrantID));

        //Register the entrant locally
        entrant.addOrUpdateEventState(randomEventID, EventEntrantStatus.WAITLISTED, localTimestamp);

        AdminDatabaseManager adminDbManager = new AdminDatabaseManager(testDb);
        //Get the entrant
        Entrant returnedEntrant = Tasks.await(adminDbManager.getEntrantFromID(randomEntrantID));

        Assert.assertEquals(entrant, returnedEntrant);

    }













}
