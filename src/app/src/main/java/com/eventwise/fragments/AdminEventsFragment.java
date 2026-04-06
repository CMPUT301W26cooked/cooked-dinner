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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for the admin Events Fragment.
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-09
 * Updated By Becca Irving on 2026-03-13
 */

// TODO (AdminEventsFragment.java)
// - Revisit the empty state wording later if want a different message.
// - Add tests for canceling from both the card and detail page.

public class AdminEventsFragment extends Fragment {

    private RecyclerView currentEventListView;
    private RecyclerView historyEventListView;

    private TextView currentEmptyText;
    private TextView historyEmptyText;

    private EventAdapter currentEventAdapter;
    private EventAdapter historyEventAdapter;

    private List<Event> currentEventList;
    private List<Event> historyEventList;

    private EventSearcherDatabaseManager eventSearcherDBMan;

    public AdminEventsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventSearcherDBMan = new EventSearcherDatabaseManager();

        currentEventListView = view.findViewById(R.id.admin_current_events_list_view);
        historyEventListView = view.findViewById(R.id.admin_history_events_list_view);
        currentEmptyText = view.findViewById(R.id.empty_current_admin_events_text);
        historyEmptyText = view.findViewById(R.id.empty_history_admin_events_text);

        currentEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        currentEventListView.setNestedScrollingEnabled(false);
        historyEventListView.setNestedScrollingEnabled(false);

        currentEventList = new ArrayList<>();
        historyEventList = new ArrayList<>();

        currentEventAdapter = new EventAdapter(
                currentEventList,
                EventAdapter.TYPE_CANCEL,
                this::deleteEvent,
                this::openEventDetail
        );

        historyEventAdapter = new EventAdapter(
                historyEventList,
                EventAdapter.TYPE_CANCEL,
                this::deleteEvent,
                this::openEventDetail
        );

        currentEventListView.setAdapter(currentEventAdapter);
        historyEventListView.setAdapter(historyEventAdapter);

        getParentFragmentManager().setFragmentResultListener(
                AdminEventDetailFragment.REQUEST_KEY_EVENT_CANCELLED,
                getViewLifecycleOwner(),
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        String deletedEventId = result.getString(AdminEventDetailFragment.BUNDLE_KEY_DELETED_EVENT_ID);
                        if (deletedEventId == null) {
                            return;
                        }

                        removeEventFromList(currentEventList, currentEventAdapter, deletedEventId);
                        removeEventFromList(historyEventList, historyEventAdapter, deletedEventId);
                        updateEmptyStates();
                    }
                }
        );

        loadEvents();
    }

    /**
     * Loads events into the admin list view.
     */
    private void loadEvents() {
        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList -> {
                    currentEventList.clear();
                    historyEventList.clear();

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

                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                    updateEmptyStates();
                })
                .addOnFailureListener(param -> {
                    Log.d("Event", "Event failed to get");
                    updateEmptyStates();
                });
    }

    /**
     * Deletes one event from the modular list view.
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
     * Opens the detail page for one event.
     *
     * @param event event to open
     */
    private void openEventDetail(Event event) {
        View parentView = (View) requireView().getParent();
        int containerId = parentView.getId();

        getParentFragmentManager()
                .beginTransaction()
                .replace(containerId, AdminEventDetailFragment.newInstance(event))
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
    private void removeEventFromList(@NonNull List<Event> list,
                                     @NonNull EventAdapter adapter,
                                     @NonNull String deletedEventId) {
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
     * Swaps each section between the list and empty state.
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
