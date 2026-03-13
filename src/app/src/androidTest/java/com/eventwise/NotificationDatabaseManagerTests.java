package com.eventwise;

import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.NotificationDatabaseManager;
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

public class NotificationDatabaseManagerTests extends DatabaseManagerTests{

    private String randomEventID;
    private ArrayList<String> randomEntrantIDs = new ArrayList<String>();

    @Before
    public void TestCaseSetup() throws InterruptedException, ExecutionException {

        //Create Fake Entrants
        for (int i = 0; i < 10; ++i){
            Entrant entrant = new Entrant("Spongebob", "sponge@krustykrab.ca", "1234567890", true, ApplicationProvider.getApplicationContext());
            randomEntrantIDs.add(entrant.getProfileID());
            Tasks.await(new EntrantDatabaseManager(testDb).addEntrant(entrant));
        }


        //Generate RandomIDs for everything to avoid collisions
        randomEventID = UUID.randomUUID().toString();


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
        for (String entrantID : randomEntrantIDs) {
            Tasks.await(testDb.collection("profiles").document(entrantID).delete());
        }
    }



    @Test
    public void createNotification() throws ExecutionException, InterruptedException {
        Notification notification = new Notification(randomEntrantIDs, "TestOrganizerProfileID", randomEventID, Notification.NotificationType.INVITED, "Test Message");
        NotificationDatabaseManager dbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(dbManager.createNotification(notification));
        DocumentSnapshot snapshot = Tasks.await(testDb.collection("notifications").document(notification.getNotificationID()).get());
        Assert.assertTrue(snapshot.exists());
        Log.d("createNotification", "NotificationID:" + notification.getNotificationID());
    }

    @Test
    public void verifyNotificationInEntrantsNotificationList() throws ExecutionException, InterruptedException {
        //Create Notification with target entrants
        Notification notification = new Notification(randomEntrantIDs, "TestOrganizerProfileID", randomEventID, Notification.NotificationType.INVITED, "Test Message");
        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(notificationDbManager.createNotification(notification));

        //Check if each entrant has notificationID in their list
        AdminDatabaseManager adminDbManager = new AdminDatabaseManager(testDb);

        //For each entrant
        for (String entrantID : randomEntrantIDs) {
            Entrant entrant = Tasks.await(adminDbManager.getEntrantFromId(entrantID));
            Assert.assertTrue(entrant.getNotificationIDs().contains(notification.getNotificationID()));
        }
    }

    @Test
    public void verifyEntrantsInNotificationEntrantList() throws ExecutionException, InterruptedException {
        //Create Notification with target entrants
        Notification notification = new Notification(randomEntrantIDs, "TestOrganizerProfileID", randomEventID, Notification.NotificationType.INVITED, "Test Message");
        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(notificationDbManager.createNotification(notification));

        Notification returnedNotification = Tasks.await(notificationDbManager.getNotificationById(notification.getNotificationID()));

        for (String entrantID : returnedNotification.getEntrantsIDs()){
            Assert.assertTrue(randomEntrantIDs.contains(entrantID));
        }

    }

    @Test
    public void verifyNoDataLoss() throws ExecutionException, InterruptedException {
        Notification notification = new Notification(randomEntrantIDs, "TestOrganizerProfileID", randomEventID, Notification.NotificationType.INVITED, "Test Message");

        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);
        Tasks.await(notificationDbManager.createNotification(notification));
        Notification returnedNotification = Tasks.await(notificationDbManager.getNotificationById(notification.getNotificationID()));
        Assert.assertEquals(notification, returnedNotification);

    }


}
