package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the admin Organizer Your Events Fragment.
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-09
 * Updated by Hao on 2026-03-11 - Added organizer event loading by ID
 * Updated By Becca Irving on 2026-03-13
 */
public class OrganizerYourEventsFragment extends Fragment {

    /**
     * TODO (OrganizerYourEventsFragment.java)
     * - Replace the temporary organizer id when real organizer session wiring exists.
     * - Add tests for organizer-only loading and delete from list/detail.
     */

    private String organizerProfileId;
    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventSearcherDatabaseManager eventSearcherDBMan;
    private OrganizerDatabaseManager organizerDatabaseManager;

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

        SessionStore sessionStore = new SessionStore(requireContext());
        organizerProfileId = sessionStore.getOrCreateDeviceId();
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        organizerDatabaseManager = new OrganizerDatabaseManager();
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
        eventListView = view.findViewById(R.id.events_community_list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();

        eventAdapter = new EventAdapter(
                eventList,
                EventAdapter.TYPE_EDIT_CANCEL,
                this::openEditEvent,
                this::deleteEvent,
                this::openEventDetail
        );
        eventListView.setAdapter(eventAdapter);

        getParentFragmentManager().setFragmentResultListener(
                OrganizerEventDetailFragment.REQUEST_KEY_EVENT_CANCELLED,
                getViewLifecycleOwner(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        String deletedEventId = result.getString(OrganizerEventDetailFragment.BUNDLE_KEY_DELETED_EVENT_ID);
                        if (deletedEventId == null) {
                            return;
                        }

                        for (int i = 0; i < eventList.size(); i++) {
                            Event event = eventList.get(i);
                            if (deletedEventId.equals(event.getEventId())) {
                                eventList.remove(i);
                                eventAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                }
        );

        loadOrganizerEvents();
    }

    /**
     * Loads only the current organizer's events.
     */
    private void loadOrganizerEvents() {
        if (organizerDatabaseManager == null) {
            Log.e("OrganizerEvents", "OrganizerDatabaseManager is null in loadOrganizerEvents");
            return;
        }

        organizerDatabaseManager.getOrganizersCreatedEventsFromOrganizerId(organizerProfileId)
                .addOnSuccessListener(returnedList -> {
                    eventList.clear();
                    if (returnedList != null) {
                        eventList.addAll(returnedList);
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("OrganizerEvents", "Failed to load organizer events", e));
    }

    /**
     * Deletes one event from the organizer modular view.
     *
     * @param event event to delete
     */
    public void deleteEvent(Event event) {
        if (event == null || event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            Log.e("Event", "Cannot delete event because eventId is null or empty");
            return;
        }

        eventSearcherDBMan.deleteEvent(event)
                .addOnSuccessListener(unused -> {
                    eventList.remove(event);
                    eventAdapter.notifyDataSetChanged();
                    Log.d("Event", "Event deleted successfully...");
                })
                .addOnFailureListener(e -> Log.e("Event", "Event delete failed...", e));
    }

    /**
     * Opens the edit event detail page for one event.
     *
     * @param event event to open
     */
    private void openEditEvent(Event event) {
        openEventDetail(event);
    }

    // ====== New method start ======

    /**
     * Opens the organizer event detail page for one event.
     *
     * @param event event to open
     */
    private void openEventDetail(Event event) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.organizer_fragment_container, OrganizerEventDetailFragment.newInstance(event))
                .addToBackStack(null)
                .commit();
    }
}
