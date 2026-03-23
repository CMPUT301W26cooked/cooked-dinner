package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.EventAdapter;
import com.eventwise.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.List;
import com.eventwise.Notification;
import com.eventwise.database.NotificationDatabaseManager;

/**
 * This class is responsible for the Entrant Events Community Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 * Updated By Becca Irving on 2026-03-16
 */

public class EntrantEventsCommunityFragment extends Fragment {

    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    public EntrantEventsCommunityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventListView = view.findViewById(R.id.events_community_list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        //eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_JOIN, getCurrentEntrantId(), this::joinEvent);
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_JOIN, getCurrentEntrantId(), this::joinEvent, this::openEventDetail);
        eventListView.setAdapter(eventAdapter);
        //Get events from Firebase
        refreshEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshEvents();
    }

    public void joinEvent(Event event) {
        String entrantId = getCurrentEntrantId();
        long timestamp = System.currentTimeMillis() / 1000L;

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Join/leave failed: entrant Id is null");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        boolean alreadyInEvent =
                event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId);

        if (alreadyInEvent) {
            event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
            eventAdapter.notifyDataSetChanged();

            db.unregisterEntrantInEvent(entrantId, event.getEventId(), timestamp)
                    .addOnSuccessListener(unused -> {
                        Log.d("Event", "Successfully left: " + event.getName());
                        sendLeaveNotifications(event, entrantId);
                        refreshEvents();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Event", "Leave failed", e);

                        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                        eventAdapter.notifyDataSetChanged();
                    });
        } else {
            event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
            eventAdapter.notifyDataSetChanged();

            db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp)
                    .addOnSuccessListener(unused -> {
                        Log.d("Event", "Successfully joined: " + event.getName());
                        sendJoinNotifications(event, entrantId);
                        refreshEvents();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Event", "Join failed", e);

                        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                        eventAdapter.notifyDataSetChanged();
                    });
        }
    }


    private void openEventDetail(@NonNull Event event) {
        String entrantId = getCurrentEntrantId();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Cannot open event detail: entrant Id is null");
            return;
        }

        EntrantEventDetailFragment frag =
                EntrantEventDetailFragment.newInstance(event.getEventId(), entrantId);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.entrant_fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }


    private String getCurrentEntrantId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        String deviceId = sessionStore.getOrCreateDeviceId();
        Log.d("Event", "Current entrant/device Id: " + deviceId);
        return deviceId;
    }

    private void refreshEvents() {
        EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();

        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList -> {
                    eventList.clear();
                    eventList.addAll(returnedList);
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Failed to refresh events", e);
                });
    }

    private void sendJoinNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> entrantRecipients = new ArrayList<>();
        entrantRecipients.add(entrantId);

        Notification entrantNotification = new Notification();
        entrantNotification.setRecipientRole(Notification.RecipientRole.ENTRANT);
        entrantNotification.setEntrantIds(entrantRecipients);
        entrantNotification.setMessageTitle("Event Joined");
        entrantNotification.setMessageBody("You joined " + event.getName());
        entrantNotification.setType(Notification.NotificationType.OTHER);
        entrantNotification.setTimestamp(now);
        notificationDB.createNotification(entrantNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Entrant notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Entrant notification failed", e));


        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("New Entrant");
        organizerNotification.setMessageBody(
                entrantId + " joined your event " + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                    Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                    Log.e("Notification", "Organizer notification failed", e));
    }

    private void sendLeaveNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> entrantRecipients = new ArrayList<>();
        entrantRecipients.add(entrantId);

        Notification entrantNotification = new Notification();
        entrantNotification.setRecipientRole(Notification.RecipientRole.ENTRANT);
        entrantNotification.setEntrantIds(entrantRecipients);
        entrantNotification.setMessageTitle("Event Left");
        entrantNotification.setMessageBody("You left " + event.getName());
        entrantNotification.setType(Notification.NotificationType.OTHER);
        entrantNotification.setTimestamp(now);
        notificationDB.createNotification(entrantNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Entrant notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Entrant notification failed", e));


        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("Entrant Left Event");
        organizerNotification.setMessageBody(
                entrantId + " left your event " + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Organizer notification failed", e));
    }
}
