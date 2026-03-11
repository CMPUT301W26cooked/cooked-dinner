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

import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import com.eventwise.Event;

// ========== New imports start ==========
import android.widget.Toast;
import com.eventwise.OrganizerEventAdapter;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.fragments.InvitedEntrantsFragment;
// ========== New imports end ==========

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

    // ========== New member variables start ==========
    private RecyclerView organizerRecyclerView;
    private OrganizerEventAdapter organizerAdapter;
    private OrganizerDatabaseManager organizerDBMan;
    private final String TEST_ORGANIZER_ID = "TEMP_ORGANIZER_ID";
    // ========== New member variables end ==========

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
        View createEventButton = view.findViewById(R.id.create_new_event_button);

        // Original code: Create New Event button
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

        // Original code: Set up EventAdapter with edit/cancel type
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_EDIT_CANCEL, this::deleteEvent);
        eventListView.setAdapter(eventAdapter);

        // Original code: Fetch events from Firebase
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

        // ========== New code start: Add organizer event management functionality ==========
        
        // Initialize organizer database manager
        organizerDBMan = new OrganizerDatabaseManager();

        // Set up RecyclerView for organizer view (using the same list_view)
        organizerRecyclerView = view.findViewById(R.id.list_view);
        
        // Load events created by this organizer (using test ID)
        loadOrganizerEvents();
        
        // ========== New code end ==========
    }

    /**
     * Original method: Delete an event
     * @param event The event to delete
     */
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

    // ========== New methods start ==========
    
    /**
     * Load events created by the organizer and display them using the new adapter
     */
    private void loadOrganizerEvents() {
        organizerDBMan.getOrganizersCreatedEventsFromOrganizerID(TEST_ORGANIZER_ID)
                .addOnSuccessListener(events -> {
                    if (events == null || events.isEmpty()) {
                        // No events found, keep original EventAdapter
                        return;
                    }

                    // Create new adapter with organizer event card layout
                    organizerAdapter = new OrganizerEventAdapter(events);
                    
                    // Set click listeners for the buttons
                    organizerAdapter.setOnItemClickListener(new OrganizerEventAdapter.OnItemClickListener() {
                        @Override
                        public void onViewInvitedClick(Event event) {
                            // Navigate to Invited Entrants Fragment
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
                            // Reuse the original deleteEvent method
                            deleteEvent(event);
                        }
                    });
                    
                    // Switch adapter to organizer view
                    organizerRecyclerView.setAdapter(organizerAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
    
    // ========== New methods end ==========
}
