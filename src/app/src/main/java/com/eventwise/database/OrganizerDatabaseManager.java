package com.eventwise.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Profile;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
/**
 * Manages database operations specific to entrants, including event creation,
 * and getting entrants in lists. This class extends {@link DatabaseManager}
 * to provide specialized Firestore interactions for organizer-related data.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-06
 */
public class OrganizerDatabaseManager extends DatabaseManager{

    public OrganizerDatabaseManager(){
        super();
    }

    String ORGANIZER_TAG = "Database - Organizer:";

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
     * @param event The {@link Event} object for which to retrieve the waiting list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of waiting entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInWaitingList(Event event){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(event.getEventId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                            Event returned_event = documentSnapshot.toObject(Event.class);
                            if (returned_event == null || returned_event.getWaitingListEntrantIds() == null) {
                                tcs.setResult(new ArrayList<>());
                            } else {
                                tcs.setResult(returned_event.getWaitingListEntrantIds());
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
     * @param event The {@link Event} object for which to retrieve the cancelled list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of cancelled entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInCancelledList(Event event){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(event.getEventId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null || returned_event.getCancelledEntrantIds() == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getCancelledEntrantIds());
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
     * @param event The {@link Event} object for which to retrieve the confirmed list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of confirmed entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInConfirmedList(Event event){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(event.getEventId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null || returned_event.getConfirmedEntrantIds() == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getConfirmedEntrantIds());
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
     * @param event The {@link Event} object for which to retrieve the chosen list.
     * @return A {@link Task} that resolves to an {@link ArrayList} of strings containing the IDs of chosen entrants.
     */
    public Task<ArrayList<String>> getEntrantsIDsInChosenList(Event event){
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();

        events.document(event.getEventId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event returned_event = documentSnapshot.toObject(Event.class);
                    if (returned_event == null || returned_event.getChosenEntrantIds() == null) {
                        tcs.setResult(new ArrayList<>());
                    } else {
                        tcs.setResult(returned_event.getChosenEntrantIds());
                    }
                })
                .addOnFailureListener(e -> {
                    tcs.setException(new DatabaseException("Error getting event data"));
                });
        return tcs.getTask();
    }










}
