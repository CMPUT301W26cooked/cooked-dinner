package com.eventwise.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.Notification;
import com.eventwise.Organizer;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Manages database operations specific to Organizers, including event creation,
 * and getting Organizers in lists. This class extends {@link DatabaseManager}
 * to provide specialized Firestore interactions for Organizer-related data.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-06
 * Updated By Becca Irving on 2026-03-09
 */
public class OrganizerDatabaseManager extends DatabaseManager{

    public OrganizerDatabaseManager(){
        super();
    }
    public OrganizerDatabaseManager(FirebaseFirestore db){
        super(db);
    }


    /**
     * Adds a new entrant profile to the database.
     *
     * @param organizer The entrant object to be added.
     * @throws DatabaseException If an error occurs while adding the entrant to the database.
     */
    public Task<Void> addOrganizer(Organizer organizer) {
        return super.addProfile(organizer);
    }


    /**
     * Adds a new event to the database.
     *
     * @param event The {@link Event} object containing the details to be stored.
     * @throws DatabaseException If an error occurs while attempting to add the event to the database.
     */
    public Task<Void> addEvent(Event event) throws DatabaseException {
        return super.addEvent(event);
    }

    /**
     * Updates an existing event in the database.
     *
     * @param event The {@link Event} object containing the updated details.
     * @return A {@link Task} that completes when the event is updated.
     */
    public Task<Void> updateEvent(@NonNull Event event) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        events.document(event.getEventId()).set(event)
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to update event")));

