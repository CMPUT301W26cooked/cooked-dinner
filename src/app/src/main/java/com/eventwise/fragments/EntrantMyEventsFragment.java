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
import com.eventwise.Location;
import com.eventwise.Notification;
import com.eventwise.adapters.EventAdapter;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.NotificationDatabaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for the Entrant My Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-16
 * @update Luke Forster GeoLocation 2026-04-01
 */

public class EntrantMyEventsFragment extends Fragment {

    private RecyclerView currentEventListView;
    private RecyclerView historyEventListView;
    private EventAdapter currentEventAdapter;
    private EventAdapter historyEventAdapter;
    private List<Event> currentEventList;
    private List<Event> historyEventList;

    private TextView currentEmptyText;
    private TextView historyEmptyText;

    public EntrantMyEventsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentEventListView = view.findViewById(R.id.my_events_current_list_view);
        historyEventListView = view.findViewById(R.id.my_events_history_list_view);
        currentEmptyText = view.findViewById(R.id.empty_current_my_events_list);
        historyEmptyText = view.findViewById(R.id.empty_history_my_events_list);
        currentEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyEventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        currentEventListView.setNestedScrollingEnabled(false);
        historyEventListView.setNestedScrollingEnabled(false);
        currentEventList = new ArrayList<>();
        historyEventList = new ArrayList<>();

        currentEventAdapter = new EventAdapter(currentEventList,EventAdapter.TYPE_EDIT_LEAVE,getCurrentEntrantId(),this::primaryButton,this::secondaryButton,this::openEventDetail);

        historyEventAdapter = new EventAdapter(
                historyEventList,
                EventAdapter.TYPE_EDIT_LEAVE,
                getCurrentEntrantId(),
                this::primaryButton,
                this::secondaryButton,
                this::openEventDetail
        );

        currentEventListView.setAdapter(currentEventAdapter);
        historyEventListView.setAdapter(historyEventAdapter);

        refreshEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshEvents();
    }

    public void joinEvent(Event event) {
        String entrantId = getCurrentEntrantId();
        long timestamp = System.currentTimeMillis() / 1000L;

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Join/leave failed: entrant Id is null");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        boolean alreadyInEvent =
                event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId);

        if (alreadyInEvent) {
            if (event.isPrivateEvent()) {
                db.removeEntrantFromEvent(entrantId, event.getEventId())
                        .addOnSuccessListener(unused -> {
                            Log.d("Event", "Successfully removed from private event: " + event.getName());
                            refreshEvents();
                        })
                        .addOnFailureListener(e -> Log.e("Event", "Private event removal failed", e));
                return;
            }

            event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
            currentEventAdapter.notifyDataSetChanged();
            historyEventAdapter.notifyDataSetChanged();

            db.unregisterEntrantInEvent(entrantId, event.getEventId(), timestamp)
                    .addOnSuccessListener(unused -> {
                        Log.d("Event", "Successfully left: " + event.getName());
                        refreshEvents();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Event", "Leave failed", e);

                        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                        currentEventAdapter.notifyDataSetChanged();
                        historyEventAdapter.notifyDataSetChanged();
                    });
        } else {
            if (event.isPrivateEvent()) {
                Log.d("Event", "Private events cannot be joined from my events");
                return;
            }

            if (event.isGeolocationRequired()) {
                Location.getCurrentLocation(requireContext(), location -> {
                    event.addOrUpdateEntrantStatus(
                            entrantId,
                            EventEntrantStatus.WAITLISTED,
                            timestamp,
                            location
                    );

                    db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp, location)
                            .addOnSuccessListener(unused -> {
                                Log.d("Event", "Successfully joined: " + event.getName());
                                sendJoinNotifications(event, entrantId);
                                refreshEvents();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Event", "Join failed", e);

                                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                                currentEventAdapter.notifyDataSetChanged();
                                historyEventAdapter.notifyDataSetChanged();
                            });
                });
            } else {
                event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                db.registerEntrantInEvent(entrantId, event.getEventId(), timestamp, null)
                        .addOnSuccessListener(unused -> {
                            Log.d("Event", "Successfully joined: " + event.getName());
                            sendJoinNotifications(event, entrantId);
                            refreshEvents();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Event", "Join failed", e);

                            event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                            currentEventAdapter.notifyDataSetChanged();
                            historyEventAdapter.notifyDataSetChanged();
                        });
            }

            currentEventAdapter.notifyDataSetChanged();
            historyEventAdapter.notifyDataSetChanged();
        }
    }


    private void openEventDetail(@NonNull Event event) {
        String entrantId = getCurrentEntrantId();

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Cannot open event detail: entrant Id is null");
            return;
        }

        EntrantEventDetailFragment frag =
                EntrantEventDetailFragment.newInstance(event.getEventId(), entrantId);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.entrant_fragment_container, frag)
                .addToBackStack(null)
                .commit();
    }


    private String getCurrentEntrantId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        String entrantProfileId = sessionStore.getEntrantProfileId();
        Log.d("Event", "Current entrant/device Id: " + entrantProfileId);
        return entrantProfileId;
    }

    private void refreshEvents() {
        EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();
        String entrantId = getCurrentEntrantId();

        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList -> {
                    currentEventList.clear();
                    historyEventList.clear();

                    for (Event event : returnedList) {
                        if (event == null) {
                            continue;
                        }

                        if (!shouldShowEventForEntrant(event, entrantId)) {
                            continue;
                        }

                        if (event.isEventOverNow()) {
                            historyEventList.add(event);
                        } else {
                            currentEventList.add(event);
                        }
                    }

                    Collections.sort(currentEventList, (eventOne, eventTwo) ->
                            Long.compare(eventTwo.getEventStartEpochSec(), eventOne.getEventStartEpochSec())
                    );

                    Collections.sort(historyEventList, (eventOne, eventTwo) ->
                            Long.compare(eventTwo.getEventStartEpochSec(), eventOne.getEventStartEpochSec())
                    );

                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();

                    updateSectionEmptyStates();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Failed to refresh events", e);
                    updateSectionEmptyStates();
                });
    }

    /**
     * Applies the same entrant event membership rules as before.
     *
     * Current and history both only include events where the entrant is
     * still waitlisted, invited, or enrolled.
     *
     * @param event event to check
     * @param entrantId entrant id
     * @return true if this event belongs in My Events
     */
    private boolean shouldShowEventForEntrant(@NonNull Event event, @Nullable String entrantId) {
        if (entrantId == null || entrantId.trim().isEmpty()) {
            return false;
        }

        boolean isWaitlisted =
                event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId);
        boolean isInvited =
                event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId);
        boolean isEnrolled =
                event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).contains(entrantId);

        if (event.isPrivateEvent()) {
            return isInvited || isEnrolled;
        }

        return isWaitlisted || isInvited || isEnrolled;
    }

    /**
     * Updates the empty state for both sections.
     */
    private void updateSectionEmptyStates() {
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

    private void leaveEvent(Event event) {
        joinEvent(event);
    }

    private void acceptEvent(Event event) {
        String entrantId = getCurrentEntrantId();
        long timestamp = System.currentTimeMillis() / 1000L;

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Accept failed: entrant Id is null");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.ENROLLED, timestamp);
        currentEventAdapter.notifyDataSetChanged();
        historyEventAdapter.notifyDataSetChanged();

        db.setEntrantStatusForEvent(entrantId, event.getEventId(), EventEntrantStatus.ENROLLED, timestamp)
                .addOnSuccessListener(unused -> {
                    Log.d("Event", "Successfully accepted: " + event.getName());
                    sendAcceptedNotifications(event, entrantId);
                    refreshEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Accept failed", e);

                    event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.INVITED, timestamp);
                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                });
    }

    private void declineEvent(Event event) {
        String entrantId = getCurrentEntrantId();
        long timestamp = System.currentTimeMillis() / 1000L;

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Decline failed: entrant Id is null");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        if (event.isPrivateEvent()) {
            db.removeEntrantFromEvent(entrantId, event.getEventId())
                    .addOnSuccessListener(unused -> {
                        Log.d("Event", "Successfully declined private event invite: " + event.getName());
                        sendDeclinedNotifications(event, entrantId);
                        refreshEvents();
                    })
                    .addOnFailureListener(e -> Log.e("Event", "Private decline failed", e));
            return;
        }

        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.DECLINED, timestamp);
        currentEventAdapter.notifyDataSetChanged();
        historyEventAdapter.notifyDataSetChanged();

        db.setEntrantStatusForEvent(entrantId, event.getEventId(), EventEntrantStatus.DECLINED, timestamp)
                .addOnSuccessListener(unused -> {
                    Log.d("Event", "Successfully declined: " + event.getName());
                    sendDeclinedNotifications(event, entrantId);
                    refreshEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Decline failed", e);

                    event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.INVITED, timestamp);
                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                });
    }

    private void leaveEnrolledEvent(Event event) {
        String entrantId = getCurrentEntrantId();
        long timestamp = System.currentTimeMillis() / 1000L;

        if (entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("Event", "Leave enrolled failed: entrant Id is null");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        if (event.isPrivateEvent()) {
            db.removeEntrantFromEvent(entrantId, event.getEventId())
                    .addOnSuccessListener(unused -> {
                        Log.d("Event", "Successfully left private event: " + event.getName());
                        refreshEvents();
                    })
                    .addOnFailureListener(e -> Log.e("Event", "Private leave failed", e));
            return;
        }

        event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.CANCELLED, timestamp);
        currentEventAdapter.notifyDataSetChanged();
        historyEventAdapter.notifyDataSetChanged();

        db.setEntrantStatusForEvent(entrantId, event.getEventId(), EventEntrantStatus.CANCELLED, timestamp)
                .addOnSuccessListener(unused -> {
                    Log.d("Event", "Successfully left enrolled event: " + event.getName());
                    refreshEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Leave enrolled failed", e);

                    event.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.ENROLLED, timestamp);
                    currentEventAdapter.notifyDataSetChanged();
                    historyEventAdapter.notifyDataSetChanged();
                });
    }

    private void primaryButton(Event event) {
        String entrantId = getCurrentEntrantId();

        if (event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId)) {
            acceptEvent(event);
        } else if (event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId)) {
            leaveEvent(event);
        } else if (event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).contains(entrantId)) {
            leaveEnrolledEvent(event);
        }
    }

    private void secondaryButton(Event event) {
        String entrantId = getCurrentEntrantId();

        if (event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId)) {
            declineEvent(event);
        }
    }
    private void sendJoinNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> entrantRecipients = new ArrayList<>();
        entrantRecipients.add(entrantId);

        Notification entrantNotification = new Notification();
        entrantNotification.setRecipientRole(Notification.RecipientRole.ENTRANT);
        entrantNotification.setEntrantIds(entrantRecipients);
        entrantNotification.setMessageTitle("Event Joined");
        entrantNotification.setMessageBody("You joined " + event.getName());
        entrantNotification.setType(Notification.NotificationType.OTHER);
        entrantNotification.setTimestamp(now);
        notificationDB.createNotification(entrantNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Entrant notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Entrant notification failed", e));


        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("New Entrant");
        organizerNotification.setMessageBody(
                entrantId + " joined your event " + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Organizer notification failed", e));
    }

    private void sendLeaveNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> entrantRecipients = new ArrayList<>();
        entrantRecipients.add(entrantId);

        Notification entrantNotification = new Notification();
        entrantNotification.setRecipientRole(Notification.RecipientRole.ENTRANT);
        entrantNotification.setEntrantIds(entrantRecipients);
        entrantNotification.setMessageTitle("Event Left");
        entrantNotification.setMessageBody("You left " + event.getName());
        entrantNotification.setType(Notification.NotificationType.OTHER);
        entrantNotification.setTimestamp(now);
        notificationDB.createNotification(entrantNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Entrant notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Entrant notification failed", e));


        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("Entrant Left Event");
        organizerNotification.setMessageBody(
                entrantId + " left your event " + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Organizer notification failed", e));
    }
    private void sendAcceptedNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("New Entrant");
        organizerNotification.setMessageBody(
                entrantId + " Accepted your invite to" + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Organizer notification failed", e));
    }
    private void sendDeclinedNotifications(Event event, String entrantId) {

        NotificationDatabaseManager notificationDB =
                new NotificationDatabaseManager();

        long now = System.currentTimeMillis() / 1000L;
        ArrayList<String> organizerRecipients = new ArrayList<>();
        organizerRecipients.add(event.getOrganizerProfileId());

        Notification organizerNotification = new Notification();
        organizerNotification.setRecipientRole(Notification.RecipientRole.ORGANIZER);
        organizerNotification.setEntrantIds(organizerRecipients);
        organizerNotification.setMessageTitle("New Entrant");
        organizerNotification.setMessageBody(
                entrantId + " Declined your invite to " + event.getName()
        );
        organizerNotification.setType(Notification.NotificationType.OTHER);
        organizerNotification.setTimestamp(now);

        notificationDB.createNotification(organizerNotification)
                .addOnSuccessListener(unused ->
                        Log.d("Notification", "Organizer notification created"))
                .addOnFailureListener(e ->
                        Log.e("Notification", "Organizer notification failed", e));
    }
}
