package com.eventwise.database;


import com.eventwise.Event;
import com.eventwise.Location;
import com.eventwise.Profile;
import com.google.android.gms.tasks.Task;

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

    public AdminDatabaseManager(){
        super();
    }

    public Task<Void> removeEvent(Event event) {
        return events.document(event.getEventId()).delete();
    }

    public Task<Void> removeProfile(Profile profile) {
        return profiles.document(profile.getProfileID()).delete();
    }

//    public Task<Void> removeLocation(Location location) {
//        return locations.document(location.getName()).delete();
//    }

//    public Task<Void> removeTopics(Topics topics) {
//        return topics.document(topics.getName()).delete();
//    }





}
