package com.eventwise.database;

import android.util.Log;

import com.eventwise.Event;
import com.eventwise.Profile;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.firebase.firestore.DocumentReference;

public class OrganizerDatabaseManager extends DatabaseManager{

    public OrganizerDatabaseManager(){
        super();
    }

    /**
     * Adds a new event to the database.
     *
     * @param event The {@link Event} object containing the details to be stored.
     * @throws DatabaseException If an error occurs while attempting to add the event to the database.
     */
    public void addEvent(Event event) throws DatabaseException {
        super.addEvent(event);
    }


}
