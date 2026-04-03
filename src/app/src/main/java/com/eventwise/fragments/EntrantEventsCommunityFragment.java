package com.eventwise.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.adapters.EventAdapter;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.FilterBottomSheet;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.EventFilter;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.eventwise.Notification;
import com.eventwise.database.NotificationDatabaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.eventwise.Location;

/**
 * This class is responsible for the Entrant Events Community Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 * Updated By Becca Irving on 2026-03-16
 * Updated By Pablo Osorio on 2026-03-29
 */

public class EntrantEventsCommunityFragment extends Fragment {

    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    private EditText searchBar;

    FloatingActionButton filterButton;

    private EventFilter currentFilter = new EventFilter();

    private LinearLayout emptyState;





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

        //Empty state stuff
        emptyState = view.findViewById(R.id.empty_state);


        //Searchbar stuff
        searchBar = view.findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                String keyword = searchBar.getText().toString().trim();
                //If no keywords, clear the filter
                if (keyword.isEmpty()){
                    currentFilter.resetKeywords();
                    refreshEvents();
                    return true;
                }
                //Set filter to keywords
                currentFilter.setKeywords(new ArrayList<>(Arrays.asList(keyword.trim().split("\\s+"))));
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                refreshEvents();
                return true;
            }
            return false;
        });

        //Filter button stuff
        filterButton = view.findViewById(R.id.fab_filter);
        filterButton.setOnClickListener(v -> {
            FilterBottomSheet sheet = new FilterBottomSheet();
            sheet.setFilterListener((startDate, endDate, minSpots) -> {
                EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();
                currentFilter.setStartTimestamp(startDate);
                currentFilter.setEndTimestamp(endDate);
                currentFilter.setEventCapacity(minSpots);
                eventSearcherDBMan.getFilteredEvents(currentFilter)
                        .addOnSuccessListener(returnedList -> {
                            refreshEvents();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Event", "Failed to refresh events", e);
                        });
            });
            sheet.show(getParentFragmentManager(), "filter_sheet");
        });


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
            if (event.isGeolocationRequired()) {
                Location.getCurrentLocation(requireContext(), location -> {
                    event.addOrUpdateEntrantStatus(
                            entrantId,
                            EventEntrantStatus.WAITLISTED,
                            timestamp,
                            location
                    );

                    db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp, location)
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
                });
            } else {
                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp, null)
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

            eventAdapter.notifyDataSetChanged();

            db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp, null)
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

        eventSearcherDBMan.getFilteredEvents(currentFilter)
                .addOnSuccessListener(returnedList -> {
                    eventList.clear();
                    eventList.addAll(returnedList);

                    Collections.sort(eventList, (eventOne, eventTwo) ->
                            Long.compare(eventTwo.getEventStartEpochSec(), eventOne.getEventStartEpochSec())
                    );

                    eventAdapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        eventListView.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        eventListView.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
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
