package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.adapters.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.Collections;
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


    private RecyclerView currentEventListView;
    private RecyclerView historyEventListView;

    private TextView currentEmptyText;
    private TextView historyEmptyText;


    private EventAdapter currentEventAdapter;
    private EventAdapter historyEventAdapter;

    private List<Event> currentEventList;
    private List<Event> historyEventList;

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
        organizerProfileId = sessionStore.getOrganizerProfileId();
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        organizerDatabaseManager = new OrganizerDatabaseManager();

        View createEventButton = view.findViewById(R.id.create_new_event_button);
        currentEventListView = view.findViewById(R.id.organizer_current_events_list_view);
        historyEventListView = view.findViewById(R.id.organizer_history_events_list_view);
        currentEmptyText = view.findViewById(R.id.empty_current_organizer_events_text);
        historyEmptyText = view.findViewById(R.id.empty_history_organizer_events_text);

        // Create New Event button
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        currentEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        currentEventListView.setNestedScrollingEnabled(false);
        historyEventListView.setNestedScrollingEnabled(false);

        currentEventList = new ArrayList<>();
        historyEventList = new ArrayList<>();

        currentEventAdapter = new EventAdapter(
                currentEventList,
                EventAdapter.TYPE_EDIT_CANCEL,
                this::openEditEvent,
                this::deleteEvent,
                this::openEventDetail
        );

        historyEventAdapter = new EventAdapter(
                historyEventList,
                EventAdapter.TYPE_EDIT_CANCEL,
                this::openEditEvent,
                this::deleteEvent,
                this::openEventDetail
        );

        currentEventListView.setAdapter(currentEventAdapter);
        historyEventListView.setAdapter(historyEventAdapter);

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

                        removeEventFromList(currentEventList, currentEventAdapter, deletedEventId);
                        removeEventFromList(historyEventList, historyEventAdapter, deletedEventId);
                        updateEmptyStates();
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
                    currentEventList.clear();
                    historyEventList.clear();

                    if (returnedList != null) {
                        for (Event event : returnedList) {
                            if (event == null) {
                                continue;
                            }

                            if (event.isEventOverNow()) {
                                historyEventList.add(event);
                            } else {
                                currentEventList.add(event);
                            }
                        }

                        Collections.sort(currentEventList, (first, second) ->
                                Long.compare(second.getEventStartEpochSec(), first.getEventStartEpochSec()));

                        Collections.sort(historyEventList, (first, second) ->
                                Long.compare(second.getEventStartEpochSec(), first.getEventStartEpochSec()));
                    }

                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                    updateEmptyStates();
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
                    currentEventList.remove(event);
                    historyEventList.remove(event);
                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                    updateEmptyStates();
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
        if (event == null || event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            Log.e("OrganizerEvents", "Cannot open edit because eventId is null or empty");
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.organizer_fragment_container, CreateEventFragment.newEditInstance(event.getEventId()))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Opens the organizer event detail page for one event.
     *
     * @param event event to open
     */
    private void openEventDetail(Event event) {
        if (event == null || event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            Log.e("OrganizerEvents", "Cannot open event detail because eventId is null or empty");
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.organizer_fragment_container,
                        OrganizerEventDetailFragment.newInstance(event.getEventId())
                )
                .addToBackStack(null)
                .commit();
    }

    /**
     * Removes one event by id from a list and notifies the adapter.
     *
     * @param list target list
     * @param adapter adapter tied to the list
     * @param deletedEventId deleted event id
     */
    private void removeEventFromList(@NonNull List<Event> list,@NonNull EventAdapter adapter,@NonNull String deletedEventId) {
        for (int i = 0; i < list.size(); i++) {
            Event event = list.get(i);
            if (event != null && deletedEventId.equals(event.getEventId())) {
                list.remove(i);
                adapter.notifyItemRemoved(i);
                return;
            }
        }
    }

    /**
     * Updates the empty state labels for both sections.
     */
    private void updateEmptyStates() {
        if (currentEventList.isEmpty()) {
            currentEventListView.setVisibility(View.GONE);
            currentEmptyText.setVisibility(View.VISIBLE);
        } else {
            currentEventListView.setVisibility(View.VISIBLE);
            currentEmptyText.setVisibility(View.GONE);
        }

        if (historyEventList.isEmpty()) {
            historyEventListView.setVisibility(View.GONE);
            historyEmptyText.setVisibility(View.VISIBLE);
        } else {
            historyEventListView.setVisibility(View.VISIBLE);
            historyEmptyText.setVisibility(View.GONE);
        }
    }
}
