package com.eventwise.database;

import android.util.Log;

import com.eventwise.Admin;
import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Location;
import com.eventwise.Organizer;
import com.eventwise.Profile;
import com.eventwise.ProfileType;
import com.eventwise.Topic;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * The DatabaseManager class handles transaction of basic data from and to Firestore.
 * It provides methods to create, retrieve, and manage profiles, events, locations, and topics.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-03
 */

//TODO:
//Uncomment Other 'setters' when other classes get made (Just want it to compile rn)
//Create Test Cases
//Break down into multiple subclasses (for profiles, events, )

public abstract class DatabaseManager {

    private FirebaseFirestore db;

    protected CollectionReference profiles;
    protected CollectionReference events;
    protected CollectionReference locations;
    protected CollectionReference topics;



    /**
     * Constructs a new DatabaseManager and initializes the connection to Firebase Firestore.
     * This constructor sets up references to the "profiles", "events", "locations",
     * and "topics" collections.
     */
    protected DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        profiles = db.collection("profiles");
        events = db.collection("events");
        locations = db.collection("locations");;
        topics = db.collection("topics");
    }
    /**************************************************************************************************
     *                                            Profiles
     *************************************************************************************************/
    String PROFILE_TAG = "Database - Profile:";


    //Setters
    protected void addProfile(Profile profile) {

        DocumentReference profileRef = profiles.document(profile.getProfileID());
        profileRef.set(profile)
                .addOnSuccessListener(aVoid -> Log.d(PROFILE_TAG, "Profile added successfully"))
                .addOnFailureListener(e ->{
                    Log.w(PROFILE_TAG, "Error adding profile", e);
                    throw new DatabaseException("Error adding profile");
                });
    }

    /**
     * Updates the profile information of an existing entrant in the database.
     * Overwrites the document in the profiles collection with the provided entrant's data.
     *
     * @param profile The entrant object containing updated information and a valid profile ID.
     * @throws DatabaseException If there is an error communicating with the database or if the update fails.
     */
    protected void updateProfile(Profile profile) throws DatabaseException{
        DocumentReference docRef = profiles.document(profile.getProfileID());
        docRef.set(profile)
                .addOnSuccessListener(aVoid -> Log.d("EntrantDatabaseManager", "Entrant updated successfully"))
                .addOnFailureListener(e -> {
                    Log.w(PROFILE_TAG, "Error updating entrant", e);
                    throw new DatabaseException("Could not update Entrant");
                });
    }

    /**
     * Retrieves a list of all users with the profile type of ENTRANT from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Entrant} objects found in the database.
     */
    protected ArrayList<Entrant> getEntrants(){

        ArrayList<Entrant> entrants_array = new ArrayList<Entrant>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d(PROFILE_TAG, "Got Profile " + document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ENTRANT.toString())){
                    entrants_array.add(document.toObject(Entrant.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return entrants_array;
    }


    /**
     * Retrieves a list of all users with the profile type of ADMIN from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Admin} objects found in the database.
     */
    protected ArrayList<Admin> getAdmins(){

        ArrayList<Admin> admins_array = new ArrayList<Admin>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d(PROFILE_TAG, "Got Profile " + document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ADMIN.toString())){
                    admins_array.add(document.toObject(Admin.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return admins_array;
    }

    /**
     * Retrieves a list of all users with the profile type of ORGANIZER from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Organizer} objects found in the database.
     */
    protected ArrayList<Organizer> getOrganizers(){

        ArrayList<Organizer> organizers_array = new ArrayList<Organizer>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d(PROFILE_TAG, "Got Profile " + document.getId() + " => " + document.getData());
                if (document.getData().get("profileType").equals(ProfileType.ORGANIZER.toString())){
                    organizers_array.add(document.toObject(Organizer.class));
                }
            }
        }).addOnFailureListener(exception -> {
            Log.d("Error", "Error getting documents: ", exception);
        });
        return organizers_array;
    }
/**************************************************************************************************
 *                                            Events
 *************************************************************************************************/

    String EVENTS_TAG = "Database - Events:";




    /**
     * Retrieves a list of all events from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Event} objects found in the database.
     */
    protected ArrayList<Event> getEvents(){
        ArrayList<Event> events_array = new ArrayList<Event>();
        events.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Log.d("EVENTS_TAG", "Got Event " +  document.getId() + " => " + document.getData());
                events_array.add(document.toObject(Event.class));
            }
        }).addOnFailureListener(exception -> {
            Log.d("EVENTS_TAG", "Error getting events: ", exception);
        });
        return events_array;
    }

    protected void addEvent(Event event) {
        DocumentReference eventRef = events.document(event.getEventId());
        eventRef.set(event);
    }
/**************************************************************************************************
 *                                            Location
 *************************************************************************************************/
    String LOCATION_TAG = "Database - Location:";

    /**
     * Retrieves a list of all locations from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Location} objects found in the database.
     */
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

    /**************************************************************************************************
     *                                            Topic
     *************************************************************************************************/
    String TOPIC_TAG = "Database - Location:";


    /**
     * Retrieves a list of all topics from the Firestore database.
     * Note: This method initiates an asynchronous Firestore fetch; the returned list may
     * be empty initially as the data is loaded in the background.
     *
     * @return An {@link ArrayList} containing {@link Topic} objects found in the database.
     */
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





//    public void addLocation(Location location) {
//        DocumentReference locationRef = locations.document(location.getName());
//        locationRef.set(location);
//    }
//    public void addTopic(Topic topic) {
//        DocumentReference topicRef = topics.document(topic.getName());
//        topics.add(topic);
//    }

}
