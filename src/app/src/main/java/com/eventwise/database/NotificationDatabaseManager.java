package com.eventwise.database;

import com.eventwise.Entrant;
import com.eventwise.Notification;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
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
 *     <li>Retrieving notification data by Id.</li>
 *     <li>Creating new notifications and atomically updating associated entrant profiles.</li>
 *     <li>Fetching lists of notification Ids for a specific entrant.</li>
 *     <li>Fetching lists of entrant Ids associated with a specific notification.</li>
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

    public Task<Notification> getNotificationById(String notificationId) {
        return super.getNotificationById(notificationId);
    }

    /**
     * Creates a new notification in the database and updates all associated profiles
     * to include this notification's Id.
     *
     * This respects the profile notificationsEnabled setting.
     *
     * @param notification The {@link Notification} object to be created and linked to profiles.
     * @return A {@link Task} that resolves when the notification has been successfully created
     * and all enabled recipient profiles have been updated.
     */
    public Task<Void> createNotification(Notification notification) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (notification == null || notification.getEntrantIds() == null || notification.getEntrantIds().isEmpty()) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        ArrayList<String> requestedRecipientIds = notification.getEntrantIds();

        profiles.get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> enabledRecipientIds = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        if (!requestedRecipientIds.contains(document.getId())) {
                            continue;
                        }

                        Boolean notificationsEnabled = document.getBoolean("notificationsEnabled");
                        if (notificationsEnabled == null || notificationsEnabled) {
                            enabledRecipientIds.add(document.getId());
                        }
                    }

                    if (enabledRecipientIds.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    notification.setEntrantIds(enabledRecipientIds);

                    WriteBatch batch = super.db.batch();

                    for (String profileId : enabledRecipientIds) {
                        DocumentReference docRef = profiles.document(profileId);
                        batch.update(docRef, "notificationIds", FieldValue.arrayUnion(notification.getNotificationId()));
                    }

                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    super.addNotification(notification)
                                            .addOnSuccessListener(unused2 -> tcs.setResult(null))
                                            .addOnFailureListener(tcs::setException))
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    /**
     * Retrieves notification Ids from a profile document by profile Id.
     *
     * This works for entrant, organizer, or admin profile docs w the Firestore document notificationIds
     *
     * @param profileId The profile Id.
     * @return A {@link Task} that resolves to the list of notification Ids
     */
    public Task<ArrayList<String>> getNotificationsIdsByProfileId(String profileId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        if (profileId == null || profileId.trim().isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        profiles.document(profileId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        tcs.setResult(new ArrayList<>());
                        return;
                    }

                    ArrayList<String> notificationIds = new ArrayList<>();
                    Object rawNotificationIds = documentSnapshot.get("notificationIds");

                    if (rawNotificationIds instanceof List<?>) {
                        for (Object item : (List<?>) rawNotificationIds) {
                            if (item instanceof String) {
                                notificationIds.add((String) item);
                            }
                        }
                    }

                    tcs.setResult(notificationIds);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    /**
     * Retrieves a list of notification Ids associated with a specific entrant.
     * This method fetches the profile corresponding to the provided entrant Id and extracts
     * its associated notification identifiers.
     *
     * @param entrantId The unique identifier of the entrant whose notifications are being retrieved.
     */
    public Task<ArrayList<String>> getNotificationsIdsByEntrantId(String entrantId) {
        return getNotificationsIdsByProfileId(entrantId);
    }


    /**
     * Retrieves a list of entrant Ids associated with a specific notification.
     * This method fetches the notification object from the database using its Id
     * and extracts the list of Ids for the entrants who are recipients of that notification.
     *
     * @param notificationId The unique identifier of the notification.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the entrant Ids.
     * If the notification is not found or an error occurs, the task will fail with a {@link DatabaseException}.
     */
    public Task<ArrayList<String>> getEntrantIdsByNotificationId(String notificationId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        super.getNotificationById(notificationId)
                .addOnSuccessListener(notification -> {
                    if (notification != null) {
                        tcs.setResult(notification.getEntrantIds());
                    } else {
                        tcs.setException(new DatabaseException("Error getting EntrantIds"));
                    }
                })
                .addOnFailureListener(notUsed ->
                        tcs.setException(new DatabaseException("Error getting EntrantIds")));

        return tcs.getTask();
    }
}
