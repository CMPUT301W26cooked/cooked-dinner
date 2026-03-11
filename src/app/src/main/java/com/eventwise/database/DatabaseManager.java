package com.eventwise.database;

import android.provider.ContactsContract;
import android.util.Log;

import com.eventwise.Admin;
import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Location;
import com.eventwise.Notification;
import com.eventwise.Organizer;
import com.eventwise.Profile;
import com.eventwise.ProfileType;
import com.eventwise.Tag;
import com.eventwise.database.exceptions.DatabaseException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * The DatabaseManager class handles transaction of basic data from and to Firestore.
 * It provides methods to create, retrieve, and manage profiles, events, locations, and topics.
 * This class handles basic operations that the child classes use for more complex operations.
 *
 * @author Pablo Osorio
 * @version 1.1
 * @since 2026-03-03
 * Updated By Becca Irving on 2026-03-09
 */

//TODO:
//Uncomment Other 'setters' when other classes get made (Just want it to compile rn)
//Create Test Cases

public abstract class DatabaseManager {

    protected FirebaseFirestore db;

    protected CollectionReference profiles;
    protected CollectionReference events;
    protected CollectionReference locations;
    protected CollectionReference tags;
    protected CollectionReference notifications;




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
        tags = db.collection("tags");
        notifications = db.collection("notifications");
    }

    /**
     * Constructs a new DatabaseManager and initializes the connection to Firebase Firestore.
     * This constructor sets up references to the "profiles", "events", "locations",
     * and "topics" collections. Required for using testing database.
     */
   protected DatabaseManager(FirebaseFirestore db) {
        this.db = db;
        profiles = db.collection("profiles");
        events = db.collection("events");
        locations = db.collection("locations");;
        tags = db.collection("tags");
        notifications = db.collection("notifications");
   }


    //**************************************************************************************************
    // *                                            Profiles
    // *************************************************************************************************/

    /**
     * Adds a new profile to the Firestore database.
     * This method creates a document in the "profiles" collection using the profile's unique ID
     * and stores the provided profile object.
     *
     * @param profile The {@link Profile} object containing the data to be stored.
     * @return A {@link Task} representing the asynchronous database write operation.
     */
    protected Task<Void> addProfile(Profile profile) {
        return profiles.document(profile.getProfileID()).set(profile);
    }


    /**
     * Updates an existing profile in the database.
     * This method first verifies that a profile with the given ID exists before attempting to
     * overwrite it. If the profile does not exist, the returned task will fail with a
     * {@link DatabaseException}.
     *
     * @param profile The {@link Profile} object containing the updated information and a valid profile ID.
     * @return A {@link Task} that will be completed when the update is successful.
     */
    protected Task<Void> updateProfile(Profile profile) {
        return profiles.document(profile.getProfileID()).get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                return Tasks.forException(new DatabaseException("Profile does not exist"));
            }
            return profiles.document(profile.getProfileID()).set(profile);
        });
    }

    protected Task<Void> deleteProfileFromID(String profileID) {
        return profiles.document(profileID).delete();
    }



    /**
     * Retrieves a profile from the Firestore database using the specified profile ID.
     * This method performs an asynchronous fetch and returns a Task that will resolve
     * to the Profile object if found.
     *
     * @param profileID The unique identifier of the profile to retrieve.
     * @return A {@link Task} that will contain the {@link Profile} object upon success,
     *         or a {@link DatabaseException} if the profile does not exist or the fetch fails.
     */
    protected Task<Profile> getProfileFromID(String profileID) {
        TaskCompletionSource<Profile> tcs = new TaskCompletionSource<>();

        profiles.document(profileID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.getData().get("profileType").equals(ProfileType.ENTRANT.toString())){
                    tcs.setResult(documentSnapshot.toObject(Entrant.class));
                }
                else if (documentSnapshot.getData().get("profileType").equals(ProfileType.ORGANIZER.toString())) {
                    tcs.setResult(documentSnapshot.toObject(Organizer.class));
                }
                else if (documentSnapshot.getData().get("profileType").equals(ProfileType.ADMIN.toString())) {
                    tcs.setResult(documentSnapshot.toObject(Admin.class));
                }

            } else {
                tcs.setException(new DatabaseException("Error getting Profile"));
            }
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }



    /**
     * Asynchronously retrieves a list of all profiles from the database that have the
     * profile type of {@link ProfileType#ENTRANT}.
     * <p>
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Entrant} objects.
     * @see Entrant
     * @see ProfileType
     */
    protected Task<ArrayList<Entrant>> getEntrants(){

        TaskCompletionSource<ArrayList<Entrant>> tcs = new TaskCompletionSource<>();
        ArrayList<Entrant> entrants_array = new ArrayList<Entrant>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ENTRANT.toString())){
                    entrants_array.add(document.toObject(Entrant.class));
                }
            }
            tcs.setResult(entrants_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting Entrants"));
        });
        return tcs.getTask();
    }


    /**
     * Asynchronously retrieves a list of all profiles from the database that have the
     * profile type of {@link ProfileType#ADMIN}.
     * <p>
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Admin} objects.
     * @see Admin
     * @see ProfileType
     */
    protected Task<ArrayList<Admin>> getAdmins(){

        TaskCompletionSource<ArrayList<Admin>> tcs = new TaskCompletionSource<>();
        ArrayList<Admin> admin_array = new ArrayList<Admin>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ADMIN.toString())){
                    admin_array.add(document.toObject(Admin.class));
                }
            }
            tcs.setResult(admin_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting Admins"));
        });
        return tcs.getTask();
    }

    /**
     * Asynchronously retrieves a list of all profiles from the database that have the
     * profile type of {@link ProfileType#ORGANIZER}.
     * <p>
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Organizer} objects.
     * @see Organizer
     * @see ProfileType
     */
    protected Task<ArrayList<Organizer>> getOrganizers(){

        TaskCompletionSource<ArrayList<Organizer>> tcs = new TaskCompletionSource<>();
        ArrayList<Organizer> organizer_array = new ArrayList<Organizer>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ORGANIZER.toString())){
                    organizer_array.add(document.toObject(Organizer.class));
                }
            }
            tcs.setResult(organizer_array);
        }).addOnFailureListener(exception -> {
                    tcs.setException(new DatabaseException("Error getting Organizers"));
        });
        return tcs.getTask();
    }
