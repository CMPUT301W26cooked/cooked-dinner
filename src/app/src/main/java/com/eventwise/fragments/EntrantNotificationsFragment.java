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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
import com.eventwise.Notification;
import com.eventwise.NotificationAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.NotificationSearcherDataBaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Page for viewing Entrant Notifications.
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-22
 */

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationListView;
    private TextView emptyListView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private final Set<String> actionableEventIds = new HashSet<>();

    public EntrantNotificationsFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationListView = view.findViewById(R.id.notification_list_view);
        emptyListView = view.findViewById(R.id.empty_list);
        notificationListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(
                notificationList,
                NotificationAdapter.TYPE_ENTRANT,
                this::primaryButton,
                actionableEventIds
        );
        notificationListView.setAdapter(notificationAdapter);
        //Get events from Firebase
        refreshNotifications();
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshNotifications();
    }

    private void primaryButton(Notification notification) {
        if (notification == null) {
            Log.e("Notification", "Notification is null");
            return;
        }

        String entrantId = getCurrentEntrantId();
        String eventId = notification.getEventId();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Notification", "Entrant ID is null");
            return;
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            Log.e("Notification", "Notification event ID is missing");
            return;
        }

        if (!actionableEventIds.contains(eventId)) {
            Log.d("Notification", "Take Action ignored because entrant is no longer invited for event: " + eventId);
            refreshNotifications();
            return;
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.entrant_fragment_container,
                        EntrantEventDetailFragment.newInstance(eventId, entrantId)
                )
                .addToBackStack("entrant_notifications")
                .commit();
    }
    private String getCurrentEntrantId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        String deviceId = sessionStore.getOrCreateDeviceId();
        Log.d("Notification", "Current entrant/device Id: " + deviceId);
        return deviceId;
    }

    private void refreshNotifications() {
        String entrantId = getCurrentEntrantId();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Notification", "Entrant ID is null");
            notificationList.clear();
            actionableEventIds.clear();
            notificationAdapter.notifyDataSetChanged();
            notificationListView.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
            return;
        }

        NotificationSearcherDataBaseManager notificationSearcherDBMan =
                new NotificationSearcherDataBaseManager();

        notificationSearcherDBMan.getEntrantNotifications(entrantId)
                .addOnSuccessListener(returnedList -> {
                    notificationList.clear();
                    if (returnedList != null) {
                        notificationList.addAll(returnedList);
                        Collections.sort(notificationList, (first, second) -> {
                            long firstTimestamp = first != null && first.getTimestamp() != null ? first.getTimestamp() : Long.MIN_VALUE;
                            long secondTimestamp = second != null && second.getTimestamp() != null ? second.getTimestamp() : Long.MIN_VALUE;
                            return Long.compare(secondTimestamp, firstTimestamp);
                        });
                    }

                    if (notificationList.isEmpty()) {
                        actionableEventIds.clear();
                        notificationAdapter.notifyDataSetChanged();
                        notificationListView.setVisibility(View.GONE);
                        emptyListView.setVisibility(View.VISIBLE);
                    } else {
                        notificationListView.setVisibility(View.VISIBLE);
                        emptyListView.setVisibility(View.GONE);
                        refreshActionableEventIds(entrantId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to refresh Notification", e);
                    actionableEventIds.clear();
                    notificationListView.setVisibility(View.GONE);
                    emptyListView.setVisibility(View.VISIBLE);
                });
    }

    private void refreshActionableEventIds(@NonNull String entrantId) {
        EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();

        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(events -> {
                    actionableEventIds.clear();

                    if (events != null) {
                        for (Event event : events) {
                            if (event != null
                                    && event.getEventId() != null
                                    && !hasEventStarted(event) // stop allowing enrtrants to take action on an over event
                                    && event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId)) {
                                actionableEventIds.add(event.getEventId());
                            }
                        }
                    }

                    notificationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to refresh actionable event IDs", e);
                    actionableEventIds.clear();
                    notificationAdapter.notifyDataSetChanged();
                });

    }

    private boolean hasEventStarted(@NonNull Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= event.getEventStartEpochSec();
    }
}
