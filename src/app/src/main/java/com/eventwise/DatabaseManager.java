package com.eventwise;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * The DatabaseManager class handles all transactions to Firestore.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-03
 */

//TODO:
//Uncomment Other 'setters' when other classes get made (Just want it to compile rn)
//Create Test Cases

public class DatabaseManager {

    private FirebaseFirestore db;

    private CollectionReference profiles;
    private CollectionReference events;
    private CollectionReference locations;
    private CollectionReference topics;



    public DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        profiles = db.collection("profiles");
        events = db.collection("events");
        locations = db.collection("locations");;
        topics = db.collection("topics");
    }


    //Setters
    public void addProfile(Profile profile) {

        DocumentReference profileRef = profiles.document(profile.getProfileID());
        profileRef.set(profile);
    }

    public ArrayList<Entrant> getEntrants(){

        ArrayList<Entrant> entrants_array = new ArrayList<Entrant>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Profile", document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ENTRANT.toString())){
                    entrants_array.add(document.toObject(Entrant.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return entrants_array;
    }



    public ArrayList<Admin> getAdmins(){

        ArrayList<Admin> admins_array = new ArrayList<Admin>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Profile", document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ADMIN.toString())){
                    admins_array.add(document.toObject(Admin.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return admins_array;
    }

    public ArrayList<Organizer> getOrganizers(){

        ArrayList<Organizer> organizers_array = new ArrayList<Organizer>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Profile", document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ORGANIZER.toString())){
                    organizers_array.add(document.toObject(Organizer.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return organizers_array;
    }


    public ArrayList<Event> getEvents(){
        ArrayList<Event> events_array = new ArrayList<Event>();
        events.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Events", document.getId() + " => " + document.getData());
                events_array.add(document.toObject(Event.class));
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting events: ", exception);
        });
        return events_array;
    }

    public ArrayList<Location> getLocations(){
        ArrayList<Location> locations_array = new ArrayList<Location>();
        locations.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Locations", document.getId() + " => " + document.getData());
                locations_array.add(document.toObject(Location.class));
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting locations: ", exception);
        });
        return locations_array;
    }

    public ArrayList<Topic> getTopics(){
        ArrayList<Topic> topics_array = new ArrayList<Topic>();
        topics.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("Got Topics", document.getId() + " => " + document.getData());
                topics_array.add(document.toObject(Topic.class));
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting topics: ", exception);
        });
        return topics_array;
    }




//    public void addEvent(Event event) {
//        DocumentReference eventRef = events.document(event.getEventID());
//        eventRef.set(event);
//    }
//    public void addLocation(Location location) {
//        DocumentReference locationRef = locations.document(location.getName());
//        locationRef.set(location);
//    }
//    public void addTopic(Topic topic) {
//        DocumentReference topicRef = topics.document(topic.getName());
//        topics.add(topic);
//    }

}