//**************************************************************************************************
// *                                            Events
//*************************************************************************************************/

    /**
     * Asynchronously retrieves a list of all events from the Firestore database.
     * This method performs a fetch of the "events" collection and converts each document
     * into an {@link Event} object.
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Event} objects, or a {@link DatabaseException} if the fetch fails.
     */
    protected Task<ArrayList<Event>> getEvents(){
        TaskCompletionSource<ArrayList<Event>> tcs = new TaskCompletionSource<>();
        ArrayList<Event> events_array = new ArrayList<Event>();

        events.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                events_array.add(document.toObject(Event.class));
            }
            tcs.setResult(events_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting events"));
        });
        return tcs.getTask();
    }

    /**
     * Adds a new event to the Firestore database.
     * This method creates a document in the "event" collection using the event's unique ID
     * and stores the provided event object.
     *
     * @param event The {@link Event} object containing the data to be stored.
     * @return A {@link Task} representing the asynchronous database write operation.
     */
    protected Task<Void> addEvent(Event event) {
        DocumentReference newEventRef = events.document(); // asking Firestore to create one here

        event.setEventId(newEventRef.getId()); // Event class expects ID is already existing

        return newEventRef.set(event);
    }
//**************************************************************************************************
//*                                            Location
//*************************************************************************************************/

    /**
     * Asynchronously retrieves a list of all locations from the Firestore database.
     * This method performs a fetch of the "locations" collection and converts each document
     * into an {@link Location} object.
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Location} objects, or a {@link DatabaseException} if the fetch fails.
     */
    protected Task<ArrayList<Location>> getLocations(){
        TaskCompletionSource<ArrayList<Location>> tcs = new TaskCompletionSource<>();
        ArrayList<Location> location_array = new ArrayList<Location>();

        locations.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                location_array.add(document.toObject(Location.class));
            }
            tcs.setResult(location_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting locations"));
        });
        return tcs.getTask();
    }
    /**
     * Adds a new location to the Firestore database.
     * This method creates a document in the "location" collection using the location's unique ID
     * and stores the provided location object.
     *
     * @param location The {@link Location} object containing the data to be stored.
     * @return A {@link Task} representing the asynchronous database write operation.
     */
//    public void addLocation(Location location) {
//        return locations.document(location.getName()).set(location);
//    }

//**************************************************************************************************
// *                                            Topic
// *************************************************************************************************/

    /**
     * Asynchronously retrieves a list of all tags from the Firestore database.
     * This method performs a fetch of the "tags" collection and converts each document
     * into an {@link Tag} object.
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link Tag} objects, or a {@link DatabaseException} if the fetch fails.
     */
    protected Task<ArrayList<Tag>> getTags() {
        TaskCompletionSource<ArrayList<Tag>> tcs = new TaskCompletionSource<>();
        ArrayList<Tag> tags_array = new ArrayList<>();

        tags.get().addOnSuccessListener(result -> {
            for (DocumentSnapshot document : result) {
                tags_array.add(document.toObject(Tag.class));
            }
            tcs.setResult(tags_array);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting Tags"));
        });

        return tcs.getTask();
    }

//    /**
//     * DO NOT USE, CHANGED TO TAGS.
//     * Adds a new topic to the Firestore database.
//     * This method creates a document in the "topic" collection using the topic's unique ID
//     * and stores the provided topic object.
//     *
//     * @param topic The {@link Topic} object containing the data to be stored.
//     * @return A {@link Task} representing the asynchronous database write operation.
//     */
//    public Task<Void> addTopic(Topic topic) {
//        return topics.document(topic.getName()).add(topic);
//    }

//**************************************************************************************************
// *                                           Notifications
// *************************************************************************************************/

    protected Task<Void> addNotification(Notification notification){
        return notifications.document(notification.getNotificationID()).set(notification);
    }

    protected Task<Notification> getNotificationByID(String notificationID){
        TaskCompletionSource<Notification> tcs = new TaskCompletionSource<>();

        notifications.document(notificationID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Notification notification = documentSnapshot.toObject(Notification.class);
                        tcs.setResult(notification);
                    } else {
                        tcs.setException(new DatabaseException("Error getting Notification"));
                    }
                })
                .addOnFailureListener(exception -> {
                        tcs.setException(new DatabaseException("Error getting Notification"));
                    });
                return tcs.getTask();
                }
    }
