package com.eventwise.database;

import android.util.Log;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FieldValue;

/**
 * Manages database operations specific to entrants, including event registration,
 * unregistration, and profile updates. This class extends {@link DatabaseManager}
 * to provide specialized Firestore interactions for entrant-related data.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-06
 */

public class EntrantDatabaseManager extends DatabaseManager {


    public EntrantDatabaseManager(){
        super();
    }

    String ENTRANT_TAG = "Database - Entrants:";


    /**
     * Adds a new entrant profile to the database.
     *
     * @param entrant The entrant object to be added.
     * @throws DatabaseException If an error occurs while adding the entrant to the database.
     */
    public void addEntrant(Entrant entrant) throws DatabaseException {
        super.addProfile(entrant);
    }


    /**
     * US 01.02.02
     * Updates the profile information for a specific entrant in the database.
     *
     * @param entrant The entrant object containing the updated information.
     * @throws DatabaseException If an error occurs during the database update process.
     */
    public void updateEntrantInfo(Entrant entrant) throws DatabaseException{
        super.updateProfile(entrant);
    }


    /**
     * US 01.01.01
     * Registers an entrant in the waiting list for a specific event in the database.
     * Updates the event document by adding the entrant's profile ID to the "waitingList" array.
     *
     * @param entrant The entrant to be registered.
     * @param event   The event for which the entrant is registering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */
    public void registerEntrantInEvent(Entrant entrant, Event event) throws DatabaseException{
        DocumentReference docRef = events.document(event.getEventId());
        docRef.update("waitingList", FieldValue.arrayUnion(entrant.getProfileID()))
                .addOnSuccessListener(aVoid -> Log.d("EntrantDatabaseManager", "Entrant registered successfully"))
                .addOnFailureListener(e -> {
                    Log.w(ENTRANT_TAG, "Error registering entrant", e);
                    throw new DatabaseException("Could not add Entrant to Event");
            });
    }
    /**
     * US 01.01.02
     * Unregisters an entrant in the waiting list for a specific event in the database.
     * Updates the event document by removing the entrant's profile ID from the "waitingList" array.
     *
     * @param entrant The entrant to be unregistered.
     * @param event   The event for which the entrant is unregistering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */
    public void unregisterEntrantInEvent(Entrant entrant, Event event) throws DatabaseException{
        DocumentReference docRef = events.document(event.getEventId());
        docRef.update("waitingList", FieldValue.arrayRemove(entrant.getProfileID()))
                .addOnSuccessListener(aVoid -> Log.d("EntrantDatabaseManager", "Entrant unregistered successfully"))
                .addOnFailureListener(e -> {
                    Log.w(ENTRANT_TAG, "Error unregistering entrant", e);
                    throw new DatabaseException("Could not remove Entrant from Event");
                });
    }








}
