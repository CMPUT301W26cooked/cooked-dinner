package com.eventwise.database;

import com.eventwise.Entrant;
import com.eventwise.Notification;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Pablo Osorio
 * @version 1.1
 * @since 2026-03-09
 *
 * Manager class for handling database operations related to Notifications in Firestore.
 * This class provides methods to create notifications, retrieve notification details,
 * and manage the relationship between notifications and entrants.
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *     <li>Retrieving notification data by ID.</li>
 *     <li>Creating new notifications and atomically updating associated entrant profiles.</li>
 *     <li>Fetching lists of notification IDs for a specific entrant.</li>
 *     <li>Fetching lists of entrant IDs associated with a specific notification.</li>
 * </ul>
 *
 * @see Notification
 * @see Entrant
 * @see DatabaseManager
 */
public class NotificationDatabaseManager extends DatabaseManager {

    public NotificationDatabaseManager() {
        super();
    }

    public NotificationDatabaseManager(FirebaseFirestore db) {
        super(db);
    }

    public Task<Notification> getNotificationByID(String notificationID) {
        return super.getNotificationByID(notificationID);
    }

    /**
     * Creates a new notification in the database and updates all associated entrants' profiles
     * to include this notification's ID. This operation is performed atomically for the entrant updates.
     *
     * @param notification The {@link Notification} object to be created and linked to entrants.
     * @return A {@link Task} that resolves when the notification has been successfully created
     * and all entrant profiles have been updated.
     */
    public Task<Void> createNotification(Notification notification) {

        //Make a batch of writes that complete atomically
        WriteBatch batch = super.db.batch();

        for (String entrantID : notification.getEntrantIds()) {
            DocumentReference docRef = profiles.document(entrantID);
            batch.update(docRef, "notificationIDs", FieldValue.arrayUnion(notification.getNotificationId()));
        }
        // Commit the batch, then chain the addNotification operation
        return batch.commit()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw new DatabaseException("Error committing notification batch: "
                                + Objects.requireNonNull(task.getException()).getMessage());
                    }
                    return super.addNotification(notification);
                });
    }

    /**
     * Retrieves a list of notification IDs associated with a specific entrant.
     * This method fetches the profile corresponding to the provided entrant ID and extracts
     * its associated notification identifiers.
     *
     * @param entrantID The unique identifier of the entrant whose notifications are being retrieved.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the notification IDs.
     * The task will fail with a {@link DatabaseException} if the profile is not an instance of {@link Entrant}
     * or if the database retrieval fails.
     */
    public Task<ArrayList<String>> getNotificationsIDsByEntrantID(String entrantID) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        //Get the entrant
        super.getProfileFromID(entrantID)
                .addOnSuccessListener(profile -> {
                    if (profile instanceof Entrant) {
                        Entrant entrant = (Entrant) profile;
                        //Set return to Notifications of Entrant
                        tcs.setResult(entrant.getNotificationIDs());
                    } else {
                        tcs.setException(new DatabaseException("Error getting NotificationsIDs"));
                    }
                })
                .addOnFailureListener(notUsed -> {
                    tcs.setException(new DatabaseException("Error getting NotificationsIDs"));
                });
        return tcs.getTask();
    }


    /**
     * Retrieves a list of entrant IDs associated with a specific notification.
     * This method fetches the notification object from the database using its ID
     * and extracts the list of IDs for the entrants who are recipients of that notification.
     *
     * @param notificationID The unique identifier of the notification.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the entrant IDs.
     * If the notification is not found or an error occurs, the task will fail with a {@link DatabaseException}.
     */
    public Task<ArrayList<String>> getEntrantIDsByNotificationID(String notificationID) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        super.getNotificationByID(notificationID)
                .addOnSuccessListener(notification -> {
                    if (notification != null) {
                        //Set return to Entrants of Notification
                        tcs.setResult(notification.getEntrantIds());
                    } else {
                        tcs.setException(new DatabaseException("Error getting EntrantsIDs"));
                    }
                })
                .addOnFailureListener(notUsed -> {
                    tcs.setException(new DatabaseException("Error getting EntrantsIDs"));
                });
        return tcs.getTask();
    }
}

