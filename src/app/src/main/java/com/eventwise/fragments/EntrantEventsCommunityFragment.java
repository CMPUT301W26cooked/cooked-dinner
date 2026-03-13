package com.eventwise.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.EventAdapter;
import com.eventwise.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;

import com.eventwise.database.EntrantDatabaseManager;

/**
 * This class is responsible for the Entrant Events Community Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 */

public class EntrantEventsCommunityFragment extends Fragment {

    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private Entrant currentEntrant;
    private EntrantDatabaseManager entrantDBMan;
    private EventSearcherDatabaseManager eventSearcherDBMan;


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

        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        entrantDBMan = new EntrantDatabaseManager();
        eventSearcherDBMan = new EventSearcherDatabaseManager();

        eventList = new ArrayList<>();

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        entrantDBMan.getEntrantProfileById(deviceId)
                .addOnSuccessListener(profile -> {
                    if (profile instanceof Entrant) {
                        currentEntrant = (Entrant) profile;
                        Log.d("Event", "Loaded entrant profile: " + currentEntrant.getProfileId());
                    } else {
                        createEntrant(deviceId);
                    }

                    setupAdapter();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    createEntrant(deviceId);
                    setupAdapter();
                    loadEvents();
                });
    }

    private void createEntrant(String deviceId) {
        currentEntrant = new Entrant(
                deviceId,
                "Test User",
                "test@email.com",
                "",
                true
        );

        entrantDBMan.addEntrant(currentEntrant)
                .addOnSuccessListener(unused ->
                        Log.d("Event", "Created entrant profile: " + currentEntrant.getProfileId()))
                .addOnFailureListener(e ->
                        Log.e("Event", "Failed to create entrant profile", e));
    }

    private void setupAdapter() {
        eventAdapter = new EventAdapter(
                eventList,
                EventAdapter.TYPE_JOIN,
                currentEntrant,
                this::joinEvent
        );
        eventListView.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList -> {
                    eventList.clear();
                    eventList.addAll(returnedList);

                    for (Event event : returnedList) {
                        Log.d("Event", event.getName());
                    }

                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Event failed to get", e);
                });
    }

    public void joinEvent(Event event) {
        if (event == null) {
            Log.d("Event", "Join failed: event is null");
            return;
        }

        if (event.getEventId() == null) {
            Log.d("Event", "Join failed: eventId is null");
            return;
        }

        if (currentEntrant == null) {
            Log.d("Event", "Join failed: currentEntrant is null");
            return;
        }

        if (!event.isRegistrationOpenNow()) {
            Log.d("Event", "Join failed: registration closed for " + event.getName());
            return;
        }

        if (event.isWaitingListFull()) {
            Log.d("Event", "Join failed: waiting list full for " + event.getName());
            return;
        }

        if (event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(currentEntrant.getProfileId())
                || event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(currentEntrant.getProfileId())
                || event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).contains(currentEntrant.getProfileId())) {
            Log.d("Event", "Join ignored: already joined " + event.getName());
            return;
        }

        entrantDBMan.registerEntrantInEvent(currentEntrant.getProfileId(), event.getEventId())
                .addOnSuccessListener(unused -> {
                    Log.d("Event", "Join success for: " + event.getName());

                    event.addOrUpdateEntrantStatus(
                            currentEntrant.getProfileId(),
                            EventEntrantStatus.WAITLISTED
                    );

                    currentEntrant.addOrUpdateEventState(
                            event.getEventId(),
                            EventEntrantStatus.WAITLISTED
                    );

                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Join failed for: " + event.getName(), e);
                });
    }
}