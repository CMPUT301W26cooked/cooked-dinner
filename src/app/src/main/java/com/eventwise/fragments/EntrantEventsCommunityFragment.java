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

}
