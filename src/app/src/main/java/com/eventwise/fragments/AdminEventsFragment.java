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
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
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

    private RecyclerView eventListView;
    private TextView emptyStateText;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

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
        eventListView = view.findViewById(R.id.events_community_list_view);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(
                eventList,
                EventAdapter.TYPE_CANCEL,
                this::deleteEvent,
                this::openEventDetail
        );
        eventListView.setAdapter(eventAdapter);

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

                        for (int i = 0; i < eventList.size(); i++) {
                            Event event = eventList.get(i);
                            if (deletedEventId.equals(event.getEventId())) {
                                eventList.remove(i);
                                eventAdapter.notifyItemRemoved(i);
                                updateEmptyState();
                                break;
                            }
                        }
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
                    eventList.clear();
                    eventList.addAll(returnedList);
                    eventAdapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(param -> {
                    Log.d("Event", "Event failed to get");
                    updateEmptyState();
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
                    eventList.remove(event);
                    eventAdapter.notifyDataSetChanged();
                    updateEmptyState();
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
     * Swaps between the list and empty state.
     */
    private void updateEmptyState() {
        if (eventList.isEmpty()) {
            eventListView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            eventListView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }
}
