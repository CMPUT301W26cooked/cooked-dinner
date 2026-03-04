package com.eventwise;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;



//TODO:
//Uncomment Other 'setters' when other classes get made (Just want it to compile rn)
//Create 'getters'
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
