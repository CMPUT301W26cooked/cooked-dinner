package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.OrganizerEventAdapter;
import com.eventwise.R;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import com.eventwise.Event;

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

    // RecyclerView and Adapter for displaying events
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private OrganizerDatabaseManager dbManager;

    // Test organizer ID - matches the one in CreateEventFragment
    private final String TEST_ORGANIZER_ID = "TEMP_ORGANIZER_ID";

    public OrganizerYourEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_organizer_your_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ========== Original code: Create Event button ==========
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        View createEventButton = view.findViewById(R.id.create_new_event_button);

        //New event button
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // ========== End of original code ==========

        // ========== New code: Display events with View Invited button ==========

        // Initialize database manager
        dbManager = new OrganizerDatabaseManager();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.event_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load events created by this organizer
        loadEvents();

        // ========== End of new code ==========
    }

    /**
     * Loads events created by the organizer from Firebase and displays them in RecyclerView
     */
    private void loadEvents() {
        dbManager.getOrganizersCreatedEventsFromOrganizerID(TEST_ORGANIZER_ID)
                .addOnSuccessListener(events -> {
                    if (events == null || events.isEmpty()) {
                        Toast.makeText(getContext(), "No events found. Create your first event!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create adapter with the list of events
                    adapter = new OrganizerEventAdapter(events);

                    // Set click listeners for the buttons on each event card
                    adapter.setOnItemClickListener(new OrganizerEventAdapter.OnItemClickListener() {
                        @Override
                        public void onViewInvitedClick(Event event) {
                            // Navigate to Invited Entrants Fragment to see who was selected
                            InvitedEntrantsFragment fragment =
                                    InvitedEntrantsFragment.newInstance(event.getEventId(), event.getName());

                            getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.organizer_fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }

                        @Override
                        public void onManageClick(Event event) {
                            // Placeholder for future manage event functionality
                            Toast.makeText(getContext(), "Managing: " + event.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Set the adapter to the RecyclerView
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();

        //UPDATE: I changed the event adapter to fetch the right widget for its screen.
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_EDIT_CANCEL, this::deleteEvent);
        eventListView.setAdapter(eventAdapter);

        //Get events from Firebase
        EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();
        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList ->{
                    for (int i = 0; i < returnedList.size(); i++) {
                        Log.d("Event", returnedList.get(i).getName());
                        eventList.add(returnedList.get(i));
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(param-> {
                    Log.d("Event", "Event failed to get...");
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