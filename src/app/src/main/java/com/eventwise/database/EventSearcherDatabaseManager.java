package com.eventwise.database;

import com.eventwise.Event;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//TODO:
// Add actual search and filter capabilities

/**
 * Database manager class specifically designed for searching and managing event data.
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-11
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

    public Task<ArrayList<Event>> getFilteredEvents(EventFilter filter){
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<ArrayList<Event>>();

        Query eventQuery = db.collection("events");

        if (filter.getFilterTypes().contains(FilterType.START_TIMESTAMP)) {
            eventQuery = eventQuery.whereGreaterThanOrEqualTo("eventStartEpochSec", filter.getStartTimestamp());
        }
        if (filter.getFilterTypes().contains(FilterType.END_TIMESTAMP)) {
            eventQuery = eventQuery.whereLessThanOrEqualTo("eventEndEpochSec", filter.getEndTimestamp());
        }
        if (filter.getFilterTypes().contains(FilterType.EVENT_CAPACITY)) {
            eventQuery = eventQuery.whereGreaterThanOrEqualTo("waitingListEmptySpots", filter.getEventCapacity());
        }
        eventQuery.get()
        .addOnSuccessListener(result -> {
            ArrayList<Event> eventsArray = new ArrayList<>();
            //Add all documents that match the previous filters to the array
            for (DocumentSnapshot document : result) {
                Event event = document.toObject(Event.class);
                if (event != null) {
                    eventsArray.add(event);
                }
            }
            //Take out events based on keywords
            Stream<Event> eventStream = eventsArray.stream();

            if (filter.getFilterTypes().contains(FilterType.TAG)) {
                eventStream = eventStream.filter(e -> e.getTags().stream()
                        .anyMatch(tag -> tag.getCategory().equals(filter.getTag().getCategory())));
            }
            if (filter.getFilterTypes().contains(FilterType.KEYWORDS)) {
                eventStream = eventStream.filter(e -> e.getTags().stream()
                        .anyMatch(tag -> filter.getKeywords().contains(tag.getKeyword())));
            }
            tcs.setResult(new ArrayList<Event>(eventStream.collect(Collectors.toList())));
        }).addOnFailureListener(e -> {
            tcs.setException(new DatabaseException("Error getting events for filtering"));
        });
        return tcs.getTask();
    }






}
