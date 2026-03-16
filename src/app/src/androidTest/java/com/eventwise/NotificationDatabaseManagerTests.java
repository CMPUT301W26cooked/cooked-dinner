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

    private String randomEventId;
    private ArrayList<String> randomEntrantIds = new ArrayList<>();

    @Before
    public void TestCaseSetup() throws InterruptedException, ExecutionException {
        //Create Fake Entrants
        for (int i = 0; i < 10; ++i) {
            Entrant entrant = new Entrant(
                    "Spongebob",
                    "sponge@krustykrab.ca",
                    "1234567890",
                    true,
                    ApplicationProvider.getApplicationContext()
            );
            randomEntrantIds.add(entrant.getProfileId());
            Tasks.await(new EntrantDatabaseManager(testDb).addEntrant(entrant));
        }

        //Generate RandomIds for everything to avoid collisions
        randomEventId = UUID.randomUUID().toString();

        //Make a dummy event to add the entrant to
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
                null);

        event.setEventId(randomEventId);
        organizerDbManager.addEvent(event);
        Thread.sleep(2000);




    }

    @After
    public void deleteTestData() throws ExecutionException, InterruptedException {
        Tasks.await(testDb.collection("events").document(randomEventId).delete());
        for (String entrantId : randomEntrantIds) {
            Tasks.await(testDb.collection("profiles").document(entrantId).delete());
        }
    }



    @Test
    public void createNotification() throws ExecutionException, InterruptedException {
        Notification notification = new Notification(
                randomEntrantIds,
                "TestOrganizerProfileId",
                randomEventId,
                Notification.NotificationType.INVITED,
                "Test Message"
        );
        NotificationDatabaseManager dbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(dbManager.createNotification(notification));
        DocumentSnapshot snapshot = Tasks.await(
                testDb.collection("notifications").document(notification.getNotificationId()).get()
        );
        Assert.assertTrue(snapshot.exists());
        Log.d("createNotification", "NotificationId:" + notification.getNotificationId());
    }

    @Test
    public void verifyNotificationInEntrantsNotificationList() throws ExecutionException, InterruptedException {
        Notification notification = new Notification(
                randomEntrantIds,
                "TestOrganizerProfileId",
                randomEventId,
                Notification.NotificationType.INVITED,
                "Test Message"
        );
        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(notificationDbManager.createNotification(notification));

        AdminDatabaseManager adminDbManager = new AdminDatabaseManager(testDb);

        for (String entrantId : randomEntrantIds) {
            Entrant entrant = Tasks.await(adminDbManager.getEntrantFromId(entrantId));
            Assert.assertTrue(entrant.getNotificationIds().contains(notification.getNotificationId()));
        }
    }

    @Test
    public void verifyEntrantsInNotificationEntrantList() throws ExecutionException, InterruptedException {
        //Create Notification with target entrants
        Notification notification = new Notification(
                randomEntrantIds,
                "TestOrganizerProfileId",
                randomEventId,
                Notification.NotificationType.INVITED,
                "Test Message"
        );
        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);

        Tasks.await(notificationDbManager.createNotification(notification));

        Notification returnedNotification = Tasks.await(
                notificationDbManager.getNotificationById(notification.getNotificationId())
        );

        for (String entrantId : returnedNotification.getEntrantsIds()) {
            Assert.assertTrue(randomEntrantIds.contains(entrantId));
        }

    }

    @Test
    public void verifyNoDataLoss() throws ExecutionException, InterruptedException {
        Notification notification = new Notification(
                randomEntrantIds,
                "TestOrganizerProfileId",
                randomEventId,
                Notification.NotificationType.INVITED,
                "Test Message"
        );

        NotificationDatabaseManager notificationDbManager = new NotificationDatabaseManager(testDb);
        Tasks.await(notificationDbManager.createNotification(notification));
        Notification returnedNotification = Tasks.await(
                notificationDbManager.getNotificationById(notification.getNotificationId())
        );
        Assert.assertEquals(notification, returnedNotification);

    }


}
