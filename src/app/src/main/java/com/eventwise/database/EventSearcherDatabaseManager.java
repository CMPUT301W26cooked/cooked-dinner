package com.eventwise.database;

import android.util.Log;

import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//TODO:
// Documentation and Tests

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
//        if (filter.getFilterTypes().contains(FilterType.EVENT_CAPACITY)) {
//            eventQuery = eventQuery.whereGreaterThanOrEqualTo("waitingListEmptySpots", filter.getEventCapacity());
//        }
        eventQuery.get()
        .addOnSuccessListener(result -> {
            ArrayList<Event> eventsArray = new ArrayList<>();
            //Add all documents that match the previous filters to the array
            for (DocumentSnapshot document : result) {
                Event event = document.toObject(Event.class);
                if (event != null) {
                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(document.getId());
                    }
                    eventsArray.add(event);
                }
            }

            //Remove all events that dont have enough spots
             if (filter.getFilterTypes().contains(FilterType.EVENT_CAPACITY)){
                 //ignore (outdated) events with a null waitingListEmptySpots variable
                 Iterator<Event> iterator = eventsArray.iterator();
                 while (iterator.hasNext()) {
                     Event event = iterator.next();
                     if (event.getWaitingListEmptySpots() != null && event.getWaitingListEmptySpots() < filter.getEventCapacity()) {
                         iterator.remove();
                     }
                 }
//                eventsArray.removeIf(event -> (event.getWaitingListEmptySpots() < filter.getEventCapacity()) || event.getWaitingListEmptySpots() != null);
            }

            //Take out events based on keywords
            Stream<Event> eventStream = eventsArray.stream();

            if (filter.getFilterTypes().contains(FilterType.TAG)) {
                eventStream = eventStream.filter(e -> e.getTags().stream()
                        .anyMatch(tag -> tag.getCategory().equals(filter.getTag().getCategory())));
            }
            //Search for keywords in a title string
            if (filter.getFilterTypes().contains(FilterType.KEYWORDS)) {
                eventStream = eventStream.filter(event -> filter.getKeywords().stream()
                        .anyMatch(keyword -> event.getName().toLowerCase()
                                //Match whole words only (the .*\\b is word boundaries)
                                .matches(".*\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b.*")));
            }
            tcs.setResult(new ArrayList<Event>(eventStream.collect(Collectors.toList())));
        }).addOnFailureListener(e -> {
            Log.e("Event", "Failed to get events", e);
            tcs.setException(new DatabaseException("Error getting events for filtering", e));
        });
        return tcs.getTask();
    }






}
