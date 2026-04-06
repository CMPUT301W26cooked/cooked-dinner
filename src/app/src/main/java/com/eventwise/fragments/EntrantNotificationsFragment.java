package com.eventwise.fragments;

import android.content.Intent;
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

import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.Event;
import com.eventwise.Notification;
import com.eventwise.R;
import com.eventwise.activities.OrganizerMainActivity;
import com.eventwise.adapters.NotificationAdapter;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.NotificationSearcherDataBaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
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
        if (notification == null || notification.getEventId() == null || notification.getEventId().trim().isEmpty()) {
            Log.e("Notification", "Cannot act because notification eventId is missing");
            return;
        }

        String entrantId = getCurrentEntrantId();
        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Notification", "Cannot act because entrant Id is missing");
            return;
        }

        if (notification.getType() == Notification.NotificationType.CO_ORGANIZER_INVITE) {
            OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

            organizerDatabaseManager.acceptCoOrganizerInvite(notification.getEventId(), entrantId)
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(requireContext(), OrganizerMainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Log.e("Notification", "Failed to accept co-organizer invite", e));

            return;
        }

        EntrantEventDetailFragment fragment =
                EntrantEventDetailFragment.newInstance(notification.getEventId(), entrantId);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.entrant_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private String getCurrentEntrantId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        return sessionStore.getEntrantProfileId();
    }

    private String getCurrentOrganizerId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        return sessionStore.getOrganizerProfileId();
    }

    private void refreshNotifications() {
        String entrantId = getCurrentEntrantId();
        String organizerId = getCurrentOrganizerId();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Notification", "Cannot refresh entrant notifications because entrant Id is missing");
            notificationList.clear();
            actionableEventIds.clear();
            notificationAdapter.notifyDataSetChanged();
            notificationListView.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
            return;
        }

        NotificationSearcherDataBaseManager notificationSearcherDBMan =
                new NotificationSearcherDataBaseManager();
        EventSearcherDatabaseManager eventSearcherDBMan =
                new EventSearcherDatabaseManager();

        notificationSearcherDBMan.getEntrantNotifications(entrantId)
                .addOnSuccessListener(returnedNotifications ->
                        eventSearcherDBMan.getEvents()
                                .addOnSuccessListener(events -> {
                                    rebuildActionableEventIds(events, returnedNotifications, entrantId, organizerId);
                                    bindNotifications(returnedNotifications);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Notification", "Failed to load events for entrant notification actions", e);
                                    rebuildActionableEventIds(null, returnedNotifications, entrantId, organizerId);
                                    bindNotifications(returnedNotifications);
                                })
                )
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to refresh entrant notifications", e);
                    notificationList.clear();
                    actionableEventIds.clear();
                    notificationAdapter.notifyDataSetChanged();
                    notificationListView.setVisibility(View.GONE);
                    emptyListView.setVisibility(View.VISIBLE);
                });
    }

    private void rebuildActionableEventIds(@Nullable List<Event> events, @Nullable List<Notification> returnedNotifications, @NonNull String entrantId,@NonNull String organizerId) {
        actionableEventIds.clear();

        if (events != null) {
            for (Event event : events) {
                if (event == null || event.getEventId() == null) {
                    continue;
                }

                if (event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId)) {
                    actionableEventIds.add(event.getEventId());
                }
            }
        }

        if (returnedNotifications != null) {
            for (Notification notification : returnedNotifications) {
                if (notification == null|| notification.getType() != Notification.NotificationType.CO_ORGANIZER_INVITE|| notification.getEventId() == null || notification.getEventId().trim().isEmpty()) {
                    continue;
                }

                boolean alreadyHasAccess = false;

                if (events != null) {
                    for (Event event : events) {
                        if (event != null && notification.getEventId().equals(event.getEventId()) && event.hasOrganizerAccess(organizerId)) {
                            alreadyHasAccess = true;
                            break;
                        }
                    }
                }

                if (!alreadyHasAccess) {
                    actionableEventIds.add(notification.getEventId());
                }
            }
        }
    }

    private void bindNotifications(@Nullable List<Notification> returnedNotifications) {
        notificationList.clear();
        if (returnedNotifications != null) {
            notificationList.addAll(returnedNotifications);
        }

        Collections.sort(notificationList, (first, second) -> {
            long firstTime = getTimestampValue(first);
            long secondTime = getTimestampValue(second);
            return Long.compare(secondTime, firstTime);
        });

        notificationAdapter.notifyDataSetChanged();

        if (notificationList.isEmpty()) {
            notificationListView.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
        } else {
            notificationListView.setVisibility(View.VISIBLE);
            emptyListView.setVisibility(View.GONE);
        }
    }

    private long getTimestampValue(@Nullable Notification notification) {
        if (notification == null || notification.getTimestamp() == null) {
            return Long.MIN_VALUE;
        }
        return notification.getTimestamp();
    }
}
