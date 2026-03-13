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
     * Retrieves the list of entrant IDs who are in the waiting list for a specific event.
     *
     * @param eventID The ID of the event for which to retrieve the waiting list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of waiting entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInWaitingListFromEventID(String eventID){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }


    /**
     * Retrieves the list of entrant IDs who have been cancelled for a specific event.
     *
     * @param eventID The ID of the event for which to retrieve the cancelled list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of cancelled entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInCancelledListFromEventID(String eventID){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getEntrantIdsByStatus(EventEntrantStatus.CANCELLED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }

    /**
     * Retrieves the list of entrant IDs who have been confirmed for a specific event.
     *
     * @param eventID The ID of the event for which to retrieve the confirmed list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of confirmed entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInConfirmedListFromEventID(String eventID){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }

    /**
     * Retrieves the list of entrant IDs who have been chosen for a specific event.
     *
     * @param eventID The ID of the event for which to retrieve the chosen list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of chosen entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInChosenList(String eventID){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });

        return tcs.getTask();
    }

    public Task<ArrayList<Event>> getOrganizersCreatedEventsFromOrganizerID(String organizerID){
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> events_array = new ArrayList<Event>();

        events.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("organizerProfileId").equals(organizerID)){
                    events_array.add(document.toObject(Event.class));
                }
            }
            tcs.setResult(events_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting organizers Events"));
        });
        return tcs.getTask();
    }

//**************************************************************************************************
// *                                       Event Poster Images
// *************************************************************************************************/

    
    // TODO - change local storage to Firebase
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

   
    // Updates and replace an event poster image in local storage
    public Task<String> updateEventPoster(String eventId, byte[] imageData, Context context) {
        return uploadEventPoster(eventId, imageData, context);
    }

}
