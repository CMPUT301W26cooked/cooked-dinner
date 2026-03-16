package com.eventwise.database;

import com.eventwise.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Manager class responsible for handling database operations related to searching and managing events.
 * Extends the base {@link DatabaseManager} to provide specific event-related data access.
 */
public class EventSearcherDatabaseManager extends DatabaseManager{

    public EventSearcherDatabaseManager(){
        super();
    }
    public EventSearcherDatabaseManager(FirebaseFirestore db){
        super(db);
    }


    /**
     * Retrieves a list of events from the database.
     *
     * @return A {@link Task} that, when complete, contains an {@link ArrayList} of {@link Event} objects.
     */
    public Task<ArrayList<Event>> getEvents(){
        return super.getEvents();
    }

    //This is all I touched Pablo I swear.
    public Task<Void> deleteEvent(Event event) {
        return events.document(event.getEventId()).delete();
    }

}
