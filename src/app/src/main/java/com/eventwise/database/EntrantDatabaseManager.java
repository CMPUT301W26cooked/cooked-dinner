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

    public Task<Entrant> getEntrantFromId(String entrantId) {
        return super.getProfileFromId(entrantId)
                .continueWith(task -> (Entrant) task.getResult());
    }

    public Task<Void> clearEntrantProfile(String entrantId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("EntrantId cannot be null or empty"));
            return tcs.getTask();
        }

        long timestamp = System.currentTimeMillis() / 1000L;

        profiles.document(entrantId).get()
                .addOnSuccessListener(profileSnapshot -> {
                    Entrant entrant = profileSnapshot.toObject(Entrant.class);

                    if (entrant == null) {
                        tcs.setException(new DatabaseException("Error getting Entrant"));
                        return;
                    }

                    entrant.setName("");
                    entrant.setEmail("");
                    entrant.setPhone("");

                    ArrayList<Entrant.EventStateEntry> eventStates = entrant.getEventStates();
                    if (eventStates != null) {
                        for (Entrant.EventStateEntry entry : eventStates) {
                            if (entry == null || entry.getStatus() == null) {
                                continue;
                            }

                            if (entry.getStatus() == EventEntrantStatus.ACCEPTED
                                    || entry.getStatus() == EventEntrantStatus.ENROLLED) {
                                entry.setStatus(EventEntrantStatus.CANCELLED);
                                entry.setTimestampEpochSec(timestamp);
                            }
                        }
                    }

                    events.get()
                            .addOnSuccessListener(eventSnapshots -> {
                                WriteBatch batch = db.batch();

                                batch.set(profiles.document(entrantId), entrant);

                                for (DocumentSnapshot eventSnapshot : eventSnapshots.getDocuments()) {
                                    Event event = eventSnapshot.toObject(Event.class);
                                    if (event == null || event.getEntrantStatuses() == null) {
                                        continue;
                                    }

                                    boolean changed = false;
                                    for (Event.EntrantStatusEntry entry : event.getEntrantStatuses()) {
                                        if (entry == null
                                                || entry.getEntrantProfileId() == null
                                                || entry.getStatus() == null) {
                                            continue;
                                        }

                                        if (entrantId.equals(entry.getEntrantProfileId())
                                                && (entry.getStatus() == EventEntrantStatus.ACCEPTED
                                                || entry.getStatus() == EventEntrantStatus.ENROLLED)) {
                                            entry.setStatus(EventEntrantStatus.CANCELLED);
                                            entry.setTimestampEpochSec(timestamp);
                                            changed = true;
                                        }
                                    }

                                    if (changed) {
                                        batch.set(eventSnapshot.getReference(), event);
                                    }
                                }

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
    public Task<Void> registerEntrantInEvent(String entrantId, String eventId, long timestamp) {
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
                                if (!profileSnapshot.exists()) {
                                    tcs.setException(new DatabaseException("Entrant profile does not exist for ID: " + entrantId));
                                    return;
                                }

                                Entrant entrant = profileSnapshot.toObject(Entrant.class);

                                if (entrant == null) {
                                    tcs.setException(new DatabaseException("Entrant document could not be parsed for ID: " + entrantId));
                                    return;
                                }

                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                                entrant.addOrUpdateEventState(eventId, EventEntrantStatus.WAITLISTED, timestamp);

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
     * @param entrantId The entrant to be unregistered.
     * @param eventId   The event for which the entrant is unregistering.
     * @throws DatabaseException If there is an error updating the database or if the entrant cannot be added.
     */
    public Task<Void> unregisterEntrantInEvent(String entrantId, String eventId, long timestamp) {
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

                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                                entrant.addOrUpdateEventState(eventId, EventEntrantStatus.LEFT_WAITLIST, timestamp);

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

    public Task<ArrayList<Event>> getEventsWhereEntrantIsInWaitingList(String entrantId) {
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> eventsArray = new ArrayList<>();

        events.get()
                .addOnSuccessListener(result -> {
                    for (DocumentSnapshot document : result) {
                        Event event = document.toObject(Event.class);
                        if (event != null && event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId)) {
                            eventsArray.add(event);
                        }
                    }
                    tcs.setResult(eventsArray);
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
     * @param entrantId The ID of the entrant declining the invitation
     * @param eventId   The ID of the event they are declining
     * @return Task<Void> a Task that completes when the decline action is saved
     */
    public Task<Void> declineInvitation(String entrantId, String eventId, long timestamp) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        events.document(eventId).get()
                .addOnSuccessListener(eventSnapshot -> {
                    Event event = eventSnapshot.toObject(Event.class);
                    if (event == null) {
                        tcs.setException(new DatabaseException("Error retrieving Event"));
                        return;
                    }

                    //load entrant profile
                    profiles.document(entrantId).get()
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
                                    if (e.getEntrantProfileId().equals(entrantId)) {
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
                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.DECLINED, timestamp);
                                entrant.addOrUpdateEventState(eventId, EventEntrantStatus.DECLINED, timestamp);

                                // 5. Save both: Event + Entrant
                                WriteBatch batch = super.db.batch();
                                batch.set(events.document(eventId), event);
                                batch.set(profiles.document(entrantId), entrant);

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
     * @param entrantId The ID of the entrant to delete.
     * @return Task<Void> that completes when the whole operation is done.
     */
    public Task<Void> deleteEntrant(String entrantId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            tcs.setException(new DatabaseException("EntrantId cannot be null or empty"));
            return tcs.getTask();
        }

        // 1) Try to delete the profile doc (ignore 404-like case by treating as success)
        DocumentReference profileRef = profiles.document(entrantId);

        // We chain: delete profile -> load events -> remove references -> batch commit
        profileRef.delete()
                .addOnSuccessListener(unused -> cleanEntrantFromAllEvents(entrantId, tcs))
                .addOnFailureListener(e -> {
                    // If deletion fails because doc not found, still continue to clean events.
                    // Firestore delete on non-existing doc usually resolves success,
                    // but if a failure happens (e.g., permission), we propagate the error.
                    // Here we choose to continue only if it's a "not found"-like case is not exposed.
                    // For simplicity, attempt to clean events anyway, then decide success/failure on that step.
                    cleanEntrantFromAllEvents(entrantId, tcs);
                });

        return tcs.getTask();
    }

    /**
     * Iterate all events and remove any entrant status entries for the given entrantID.
     * Commit all changes in one WriteBatch.
     */
    private void cleanEntrantFromAllEvents(String entrantId, TaskCompletionSource<Void> tcs) {
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
                            if (entrantId.equals(e.getEntrantProfileId())) {
                                removed = true;
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
