package com.eventwise;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import com.eventwise.database.EntrantDatabaseManager;
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

import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class EntrantDatabaseManagerTests {


    static FirebaseFirestore testDb;
    private String randomEventID;
    private String randomEntrantID;



    @BeforeClass
    public static void FirebaseSetup() {
        Context context = ApplicationProvider.getApplicationContext();
//        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId("cooked-dinner-test")
                    .setApplicationId("1:511463316367:android:6b69ce8f5da3f0c4aac9fa")
                    .setApiKey("AIzaSyDAJGtkpLkM9-zbQT4sOsAX0LMhuD9AZog")
                    .build();
                FirebaseApp.initializeApp(context, options, "test");
//        }

        testDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("test"));



    }

    @Before
    public void TestCaseSetup() throws InterruptedException {

        //Generate RandomIDs for everything to avoid collisions
        randomEventID = UUID.randomUUID().toString();
        randomEntrantID = UUID.randomUUID().toString();


        //Make a dummy event to add the entrant to
        OrganizerDatabaseManager organizerDbManager = new OrganizerDatabaseManager(testDb);

        Event event = new Event(
                "TestOrganizerProfileID",
                "Test Event",
                "Test Description",
                10.0,
                "Test Location",
                "Test Topic",
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
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        DocumentSnapshot snapshot;
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantID, randomEventID));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        Event event = snapshot.toObject(Event.class);
        Assert.assertTrue(event.getWaitingListEntrantIds().contains(randomEntrantID));
    }

    @Test
    public void unregisterEntrantInEvent() throws ExecutionException, InterruptedException {
        //Register the entrant
        EntrantDatabaseManager dbManager = new EntrantDatabaseManager(testDb);
        DocumentSnapshot snapshot;
        Tasks.await(dbManager.registerEntrantInEvent(randomEntrantID, randomEventID));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        Event event = snapshot.toObject(Event.class);
        Assert.assertTrue(event.getWaitingListEntrantIds().contains(randomEntrantID));

        //Unregister the entrant
        Tasks.await(dbManager.unregisterEntrantInEvent(randomEntrantID, randomEventID));
        snapshot = Tasks.await(testDb.collection("events").document(randomEventID).get());
        event = snapshot.toObject(Event.class);
        Assert.assertFalse(event.getWaitingListEntrantIds().contains(randomEntrantID));
    }













}
