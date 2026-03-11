package com.eventwise.database;

import com.eventwise.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventSearcherDatabaseManager extends DatabaseManager{

    public EventSearcherDatabaseManager(){
        super();
    }
    public EventSearcherDatabaseManager(FirebaseFirestore db){
        super(db);
    }


    public Task<ArrayList<Event>> getEvents(){
        return super.getEvents();
    }

    //This is all I touched Pablo I swear.
    public Task<Void> deleteEvent(Event event) {
        return events.document(event.getEventId()).delete();
    }

}
