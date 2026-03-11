package com.eventwise.database;


import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Location;
import com.eventwise.Profile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public AdminDatabaseManager(FirebaseFirestore db){
        super(db);
    }

    public Task<Void> removeEvent(Event event) {
        return events.document(event.getEventId()).delete();
    }

    public interface RemoveCallback {
        void onComplete(boolean success);
    }

    public void removeProfile(String profileId, RemoveCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("profiles")
                .document(profileId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }
    public Task<Entrant> getEntrantFromID(String entrantID) {
        return super.getProfileFromID(entrantID)
                .continueWith(task -> (Entrant) task.getResult());
    }

}
