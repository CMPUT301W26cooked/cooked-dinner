package com.eventwise.database;

import android.util.Log;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import com.eventwise.Profile;

/**
 * Manages database operations specific to entrants, including event registration,
 * unregistration, and profile updates. This class extends {@link DatabaseManager}
 * to provide specialized Firestore interactions for entrant-related data.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-06
 * Updated By Becca Irving on 2026-03-09
 */

public class EntrantDatabaseManager extends DatabaseManager {


    public EntrantDatabaseManager(){
        super();
    }

    public EntrantDatabaseManager(FirebaseFirestore db){
        super(db);
    }

    /**
     * Adds a new entrant profile to the database.
     *
     * @param entrant The entrant object to be added.
     * @throws DatabaseException If an error occurs while adding the entrant to the database.
     */
    public Task<Void> addEntrant(Entrant entrant) {
        return super.addProfile(entrant);
    }


    /**
     * US 01.02.02
     * Updates the profile information for a specific entrant in the database.
     *
     * @param entrant The entrant object containing the updated information.
     * @throws DatabaseException If an error occurs during the database update process.
     */
    public Task<Void> updateEntrantInfo(Entrant entrant) {
        return super.updateProfile(entrant);
    }




    /**
     * US 01.01.01
     * Registers an entrant in the waiting list for a specific event in the database.
     * Updates the event document by adding the entrant's profile ID to the "waitingList" array.
     *
     * @param entrantId The ID of the entrant to be registered.
     * @param eventId   The ID of the event for which the entrant is registering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */

    public Task<Void> registerEntrantInEvent(String entrantId, String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Error getting Event"));
                        return;
                    }

                    profiles.document(entrantId).get()
                            .addOnSuccessListener(profileSnapshot -> {
                                Entrant entrant = profileSnapshot.toObject(Entrant.class);

                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Error getting Entrant"));
                                    return;
                                }

                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED);
                                entrant.addOrUpdateEventState(eventId, EventEntrantStatus.WAITLISTED);

                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventId), event);
                                batch.set(profiles.document(entrantId), entrant);

                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Error registering Entrant in Event")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error getting Entrant")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error getting Event")));

        return tcs.getTask();
    }
    /**
     * US 01.01.02
     * Unregisters an entrant in the waiting list for a specific event in the database.
     * Updates the event document by removing the entrant's profile ID from the "waitingList" array.
     *
     * @param entrantId The entrant to be unregistered.
     * @param eventId   The event for which the entrant is unregistering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */

    public Task<Void> unregisterEntrantInEvent(String entrantId, String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);
                    if (event == null) {
                        tcs.setException(new DatabaseException("Error getting Event"));
                        return;
                    }
                    profiles.document(entrantId).get()
                            .addOnSuccessListener(profileSnapshot -> {
                                Entrant entrant = profileSnapshot.toObject(Entrant.class);
                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Error getting Entrant"));
                                    return;
                                }
                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST);
                                entrant.addOrUpdateEventState(eventId, EventEntrantStatus.LEFT_WAITLIST);

                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventId), event);
                                batch.set(profiles.document(entrantId), entrant);

                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Could not remove Entrant from Event")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error getting Entrant")));
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error getting Event")));

        return tcs.getTask();
    }

    public Task<ArrayList<Event>> getEventsWhereEntrantIsInWaitingList(String entrantId){
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> events_array = new ArrayList<Event>();

        events.get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot document : result) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId)) {
                            events_array.add(event);
                        }
                    }
                    tcs.setResult(events_array);
                })
                .addOnFailureListener(exception -> {
                    tcs.setException(new DatabaseException("Error getting events"));
                });

        return tcs.getTask();
    }

    public Task<Profile> getEntrantProfileById(String profileId) {
        return super.getProfileFromId(profileId);
    }
}
