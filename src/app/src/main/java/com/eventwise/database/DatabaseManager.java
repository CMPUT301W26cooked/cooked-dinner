package com.eventwise.database;

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
        locations = db.collection("locations");
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
        locations = db.collection("locations");
        tags = db.collection("tags");
        notifications = db.collection("notifications");
   }


    //**************************************************************************************************
    // *                                            Profiles
    // *************************************************************************************************/

    /**
     * Adds a new profile to the Firestore database.
     * This method creates a document in the "profiles" collection using the profile's unique Id
     * and stores the provided profile object.
     *
     * @param profile The {@link Profile} object containing the data to be stored.
     * @return A {@link Task} representing the asynchronous database write operation.
     */
    protected Task<Void> addProfile(Profile profile) {
        return profiles.document(profile.getProfileId()).set(profile);
    }


    /**
     * Updates an existing profile in the database.
     * This method first verifies that a profile with the given Id exists before attempting to
     * overwrite it. If the profile does not exist, the returned task will fail with a
     * {@link DatabaseException}.
     *
     * @param profile The {@link Profile} object containing the updated information and a valid profile Id.
     * @return A {@link Task} that will be completed when the update is successful.
     */
    protected Task<Void> updateProfile(Profile profile) {
        return profiles.document(profile.getProfileId()).get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                return Tasks.forException(new DatabaseException("Profile does not exist"));
            }
            return profiles.document(profile.getProfileId()).set(profile);
        });
    }

    protected Task<Void> deleteProfileFromId(String profileId) {
        return profiles.document(profileId).delete();
    }



    /**
     * Retrieves a profile from the Firestore database using the specified profile Id.
     * This method performs an asynchronous fetch and returns a Task that will resolve
     * to the Profile object if found.
     *
     * @param profileId The unique identifier of the profile to retrieve.
     * @return A {@link Task} that will contain the {@link Profile} object upon success,
     *         or a {@link DatabaseException} if the profile does not exist or the fetch fails.
     */
    protected Task<Profile> getProfileFromId(String profileId) {
        TaskCompletionSource<Profile> tcs = new TaskCompletionSource<>();

        profiles.document(profileId).get().addOnSuccessListener(documentSnapshot -> {
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
        ArrayList<Entrant> entrantsArray = new ArrayList<>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ENTRANT.toString())) {
                    entrantsArray.add(document.toObject(Entrant.class));
                }
            }
            tcs.setResult(entrantsArray);
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
        ArrayList<Admin> adminArray = new ArrayList<>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ADMIN.toString())) {
                    adminArray.add(document.toObject(Admin.class));
                }
            }
            tcs.setResult(adminArray);
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
        ArrayList<Organizer> organizerArray = new ArrayList<>();

        profiles.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                if (document.getData().get("profileType").equals(ProfileType.ORGANIZER.toString())) {
                    organizerArray.add(document.toObject(Organizer.class));
                }
            }
            tcs.setResult(organizerArray);
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
        ArrayList<Event> eventsArray = new ArrayList<>();

        events.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                Event event = document.toObject(Event.class);
                if (event != null) {
                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(document.getId());
                    }
                    eventsArray.add(event);
                }
            }
            tcs.setResult(eventsArray);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting events"));
        });

        return tcs.getTask();
    }

    /**
     * Adds a new event to the Firestore database.
     * This method creates a document in the "event" collection using the event's unique Id
     * and stores the provided event object.
     *
     * @param event The {@link Event} object containing the data to be stored.
     * @return A {@link Task} representing the asynchronous database write operation.
     */
    protected Task<Void> addEvent(Event event) {
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            DocumentReference newEventRef = events.document();
            event.setEventId(newEventRef.getId());
            return newEventRef.set(event);
        }

        return events.document(event.getEventId()).set(event);

//        DocumentReference newEventRef = events.document(); // asking Firestore to create one here
//
//        event.setEventId(event.getEventId()); // Event class expects Id is already existing
//
//        return newEventRef.set(event);
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
        ArrayList<Location> locationArray = new ArrayList<>();

        locations.get().addOnSuccessListener( result -> {
            for (DocumentSnapshot document : result) {
                locationArray.add(document.toObject(Location.class));
            }
            tcs.setResult(locationArray);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting locations"));
        });
        return tcs.getTask();
    }
    /**
     * Adds a new location to the Firestore database.
     * This method creates a document in the "location" collection using the location's unique Id
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
        ArrayList<Tag> tagsArray = new ArrayList<>();

        tags.get().addOnSuccessListener(result -> {
            for (DocumentSnapshot document : result) {
                tagsArray.add(document.toObject(Tag.class));
            }
            tcs.setResult(tagsArray);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting Tags"));
        });

        return tcs.getTask();
    }

//    /**
//     * DO NOT USE, CHANGED TO TAGS.
//     * Adds a new topic to the Firestore database.
//     * This method creates a document in the "topic" collection using the topic's unique Id
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
        return notifications.document(notification.getNotificationId()).set(notification);
    }

    protected Task<Notification> getNotificationById(String notificationId) {
        TaskCompletionSource<Notification> tcs = new TaskCompletionSource<>();

        notifications.document(notificationId).get()
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

//**************************************************************************************************
// *                                            Images
// *************************************************************************************************/

   
    /**
     * Asynchronously retrieves a list of all event poster image paths from the Firestore database.
     * This method iterates through all documents in the "events" collection and extracts
     * non-null and non-empty poster path strings.
     *
     * @return A {@link Task} that, when successful, contains an {@link ArrayList} of
     *         {@link String} paths, or a {@link DatabaseException} if the fetch fails.
     */
    //return a list of all event poster paths, excluding null and empty strings
    // 
    protected Task<ArrayList<String>> getEventPosterPaths() {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();
        ArrayList<String> paths = new ArrayList<>();

        events.get().addOnSuccessListener(result -> {
            for (DocumentSnapshot document : result) {
                Event event = document.toObject(Event.class);
                if (event != null && event.getPosterPath() != null && !event.getPosterPath().isEmpty()) {
                    paths.add(event.getPosterPath());
                }
            }
            tcs.setResult(paths);
        }).addOnFailureListener(exception -> {
            tcs.setException(new DatabaseException("Error getting event poster paths"));
        });
        return tcs.getTask();
    }

    /**
     * Updates the poster image path for an existing event in the Firestore database.
     * This method updates only the "posterPath" field of the specified event document.
     *
     * @param eventId    The unique identifier of the event to update.
     * @param posterPath The new string path or URI for the event poster.
     * @return A {@link Task} representing the asynchronous database update operation.
     */ //update the poster path of an existing event in Firestore
    protected Task<Void> updateEventPosterPath(String eventId, String posterPath) {
        return events.document(eventId).update("posterPath", posterPath);
    }
    }
