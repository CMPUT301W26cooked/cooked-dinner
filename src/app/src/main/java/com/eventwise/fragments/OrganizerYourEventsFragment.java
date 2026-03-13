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
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import com.eventwise.Event;
import com.eventwise.database.OrganizerDatabaseManager;

/**
 * This class is responsible for the admin Organizer Your Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

public class OrganizerYourEventsFragment extends Fragment {
    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventSearcherDatabaseManager eventSearcherDBMan;
    private OrganizerDatabaseManager organizerDatabaseManager;
    private Entrant currentEntrant;


    public OrganizerYourEventsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_organizer_your_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        organizerDatabaseManager = new OrganizerDatabaseManager();

        //This line links device ID to the database stuff... IDRKWID though...
        String deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        currentEntrant = new Entrant(
                deviceID,
                "Test User",
                "test@email.com",
                "780-000-0000",
                true
        );

        View createEventButton = view.findViewById(R.id.create_new_event_button);

        //New event button
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });
        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();

        //UPDATE: I changed the event adapter to fetch the right widget for its screen.
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_EDIT_CANCEL, this.currentEntrant, this::deleteEvent);
        eventListView.setAdapter(eventAdapter);

        //Get events from Firebase
        organizerDatabaseManager
                .getOrganizersCreatedEventsFromOrganizerID(currentEntrant.getProfileId())
                .addOnSuccessListener(returnedList -> {
                    eventList.clear();
                    eventList.addAll(returnedList);
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.d("Event", "Failed to get organizer-created events");
                });
    }

    public void deleteEvent(Event event){
        eventSearcherDBMan.deleteEvent(event)
            .addOnSuccessListener(unused -> {
                eventList.remove(event);
                eventAdapter.notifyDataSetChanged();
                Log.d("Event", "Event deleted successfully...");
            })
            .addOnFailureListener(e -> {
                Log.d("Event", "Event delete failed...");
            });
    }
}