        return tcs.getTask();
    }

    //**************************************************************************************************
    // *                                            Lists
    // *************************************************************************************************/

    /**
     * Retrieves the list of entrant Ids who are in the waiting list for a specific event.
     *
     * @param eventId The Id of the event for which to retrieve the waiting list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the Ids of waiting entrants.
     */
    public Task<ArrayList<String>> getEntrantsIdsInWaitingListFromEventId(String eventId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returnedEvent = documentSnapshot.toObject(Event.class);
                    if (returnedEvent == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returnedEvent.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }

    /**
     * Retrieves the list of entrant Ids who have been cancelled for a specific event.
     *
     * @param eventId The Id of the event for which to retrieve the cancelled list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the Ids of cancelled entrants.
     */
    public Task<ArrayList<String>> getEntrantsIdsInCancelledListFromEventId(String eventId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returnedEvent = documentSnapshot.toObject(Event.class);
                    if (returnedEvent == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returnedEvent.getEntrantIdsByStatus(EventEntrantStatus.CANCELLED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }


    /**
     * Retrieves the list of entrant Ids who have been confirmed for a specific event.
     *
     * @param eventId The Id of the event for which to retrieve the confirmed list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the Ids of confirmed entrants.
     */
    public Task<ArrayList<String>> getEntrantsIdsInConfirmedListFromEventId(String eventId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returnedEvent = documentSnapshot.toObject(Event.class);
                    if (returnedEvent == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returnedEvent.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));
                    }
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error getting event data")));

        return tcs.getTask();
    }


    /**
     * Retrieves the list of entrant Ids who have been chosen for a specific event.
     *
     * @param eventId The Id of the event for which to retrieve the chosen list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the Ids of chosen entrants.
     */
    public Task<ArrayList<String>> getEntrantsIdsInChosenList(String eventId) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returnedEvent = documentSnapshot.toObject(Event.class);
                    if (returnedEvent == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returnedEvent.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
                    }
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error getting event data")));

        return tcs.getTask();
    }


    /**
     * Retrieves all events created by a specific organizer from the database.
     *
     * @param organizerId The unique identifier of the organizer whose events are to be retrieved.
     * @return A {@link Task} that resolves to an {@link ArrayList} of {@link Event} containing the events created by the organizer.
     */
    public Task<ArrayList<Event>> getOrganizersCreatedEventsFromOrganizerId(String organizerId) {
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> eventsArray = new ArrayList<>();

        events.get().addOnSuccessListener(result -> {
            for (DocumentSnapshot document : result) {
                // some events were mising their Id we need to restore the event Id for these.
                Object organizerProfileId = document.getData() == null ? null : document.getData().get("organizerProfileId");
                if (organizerProfileId != null && organizerProfileId.equals(organizerId)) {
                    Event event = document.toObject(Event.class);
                    if (event != null) {
                        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                            event.setEventId(document.getId());
                        }
                        eventsArray.add(event);
                    }
                }
            }
            tcs.setResult(eventsArray);
        }).addOnFailureListener(exception ->
                tcs.setException(new DatabaseException("Error getting organizers Events")));

        return tcs.getTask();
    }

    //**************************************************************************************************
    // *                                       Event Poster Images
    // *************************************************************************************************/


    /**
     * Saves the event poster image to local storage and updates the event's posterPath field in Firestore.
     * <p>
     * Note: Current implementation uses internal local storage. This is intended to be migrated
     * to Firebase Storage in future iterations.
     * </p>
     *
     * @param eventId   The unique identifier of the event for which the poster is being uploaded.
     * @param imageData The raw byte array of the image data to be saved.
     * @param context   The application context used to access internal file storage.
     * @return A {@link Task} that resolves to the local file path string where the image was saved.
     * @see <a href="https://stackoverflow.com/questions/3625837/android-what-is-wrong-with-openfileoutput">Reference for openFileOutput</a>
     */ // TODO - change local storage to Firebase
    // saves the event poster image to local storage and updates the event's posterPath field in Firestore 
    // reference: https://stackoverflow.com/questions/3625837/android-what-is-wrong-with-openfileoutput
    public Task<String> uploadEventPoster(String eventId, byte[] imageData, Context context) {
        String localPath = "event_posters_" + eventId + ".jpg";

        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        try {
            FileOutputStream fos = context.openFileOutput(localPath, Context.MODE_PRIVATE);
            fos.write(imageData);
            fos.close();

            super.updateEventPosterPath(eventId, localPath)
                    .addOnSuccessListener(unused -> tcs.setResult(localPath))
                    .addOnFailureListener(tcs::setException);
        } catch (IOException e) {
            tcs.setException(e);
        }

        return tcs.getTask();
    }


    /**
     * Updates and replaces an existing event poster image in local storage and updates the database reference.
     *
     * @param eventId   The unique Id of the event for which the poster is being updated.
     * @param imageData The raw byte array of the new poster image.
     * @param context   The application context used to access internal storage.
     * @return A {@link Task} that resolves to a {@link String} containing the local file path of the updated image.
     */ // Updates and replace an event poster image in local storage
    public Task<String> updateEventPoster(String eventId, byte[] imageData, Context context) {
        return uploadEventPoster(eventId, imageData, context);
    }

    public Task<Void> updateEntrantStatusInEvent(String entrantId,
                                                 String eventId,
                                                 EventEntrantStatus status,
                                                 long timestamp) {
        ArrayList<String> entrantIds = new ArrayList<>();
        entrantIds.add(entrantId);
        return setEntrantsStatusForEvent(entrantIds, eventId, status, timestamp);
    }

    /**
     * Sets many entrants to one status for one event in one shared batch flow.
     *
     * This keeps the event document and entrant profile documents in sync.
     *
     * @param entrantIds entrant ids to update
     * @param eventId event id
     * @param status new status
     * @param timestamp epoch seconds
     * @return task that completes when updates are saved
     */
    public Task<Void> setEntrantsStatusForEvent(ArrayList<String> entrantIds,
                                                String eventId,
                                                EventEntrantStatus status,
                                                long timestamp) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantIds == null || entrantIds.isEmpty()) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        if (status == null) {
            tcs.setException(new DatabaseException("Status is null"));
            return tcs.getTask();
        }

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Event not found"));
                        return;
                    }

                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(eventSnapshot.getId());
                    }

                    ArrayList<Task<DocumentSnapshot>> profileTasks = new ArrayList<>();

                    for (String entrantId : entrantIds) {
                        if (entrantId != null && !entrantId.trim().isEmpty()) {
                            profileTasks.add(profiles.document(entrantId).get());
                        }
                    }

                    if (profileTasks.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    Tasks.whenAllComplete(profileTasks)
                            .addOnSuccessListener(profileResults -> {
                                WriteBatch batch = db.batch();
                                boolean hasAnyUpdates = false;

                                for (Task<?> profileTask : profileResults) {
                                    if (!profileTask.isSuccessful()) {
                                        tcs.setException(new DatabaseException("Error getting Entrant"));
                                        return;
                                    }

                                    DocumentSnapshot profileSnapshot =
                                            (DocumentSnapshot) profileTask.getResult();

                                    if (profileSnapshot == null || !profileSnapshot.exists()) {
                                        continue;
                                    }

                                    Entrant entrant = profileSnapshot.toObject(Entrant.class);
                                    if (entrant == null) {
                                        continue;
                                    }

                                    String entrantId = profileSnapshot.getId();

                                    event.addOrUpdateEntrantStatus(entrantId, status, timestamp);
                                    entrant.addOrUpdateEventState(eventId, status, timestamp);

                                    batch.set(profileSnapshot.getReference(), entrant);
                                    hasAnyUpdates = true;
                                }

                                if (!hasAnyUpdates) {
                                    tcs.setResult(null);
                                    return;
                                }

                                batch.set(events.document(eventId), event);

                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Failed to update entrant statuses")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to load entrant profiles")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Removes many entrants from one event entirely so they return to no status.
     *
     * @param entrantIds entrant ids to remove
     * @param eventId event id
     * @return task that completes when removals are saved
     */
    public Task<Void> removeEntrantsFromEvent(ArrayList<String> entrantIds, String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantIds == null || entrantIds.isEmpty()) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Event not found"));
                        return;
                    }

                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(eventSnapshot.getId());
                    }

                    ArrayList<Task<DocumentSnapshot>> profileTasks = new ArrayList<>();

                    for (String entrantId : entrantIds) {
                        if (entrantId != null && !entrantId.trim().isEmpty()) {
                            profileTasks.add(profiles.document(entrantId).get());
                        }
                    }

                    if (profileTasks.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    Tasks.whenAllComplete(profileTasks)
                            .addOnSuccessListener(profileResults -> {
                                WriteBatch batch = db.batch();
                                boolean hasAnyUpdates = false;

                                for (Task<?> profileTask : profileResults) {
                                    if (!profileTask.isSuccessful()) {
                                        tcs.setException(new DatabaseException("Error getting Entrant"));
                                        return;
                                    }

                                    DocumentSnapshot profileSnapshot =
                                            (DocumentSnapshot) profileTask.getResult();

                                    if (profileSnapshot == null || !profileSnapshot.exists()) {
                                        continue;
                                    }

                                    Entrant entrant = profileSnapshot.toObject(Entrant.class);
                                    if (entrant == null) {
                                        continue;
                                    }

                                    String entrantId = profileSnapshot.getId();

                                    event.removeEntrantStatus(entrantId);
                                    entrant.removeEventState(eventId);

                                    batch.set(profileSnapshot.getReference(), entrant);
                                    hasAnyUpdates = true;
                                }

                                if (!hasAnyUpdates) {
                                    tcs.setResult(null);
                                    return;
                                }

                                batch.set(events.document(eventId), event);

                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Failed to remove entrants from event")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to load entrant profiles")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Reconciles private event invite selection with the selected entrant ids.
     *
     * Selected entrants are invited if they are not already invited or enrolled.
     * Unselected invited or enrolled entrants are removed back to no status.
     *
     * @param eventId event id
     * @param selectedEntrantIds selected entrant ids
     * @return task that completes when the event matches the selection
     */
    public Task<Void> syncPrivateEventSelection(String eventId, ArrayList<String> selectedEntrantIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        getEventById(eventId)
                .addOnSuccessListener(event -> {
                    ArrayList<String> selectedIds = selectedEntrantIds == null
                            ? new ArrayList<>()
                            : new ArrayList<>(selectedEntrantIds);

                    ArrayList<String> currentSelectedIds = new ArrayList<>();
                    currentSelectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
                    currentSelectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));

                    ArrayList<String> idsToInvite = new ArrayList<>();
                    for (String selectedId : selectedIds) {
                        if (!currentSelectedIds.contains(selectedId)) {
                            idsToInvite.add(selectedId);
                        }
                    }

                    ArrayList<String> idsToRemove = new ArrayList<>();
                    for (String currentId : currentSelectedIds) {
                        if (!selectedIds.contains(currentId)) {
                            idsToRemove.add(currentId);
                        }
                    }

                    ArrayList<Task<Void>> tasks = new ArrayList<>();

                    if (!idsToInvite.isEmpty()) {
                        tasks.add(inviteEntrants(eventId, idsToInvite));
                    }

                    if (!idsToRemove.isEmpty()) {
                        tasks.add(removeEntrantsFromEvent(idsToRemove, eventId));
                    }

                    if (tasks.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener(results -> {
                                for (Task<?> task : results) {
                                    if (!task.isSuccessful()) {
                                        tcs.setException(new DatabaseException("Failed to sync private event selection"));
                                        return;
                                    }
                                }
                                tcs.setResult(null);
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to sync private event selection")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Returns one event by id and restores the Firestore document id if needed.
     *
     * @param eventId event id
     * @return matching event
     */
    public Task<Event> getEventById(String eventId) {
        TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();

        if (eventId == null || eventId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Event not found"));
                        return;
                    }

                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(eventSnapshot.getId());
                    }

                    tcs.setResult(event);
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Manually puts entrants onto the waitlist for one event.
     */
    public Task<Void> waitlistEntrants(String eventId, ArrayList<String> entrantIds) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.WAITLISTED, timestamp);
    }

    /**
     * Manually removes entrants from the waitlist for one event.
     */
    public Task<Void> removeWaitlistEntrants(String eventId, ArrayList<String> entrantIds) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
    }

    /**
     * Manually invites entrants for one event and sends the selected notification.
     */
    public Task<Void> inviteEntrants(String eventId, ArrayList<String> entrantIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        getEventById(eventId)
                .addOnSuccessListener(event -> {
                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.INVITED, timestamp)
                            .addOnSuccessListener(unused ->
                                    sendEntrantNotification(
                                            entrantIds,
                                            event,
                                            Notification.NotificationType.INVITED,
                                            "You Were Selected",
                                            "You were selected for " + event.getName() + ". Please accept or decline your invitation."
                                    ).addOnSuccessListener(unused2 -> tcs.setResult(null))
                                            .addOnFailureListener(e -> {
                                                Log.e("OrganizerDB", "Failed to send invite notifications", e);
                                                tcs.setResult(null);
                                            })
                            )
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to invite entrants")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Manually removes invited entrants and sends an invitation cancelled notification.
     */
    public Task<Void> removeInviteEntrants(String eventId, ArrayList<String> entrantIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        getEventById(eventId)
                .addOnSuccessListener(event -> {
                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.CANCELLED, timestamp)
                            .addOnSuccessListener(unused ->
                                    sendEntrantNotification(
                                            entrantIds,
                                            event,
                                            Notification.NotificationType.CANCELLED,
                                            "Invitation Cancelled",
                                            "Your invitation for " + event.getName() + " was cancelled."
                                    ).addOnSuccessListener(unused2 -> tcs.setResult(null))
                                            .addOnFailureListener(e -> {
                                                Log.e("OrganizerDB", "Failed to send invitation cancelled notifications", e);
                                                tcs.setResult(null);
                                            })
                            )
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to remove invited entrants")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Manually enrolls entrants and sends a notification.
     */
    public Task<Void> enrollEntrants(String eventId, ArrayList<String> entrantIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        getEventById(eventId)
                .addOnSuccessListener(event -> {
                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.ENROLLED, timestamp)
                            .addOnSuccessListener(unused ->
                                    sendEntrantNotification(
                                            entrantIds,
                                            event,
                                            Notification.NotificationType.OTHER,
                                            "You Are Enrolled",
                                            "You are enrolled in " + event.getName() + "."
                                    ).addOnSuccessListener(unused2 -> tcs.setResult(null))
                                            .addOnFailureListener(e -> {
                                                Log.e("OrganizerDB", "Failed to send enrolled notifications", e);
                                                tcs.setResult(null);
                                            })
                            )
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to enroll entrants")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Manually removes enrolled entrants and sends a cancellation notification.
     */
    public Task<Void> removeEnrollEntrants(String eventId, ArrayList<String> entrantIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        getEventById(eventId)
                .addOnSuccessListener(event -> {
                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(entrantIds, eventId, EventEntrantStatus.CANCELLED, timestamp)
                            .addOnSuccessListener(unused ->
                                    sendEntrantNotification(
                                            entrantIds,
                                            event,
                                            Notification.NotificationType.CANCELLED,
                                            "Enrollment Cancelled",
                                            "Your enrollment for " + event.getName() + " was cancelled."
                                    ).addOnSuccessListener(unused2 -> tcs.setResult(null))
                                            .addOnFailureListener(e -> {
                                                Log.e("OrganizerDB", "Failed to send enrollment cancelled notifications", e);
                                                tcs.setResult(null);
                                            })
                            )
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to remove enrolled entrants")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Draws from the current waitlist and moves selected entrants to invited.
     *
     * This fills as many open spots as possible. Entrants not selected remain
     * waitlisted, but can still receive a "not selected this draw" notification.
     *
     * @param eventId event id
     * @return task that completes when the draw is done
     */
    public Task<Void> drawEntrants(String eventId) {
        return drawFromWaitlist(eventId, true);
    }

    /**
     * Re-draws from the current waitlist to fill remaining open spots.
     *
     * @param eventId event id
     * @return task that completes when the re-draw is done
     */
    public Task<Void> redrawEntrants(String eventId) {
        return drawFromWaitlist(eventId, false);
    }

    /**
     * Cancels all currently invited entrants for one event.
     *
     * @param eventId event id
     * @return task that completes when invitees are cancelled
     */
    public Task<Void> cancelInvitees(String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (eventId == null || eventId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Event not found"));
                        return;
                    }

                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(eventSnapshot.getId());
                    }

                    ArrayList<String> invitedIds = event.getEntrantIdsByStatus(EventEntrantStatus.INVITED);

                    if (invitedIds.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(invitedIds, eventId, EventEntrantStatus.CANCELLED, timestamp)
                            .addOnSuccessListener(unused -> {
                                sendEntrantNotification(
                                        invitedIds,
                                        event,
                                        Notification.NotificationType.CANCELLED,
                                        "Invitation Cancelled",
                                        "Your invitation for " + event.getName() + " was cancelled."
                                ).addOnSuccessListener(unused2 -> tcs.setResult(null))
                                        .addOnFailureListener(e -> {
                                            Log.e("OrganizerDB", "Failed to send invitation cancelled notifications", e);
                                            tcs.setResult(null);
                                        });
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to cancel invitees")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Shared draw helper used by draw and re-draw.
     *
     * @param eventId event id
     * @param notifyNonSelected true for initial draw, false for re-draw
     * @return task that completes when draw work is done
     */
    private Task<Void> drawFromWaitlist(String eventId, boolean notifyNonSelected) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (eventId == null || eventId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("Event Id is null"));
            return tcs.getTask();
        }

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Event not found"));
                        return;
                    }

                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(eventSnapshot.getId());
                    }

                    ArrayList<String> waitlistedIds = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);

                    int currentlyInvitedCount = event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).size();
                    int currentlyEnrolledCount = event.getEnrolledCount();
                    int openSpots = event.getMaxWinnersToSample() - currentlyInvitedCount - currentlyEnrolledCount;

                    if (openSpots <= 0 || waitlistedIds.isEmpty()) {
                        tcs.setResult(null);
                        return;
                    }

                    Collections.shuffle(waitlistedIds);

                    int selectedCount = Math.min(openSpots, waitlistedIds.size());

                    ArrayList<String> selectedIds = new ArrayList<>(waitlistedIds.subList(0, selectedCount));
                    ArrayList<String> notSelectedIds = new ArrayList<>();

                    if (selectedCount < waitlistedIds.size()) {
                        notSelectedIds.addAll(waitlistedIds.subList(selectedCount, waitlistedIds.size()));
                    }

                    long timestamp = System.currentTimeMillis() / 1000L;

                    setEntrantsStatusForEvent(selectedIds, eventId, EventEntrantStatus.INVITED, timestamp)
                            .addOnSuccessListener(unused -> {
                                ArrayList<Task<Void>> notificationTasks = new ArrayList<>();

                                notificationTasks.add(sendEntrantNotification(
                                        selectedIds,
                                        event,
                                        Notification.NotificationType.INVITED,
                                        "You Were Selected",
                                        "You were selected for " + event.getName() + ". Please accept or decline your invitation."
                                ));

                                if (notifyNonSelected && !notSelectedIds.isEmpty()) {
                                    notificationTasks.add(sendEntrantNotification(
                                            notSelectedIds,
                                            event,
                                            Notification.NotificationType.NOT_CHOSEN,
                                            "Not Selected This Draw",
                                            "You were not selected in this draw for " + event.getName() + ". You are still on the waitlist."
                                    ));
                                }

                                Tasks.whenAllComplete(notificationTasks)
                                        .addOnSuccessListener(tasks -> {
                                            for (Task<?> task : tasks) {
                                                if (!task.isSuccessful()) {
                                                    Log.e("OrganizerDB", "A draw notification failed", task.getException());
                                                }
                                            }
                                            tcs.setResult(null);
                                        })
                                        .addOnFailureListener(e -> tcs.setResult(null));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Failed to draw entrants")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Failed to load event")));

        return tcs.getTask();
    }

    /**
     * Sends one notification to many entrant profiles.
     *
     * Notification delivery still respects notificationsEnabled because that filtering
     * happens inside NotificationDatabaseManager.createNotification().
     *
     * @param recipientIds recipient profile ids
     * @param event event context
     * @param type notification type
     * @param title notification title
     * @param message notification message
     * @return task that completes when notification work is attempted
     */
    private Task<Void> sendEntrantNotification(ArrayList<String> recipientIds,
                                               Event event,
                                               Notification.NotificationType type,
                                               String title,
                                               String message) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (recipientIds == null || recipientIds.isEmpty()) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setRecipientRole(Notification.RecipientRole.ENTRANT);
        notification.setEntrantIds(recipientIds);
        notification.setOrganizerId(event.getOrganizerProfileId());
        notification.setEventId(event.getEventId());
        notification.setMessageTitle(title);
        notification.setMessageBody(message);
        notification.setType(type);
        notification.setTimestamp(System.currentTimeMillis() / 1000L);

        NotificationDatabaseManager notificationDatabaseManager =
                new NotificationDatabaseManager(super.db);

        return notificationDatabaseManager.createNotification(notification);
    }

    public Task<Organizer> getOrganizerFromId(String organizerId) {
        return super.getProfileFromId(organizerId)
                .continueWith(task -> (Organizer) task.getResult());
    }

    public Task<Void> updateOrganizerInfo(Organizer organizer) {
        return super.updateProfile(organizer);
    }

    public Task<Void> clearOrganizerProfile(String organizerId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (organizerId == null || organizerId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("EntrantId cannot be null or empty"));
            return tcs.getTask();
        }

        long timestamp = System.currentTimeMillis() / 1000L;

        profiles.document(organizerId).get()
                .addOnSuccessListener(profileSnapshot -> {
                    Organizer organizer = profileSnapshot.toObject(Organizer.class);

                    if (organizer == null) {
                        tcs.setException(new DatabaseException("Error getting Entrant"));
                        return;
                    }

                    organizer.setName("");
                    organizer.setEmail("");
                    organizer.setPhone("");


                    events.get()
                            .addOnSuccessListener(eventSnapshots -> {
                                WriteBatch batch = db.batch();

                                batch.set(profiles.document(organizerId), organizer);
                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Error clearing Entrant profile")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error loading events")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error getting Entrant")));

        return tcs.getTask();
    }
}
