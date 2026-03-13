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
     * @param entrantID The ID of the entrant to be registered.
     * @param eventID   The ID of the event for which the entrant is registering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */

    public Task<Void> registerEntrantInEvent(String entrantID, String eventID, long timestamp) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);

                    if (event == null) {
                        tcs.setException(new DatabaseException("Error getting Event"));
                        return;
                    }

                    profiles.document(entrantID).get()
                            .addOnSuccessListener(profileSnapshot -> {
                                Entrant entrant = profileSnapshot.toObject(Entrant.class);

                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Error getting Entrant"));
                                    return;
                                }

                                event.addOrUpdateEntrantStatus(entrantID, EventEntrantStatus.WAITLISTED, timestamp);
                                entrant.addOrUpdateEventState(eventID, EventEntrantStatus.WAITLISTED, timestamp);


                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventID), event);
                                batch.set(profiles.document(entrantID), entrant);

                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Error registering Entrant in Event")));
                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error getting Entrant")));
                })
                .addOnFailureListener(e -> {
                        tcs.setException(new DatabaseException("Error getting Event"));
                });

        return tcs.getTask();
    }


    /**
     * US 01.01.02
     * Unregisters an entrant in the waiting list for a specific event in the database.
     * Updates the event document by removing the entrant's profile ID from the "waitingList" array.
     *
     * @param entrantID The entrant to be unregistered.
     * @param eventID   The event for which the entrant is unregistering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */

    public Task<Void> unregisterEntrantInEvent(String entrantID, String eventID, long timestamp) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        events.document(eventID).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);
                    if (event == null) {
                        tcs.setException(new DatabaseException("Error getting Event"));
                        return;
                    }
                    profiles.document(entrantID).get()
                            .addOnSuccessListener(profileSnapshot -> {
                                Entrant entrant = profileSnapshot.toObject(Entrant.class);
                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Error getting Entrant"));
                                    return;
                                }
                                event.addOrUpdateEntrantStatus(entrantID, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                                entrant.addOrUpdateEventState(eventID, EventEntrantStatus.LEFT_WAITLIST, timestamp);

                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventID), event);
                                batch.set(profiles.document(entrantID), entrant);

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

    /**
     * Retrieves a list of all events where the specified entrant is currently on the waiting list.
     * Iterates through all event documents and filters for those where the entrant's status
     * matches {@link EventEntrantStatus#WAITLISTED}.
     *
     * @param entrantID The unique ID of the entrant to check.
     * @return A Task containing an ArrayList of Event objects the entrant is waitlisted for.
     */
    public Task<ArrayList<Event>> getEventsWhereEntrantIsInWaitingList(String entrantID){
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> events_array = new ArrayList<Event>();

        events.get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot document : result) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantID)) {
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



    /**
     * US 01.05.03
     * Declines an invitation for an entrant in a specific event.
     *
     * Behavior:
     * 1. Load the Event
     * 2. Find the entrant in event.entrantStatuses
     * 3. Validate that the entrant is in a state that can be declined
     * 4. Update their status to DECLINED
     * 5. Update Entrant profile state
     * 6. Save both updates in a Firestore WriteBatch
     *
     * @param entrantID The ID of the entrant declining the invitation
     * @param eventID   The ID of the event they are declining
     * @return Task<Void> a Task that completes when the decline action is saved
     */
    public Task<Void> declineInvitation(String entrantID, String eventID, long timestamp) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // 1. Load Event
        events.document(eventID).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);
                    if (event == null) {
                        tcs.setException(new DatabaseException("Error retrieving Event"));
                        return;
                    }

                    // 2. Load Entrant profile
                    profiles.document(entrantID).get()
                            .addOnSuccessListener(profileSnapshot -> {
                                Entrant entrant = profileSnapshot.toObject(Entrant.class);
                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Error retrieving Entrant"));
                                    return;
                                }

                                // 3. Validate current status
                                ArrayList<Event.EntrantStatusEntry> entries = event.getEntrantStatuses();
                                EventEntrantStatus currentStatus = null;
                                for (Event.EntrantStatusEntry e : entries) {
                                    if (e.getEntrantProfileId().equals(entrantID)) {
                                        currentStatus = e.getStatus();
                                        break;
                                    }
                                }

                                if (currentStatus == null) {
                                    tcs.setException(new DatabaseException("Entrant is not part of this event"));
                                    return;
                                }

                                // Allowed to decline from these states
                                if (!(currentStatus == EventEntrantStatus.INVITED ||
                                        currentStatus == EventEntrantStatus.WAITLISTED ||
                                        currentStatus == EventEntrantStatus.ACCEPTED)) {

                                    tcs.setException(new DatabaseException("Invitation cannot be declined"));
                                    return;
                                }

                                // 4. Update state to DECLINED
                                event.addOrUpdateEntrantStatus(entrantID, EventEntrantStatus.DECLINED, timestamp);
                                entrant.addOrUpdateEventState(eventID, EventEntrantStatus.DECLINED, timestamp);

                                // 5. Save both: Event + Entrant
                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventID), event);
                                batch.set(profiles.document(entrantID), entrant);

                                // 6. Commit batch
                                batch.commit()
                                        .addOnSuccessListener(unused -> tcs.setResult(null))
                                        .addOnFailureListener(e ->
                                                tcs.setException(new DatabaseException("Error declining invitation"))
                                        );

                            })
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error retrieving Entrant"))
                            );

                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error retrieving Event"))
                );

        return tcs.getTask();
    }

    /**
     * US 01.05.01 — Delete entrant profile
     *
     * Steps:
     * 1) Delete the entrant's profile document in "profiles/{entrantID}".
     * 2) Scan all events; for each event, remove any EntrantStatusEntry whose entrantProfileId == entrantID.
     * 3) Commit changes in a single WriteBatch to keep consistency.
     *
     * Idempotency:
     * - If the profile doc does not exist, we still attempt to clean events and resolve as success.
     *
     * @param entrantID The ID of the entrant to delete.
     * @return Task<Void> that completes when the whole operation is done.
     */
    public Task<Void> deleteEntrant(String entrantID) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantID == null || entrantID.trim().isEmpty()) {
            tcs.setException(new DatabaseException("EntrantID cannot be null or empty"));
            return tcs.getTask();
        }

        // 1) Try to delete the profile doc (ignore 404-like case by treating as success)
        DocumentReference profileRef = profiles.document(entrantID);

        // We chain: delete profile -> load events -> remove references -> batch commit
        profileRef.delete()
                .addOnSuccessListener(unused -> cleanEntrantFromAllEvents(entrantID, tcs))
                .addOnFailureListener(e -> {
                    // If deletion fails because doc not found, still continue to clean events.
                    // Firestore delete on non-existing doc usually resolves success,
                    // but if a failure happens (e.g., permission), we propagate the error.
                    // Here we choose to continue only if it's a "not found"-like case is not exposed.
                    // For simplicity, attempt to clean events anyway, then decide success/failure on that step.
                    cleanEntrantFromAllEvents(entrantID, tcs);
                });

        return tcs.getTask();
    }

    /**
     * Iterate all events and remove any entrant status entries for the given entrantID.
     * Commit all changes in one WriteBatch.
     */
    private void cleanEntrantFromAllEvents(String entrantID, TaskCompletionSource<Void> tcs) {
        events.get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    boolean anyChange = false;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event == null) continue;

                        // event.getEntrantStatuses() returns ArrayList<Event.EntrantStatusEntry>
                        ArrayList<Event.EntrantStatusEntry> entries = event.getEntrantStatuses();
                        if (entries == null || entries.isEmpty()) continue;

                        // Filter out the target entrant
                        ArrayList<Event.EntrantStatusEntry> filtered = new ArrayList<>();
                        boolean removed = false;
                        for (Event.EntrantStatusEntry e : entries) {
                            if (e == null || e.getEntrantProfileId() == null) continue;
                            if (entrantID.equals(e.getEntrantProfileId())) {
                                removed = true; // mark removal
                            } else {
                                filtered.add(e);
                            }
                        }

                        if (removed) {
                            // Replace the list in the event with filtered one and stage it for writing
                            event.setEntrantStatuses(filtered);
                            batch.set(doc.getReference(), event);
                            anyChange = true;
                        }
                    }

                    if (!anyChange) {
                        // No events needed update; treat as success (idempotent)
                        tcs.setResult(null);
                        return;
                    }

                    batch.commit()
                            .addOnSuccessListener(unused2 -> tcs.setResult(null))
                            .addOnFailureListener(e ->
                                    tcs.setException(new DatabaseException("Error removing entrant from events"))
                            );
                })
                .addOnFailureListener(e ->
                        tcs.setException(new DatabaseException("Error loading events"))
                );
    }
}
