package com.eventwise.database;

import com.eventwise.Event;
import com.eventwise.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


//TODO:
// Add actual search and filter capabilities

/**
 * Database manager class specifically designed for searching and managing notification data.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-22
 */
public class NotificationSearcherDataBaseManager extends DatabaseManager{

    public NotificationSearcherDataBaseManager(){

        super();
    }
    public NotificationSearcherDataBaseManager(FirebaseFirestore db){

        super(db);
    }


    /**
     * Retrieves a list of events from the database.
     *
     * @return A {@link Task} that, when complete, contains an {@link ArrayList} of {@link Event} objects.
     */
    public Task<ArrayList<Notification>> getNotifications(){

        return super.getNotifications();
    }

    /**
     * Retrieves only the notifications linked to a specific entrant profile.
     *
     * @param entrantId the entrant profile id
     * @return a Task containing only that entrant's notifications
     */
    public Task<ArrayList<Notification>> getEntrantNotifications(String entrantId) {
        TaskCompletionSource<ArrayList<Notification>> tcs = new TaskCompletionSource<>();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        NotificationDatabaseManager notificationDatabaseManager =
                new NotificationDatabaseManager(super.db);

        notificationDatabaseManager.getNotificationsIdsByEntrantId(entrantId)
                .addOnSuccessListener(notificationIds -> {
                    if (notificationIds == null || notificationIds.isEmpty()) {
                        tcs.setResult(new ArrayList<>());
                        return;
                    }

                    ArrayList<Notification> results = new ArrayList<>();
                    final int total = notificationIds.size();
                    final int[] completed = {0};

                    for (String notificationId : notificationIds) {

                        // Skip null or blank IDs so Firestore does not crash
                        if (notificationId == null || notificationId.trim().isEmpty()) {
                            completed[0]++;
                            if (completed[0] == total) {
                                tcs.setResult(results);
                            }
                            continue;
                        }

                        super.getNotificationById(notificationId)
                                .addOnSuccessListener(notification -> {
                                    if (notification != null
                                            && notification.getRecipientRole() == Notification.RecipientRole.ENTRANT) {
                                        results.add(notification);
                                    }

                                    completed[0]++;
                                    if (completed[0] == total) {
                                        tcs.setResult(results);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        tcs.setResult(results);
                                    }
                                });
                    }
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public Task<ArrayList<Notification>> getOrganizerNotifications(String entrantId) {
        TaskCompletionSource<ArrayList<Notification>> tcs = new TaskCompletionSource<>();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        NotificationDatabaseManager notificationDatabaseManager =
                new NotificationDatabaseManager(super.db);

        notificationDatabaseManager.getNotificationsIdsByEntrantId(entrantId)
                .addOnSuccessListener(notificationIds -> {
                    if (notificationIds == null || notificationIds.isEmpty()) {
                        tcs.setResult(new ArrayList<>());
                        return;
                    }

                    ArrayList<Notification> results = new ArrayList<>();
                    final int total = notificationIds.size();
                    final int[] completed = {0};

                    for (String notificationId : notificationIds) {

                        // Skip null or blank IDs so Firestore does not crash
                        if (notificationId == null || notificationId.trim().isEmpty()) {
                            completed[0]++;
                            if (completed[0] == total) {
                                tcs.setResult(results);
                            }
                            continue;
                        }

                        super.getNotificationById(notificationId)
                                .addOnSuccessListener(notification -> {
                                    if (notification != null
                                            && notification.getRecipientRole() == Notification.RecipientRole.ORGANIZER) {
                                        results.add(notification);
                                    }

                                    completed[0]++;
                                    if (completed[0] == total) {
                                        tcs.setResult(results);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        tcs.setResult(results);
                                    }
                                });
                    }
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public Task<Void> deleteNotifications(Notification notification) {
        return notifications.document(notification.getNotificationId()).delete();
    }



}
