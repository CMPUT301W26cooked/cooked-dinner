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
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the admin Organizer Your Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 * Updated by Hao on 2026-03-11 - Added organizer event loading by ID
 */
public class OrganizerYourEventsFragment extends Fragment {
    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventSearcherDatabaseManager eventSearcherDBMan;
    private OrganizerDatabaseManager organizerDBMan;

    // ========== KEY: Use the same test ID as in CreateEventFragment ==========
    private final String TEST_ORGANIZER_ID = "TEMP_ORGANIZER_ID";

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

        // Initialize database managers
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        organizerDBMan = new OrganizerDatabaseManager();

        View createEventButton = view.findViewById(R.id.create_new_event_button);

        // Create New Event button
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Setup RecyclerView
        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();

        // Set up EventAdapter with edit/cancel type
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_EDIT_CANCEL, this::deleteEvent);
        eventListView.setAdapter(eventAdapter);

        // ========== KEY MODIFICATION: Load events created by this organizer ==========
        loadOrganizerEvents();
    }

    /**
     * Load events created by the current organizer
     */
    private void loadOrganizerEvents() {
        organizerDBMan.getOrganizersCreatedEventsFromOrganizerID(TEST_ORGANIZER_ID)
                .addOnSuccessListener(events -> {
                    eventList.clear();
                    if (events != null && !events.isEmpty()) {
                        eventList.addAll(events);
                        Log.d("Event", "Loaded " + events.size() + " events for organizer " + TEST_ORGANIZER_ID);
                    } else {
                        Log.d("Event", "No events found for organizer " + TEST_ORGANIZER_ID);
                        Toast.makeText(getContext(), R.string.no_events_found, Toast.LENGTH_SHORT).show();
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.d("Event", "Failed to load organizer events: " + e.getMessage());
                    Toast.makeText(getContext(),
                            String.format(getString(R.string.error_loading_events), e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Delete an event
     * @param event The event to delete
     */
    public void deleteEvent(Event event) {
        eventSearcherDBMan.deleteEvent(event)
                .addOnSuccessListener(unused -> {
                    eventList.remove(event);
                    eventAdapter.notifyDataSetChanged();
                    Log.d("Event", "Event deleted successfully...");
                    Toast.makeText(getContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("Event", "Event delete failed...");
                    Toast.makeText(getContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                });
    }
}