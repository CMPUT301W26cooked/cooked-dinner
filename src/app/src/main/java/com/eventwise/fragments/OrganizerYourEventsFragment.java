package com.eventwise.fragments;

import android.os.Bundle;
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

/**
 * This class is responsible for the admin Organizer Your Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 * Added code is responsible for the view list of invited entrants.
 * @ revision author Hao
 */

public class OrganizerYourEventsFragment extends Fragment {

    // ========== New code start ==========
    private RecyclerView recyclerView;
    private OrganizerEventAdapter adapter;
    private OrganizerDatabaseManager dbManager;
    private final String TEST_ORGANIZER_ID = "TEMP_ORGANIZER_ID";
    // ========== New code end ==========

    public OrganizerYourEventsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_organizer_your_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // ========== Original code start ==========
        View createEventButton = view.findViewById(R.id.create_new_event_button);

        createEventButton.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();

        });
        // ========== Original code end ==========

        // ========== New code start ==========
        // Initialize database manager
        dbManager = new OrganizerDatabaseManager();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.event_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load events from database
        loadEvents();
        // ========== New code end ==========
    }

    // ========== New method start ==========
    /**
     * Loads events created by the organizer from the database
     * and displays them in the RecyclerView
     */
    private void loadEvents() {
        dbManager.getOrganizersCreatedEventsFromOrganizerID(TEST_ORGANIZER_ID)
                .addOnSuccessListener(events -> {
                    if (events == null || events.isEmpty()) {
                        Toast.makeText(getContext(), "No events found. Create your first event!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adapter = new OrganizerEventAdapter(events);
                    adapter.setOnItemClickListener(new OrganizerEventAdapter.OnItemClickListener() {
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
                            // Placeholder for manage event functionality
                            Toast.makeText(getContext(), "Managing: " + event.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
    // ========== New method end ==========
}