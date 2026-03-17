package com.eventwise.database;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Organizer;
import com.eventwise.Profile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
/**
 * Manages database operations specific to admins, including event, profile,,
 * and location deletion. This class extends {@link DatabaseManager}
 * to provide specialized Firestore interactions for admin-related data.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-07
 */
public class AdminDatabaseManager extends DatabaseManager {

    public AdminDatabaseManager() {
        super();
    }

    public AdminDatabaseManager(FirebaseFirestore db){
        super(db);
    }

    public Task<Void> removeEvent(Event event) {
        return events.document(event.getEventId()).delete();
    }

    public Task<Void> removeProfile(Profile profile) {
        return profiles.document(profile.getProfileId()).delete();
    }

    public Task<Void> removeProfileById(String profileId) {
        return super.deleteProfileFromId(profileId);
    }

    /**
     * Retrieves an entrant's profile from the database using their unique Id.
     * This method fetches the profile data and casts it to an {@link Entrant} object.
     *
     * @param entrantId The unique identifier of the entrant to retrieve.
     * @return A {@link Task} that resolves to the {@link Entrant} object associated with the Id.
     */
    public Task<Entrant> getEntrantFromId(String entrantId) {
        return super.getProfileFromId(entrantId)
                .continueWith(task -> (Entrant) task.getResult());
    }
//    public Task<Void> removeLocation(Location location) {
//        return locations.document(location.getName()).delete();
//    }

//    public Task<Void> removeTopics(Topics topics) {
//        return topics.document(topics.getName()).delete();
//    }


    public Task<ArrayList<Event>> getAllEvents() {
        return super.getEvents();
    }


    public Task<ArrayList<Entrant>> getAllEntrants() {
        return super.getEntrants();
    }

    public Task<ArrayList<Organizer>> getAllOrganizers() {
        return super.getOrganizers();
    }
}
