package com.eventwise.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
import com.eventwise.Profile;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
     * Adds a new event to the database.
     *
     * @param event The {@link Event} object containing the details to be stored.
     * @throws DatabaseException If an error occurs while attempting to add the event to the database.
     */
    public Task<Void> addEvent(Event event) throws DatabaseException {
        return super.addEvent(event);
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
     * Compatibility wrapper while other files are still being renamed.
     */
    public Task<ArrayList<String>> getEntrantsIDsInWaitingListFromEventID(String eventID) {
        return getEntrantsIdsInWaitingListFromEventId(eventID);
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
     * Compatibility wrapper while other files are still being renamed.
     */
    public Task<ArrayList<String>> getEntrantsIDsInCancelledListFromEventID(String eventID) {
        return getEntrantsIdsInCancelledListFromEventId(eventID);
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
     * Compatibility wrapper while other files are still being renamed.
     */
    public Task<ArrayList<String>> getEntrantsIDsInConfirmedListFromEventID(String eventID) {
        return getEntrantsIdsInConfirmedListFromEventId(eventID);
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
     * Compatibility wrapper while other files are still being renamed.
     */
    public Task<ArrayList<String>> getEntrantsIDsInChosenList(String eventID) {
        return getEntrantsIdsInChosenList(eventID);
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
                Object organizerProfileId = document.getData() == null ? null : document.getData().get("organizerProfileId");
                if (organizerProfileId != null && organizerProfileId.equals(organizerId)) {
                    eventsArray.add(document.toObject(Event.class));
                }
            }
            tcs.setResult(eventsArray);
        }).addOnFailureListener(exception ->
                tcs.setException(new DatabaseException("Error getting organizers Events")));

        return tcs.getTask();
    }

    /**
     * Compatibility wrapper while other files are still being renamed.
     */
    public Task<ArrayList<Event>> getOrganizersCreatedEventsFromOrganizerID(String organizerID) {
        return getOrganizersCreatedEventsFromOrganizerId(organizerID);
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
     * @param eventId   The unique ID of the event for which the poster is being updated.
     * @param imageData The raw byte array of the new poster image.
     * @param context   The application context used to access internal storage.
     * @return A {@link Task} that resolves to a {@link String} containing the local file path of the updated image.
     */ // Updates and replace an event poster image in local storage
    public Task<String> updateEventPoster(String eventId, byte[] imageData, Context context) {
        return uploadEventPoster(eventId, imageData, context);
    }

}
