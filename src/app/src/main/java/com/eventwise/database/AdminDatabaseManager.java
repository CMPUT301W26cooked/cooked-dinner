package com.eventwise.database;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Organizer;
import com.eventwise.Profile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminDatabaseManager extends DatabaseManager {

    public AdminDatabaseManager() {
        super();
    }

    public AdminDatabaseManager(FirebaseFirestore db) {
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

    public Task<Entrant> getEntrantFromId(String entrantId) {
        return super.getProfileFromId(entrantId)
                .continueWith(task -> (Entrant) task.getResult());
    }

    public Task<ArrayList<Entrant>> getAllEntrants() {
        return super.getEntrants();
    }

    public Task<ArrayList<Organizer>> getAllOrganizers() {
        return super.getOrganizers();
    }
}
