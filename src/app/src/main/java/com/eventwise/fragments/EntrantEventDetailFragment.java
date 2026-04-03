package com.eventwise.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eventwise.CommentBottomSheet;
import com.eventwise.Event;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.ProfileType;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows the entrant event detail page.
 *
 * This page lets an entrant view event details and act on the event in their current state.
 *
 * @author Becca Irving
 * @since Mar 16 2026
 */
public class EntrantEventDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_ENTRANT_ID = "entrant_id";

    private String eventId;
    private String entrantId;
    private Event currentEvent;

    private ImageView backButton;
    private ImageView eventPoster;
    private ImageView qrCode;
    private ImageView eventStatusIcon;

    private TextView eventName;
    private TextView eventOrganization;
    private TextView eventDescription;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventSpots;
    private TextView eventWaitlisted;
    private TextView eventRegistered;
    private TextView lotteryCloseDate;
    private TextView lotteryCloseTime;
    private TextView registrationCloseDate;
    private TextView registrationCloseTime;
    private TextView eventLocationName;
    private TextView eventLocationCity;
    private TextView eventGuidelines;
    private TextView detailStatusText;

    private LinearLayout eventActionRow;
    private Button eventPrimaryButton;
    private Button eventSecondaryButton;

    private Button eventCommentButton;

    /**
     * Makes the entrant event detail fragment.
     */
    public EntrantEventDetailFragment() {
        super(R.layout.fragment_entrant_event_detail);
    }

    /**
     * Makes a new detail fragment with the event Id and entrant Id.
     *
     * @param eventId event Id to open
     * @param entrantId entrant Id using the page
     * @return configured detail fragment
     */
    public static EntrantEventDetailFragment newInstance(@NonNull String eventId, @NonNull String entrantId) {
        EntrantEventDetailFragment fragment = new EntrantEventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ENTRANT_ID, entrantId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Reads the passed event Id and entrant Id from arguments.
     *
     * @param savedInstanceState saved state bundle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID);
            entrantId = args.getString(ARG_ENTRANT_ID);
        }
    }

    /**
     * Sets up the page views and starts loading the event.
     *
     * @param view fragment root view
     * @param savedInstanceState saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        wireStaticActions();
        loadEvent();
    }

    /**
     * Finds and stores all view references on the page.
     *
     * @param view fragment root view
     */
    private void bindViews(@NonNull View view) {
        backButton = view.findViewById(R.id.back_button);
        eventPoster = view.findViewById(R.id.event_poster);
        qrCode = view.findViewById(R.id.qr_code);
        eventStatusIcon = view.findViewById(R.id.event_status_icon);

        eventName = view.findViewById(R.id.event_name);
        eventOrganization = view.findViewById(R.id.event_organization);
        eventDescription = view.findViewById(R.id.event_description);
        eventDate = view.findViewById(R.id.event_date);
        eventTime = view.findViewById(R.id.event_time);
        eventSpots = view.findViewById(R.id.event_spots);
        eventWaitlisted = view.findViewById(R.id.event_waitlisted);
        eventRegistered = view.findViewById(R.id.event_registered);
        lotteryCloseDate = view.findViewById(R.id.lottery_close_date);
        lotteryCloseTime = view.findViewById(R.id.lottery_close_time);
        registrationCloseDate = view.findViewById(R.id.registration_close_date);
        registrationCloseTime = view.findViewById(R.id.registration_close_time);
        eventLocationName = view.findViewById(R.id.event_location_name);
        eventLocationCity = view.findViewById(R.id.event_location_city);
        eventGuidelines = view.findViewById(R.id.event_guidelines);
        detailStatusText = view.findViewById(R.id.detail_status_text);

        eventActionRow = view.findViewById(R.id.event_action_row);
        eventPrimaryButton = view.findViewById(R.id.event_primary_button);
        eventSecondaryButton = view.findViewById(R.id.event_secondary_button);
        eventCommentButton = view.findViewById(R.id.event_comment_button);
        eventCommentButton.setOnClickListener(v -> {

            if (getParentFragmentManager().findFragmentByTag("CommentBottomSheet") == null) {
                CommentBottomSheet sheet = new CommentBottomSheet();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                args.putString("profileType", ProfileType.ENTRANT.name());
                sheet.setArguments(args);
                sheet.show(getParentFragmentManager(), "CommentBottomSheet");
            }

        });
    }

    /**
     * Wires the back button and detail action buttons.
     */
    private void wireStaticActions() {
        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        eventPrimaryButton.setOnClickListener(v -> handlePrimaryAction());
        eventSecondaryButton.setOnClickListener(v -> handleSecondaryAction());
    }

    /**
     * Handles the main bottom button action for the entrant's current state.
     */
    private void handlePrimaryAction() {
        if (currentEvent == null || entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("EntrantEventDetail", "Cannot act on event: missing event or entrant Id");
            return;
        }

        if (!eventPrimaryButton.isEnabled()) {
            return;
        }

        EventEntrantStatus currentState = getCurrentEntrantState(currentEvent);

        if (currentState == EventEntrantStatus.INVITED) {
            updateEntrantState(EventEntrantStatus.ENROLLED, EventEntrantStatus.INVITED);
        } else if (currentState == EventEntrantStatus.WAITLISTED) {
            updateEntrantState(EventEntrantStatus.LEFT_WAITLIST, EventEntrantStatus.WAITLISTED);
        } else if (currentState == EventEntrantStatus.ENROLLED) {
            updateEntrantState(EventEntrantStatus.CANCELLED, EventEntrantStatus.ENROLLED);
        } else {
            joinEvent(currentState);
        }
    }

    /**
     * Handles the secondary action, currently decline for invited entrants.
     */
    private void handleSecondaryAction() {
        if (currentEvent == null || entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("EntrantEventDetail", "Cannot run secondary action: missing event or entrant Id");
            return;
        }

        if (!eventSecondaryButton.isEnabled()) {
            return;
        }

        EventEntrantStatus currentState = getCurrentEntrantState(currentEvent);

        if (currentState == EventEntrantStatus.INVITED) {
            updateEntrantState(EventEntrantStatus.DECLINED, EventEntrantStatus.INVITED);
        }
    }

    /**
     * Optimistically updates the local event, saves the new state, and reloads on success.
     *
     * @param newState new state to save
     * @param revertState state to restore on failure
     */
    private void updateEntrantState(@NonNull EventEntrantStatus newState,
                                    @Nullable EventEntrantStatus revertState) {
        long timestamp = System.currentTimeMillis() / 1000L;
        EntrantDatabaseManager db = new EntrantDatabaseManager();

        currentEvent.addOrUpdateEntrantStatus(entrantId, newState, timestamp);
        bindEvent(currentEvent);

        db.setEntrantStatusForEvent(entrantId, currentEvent.getEventId(), newState, timestamp)
                .addOnSuccessListener(unused -> {
                    Log.d("EntrantEventDetail", "Successfully updated entrant state to " + newState);
                    loadEvent();
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantEventDetail", "Failed to update entrant state", e);

                    if (revertState != null) {
                        currentEvent.addOrUpdateEntrantStatus(entrantId, revertState, timestamp);
                    }
                    bindEvent(currentEvent);
                });
    }

    /**
     * Loads the event from the database using the stored event Id.
     */
    private void loadEvent() {
        if (eventId == null || eventId.trim().isEmpty()) {
            Log.e("EntrantEventDetail", "Event Id is missing");
            return;
        }

        EventSearcherDatabaseManager db = new EventSearcherDatabaseManager();
        db.getEvents()
                .addOnSuccessListener(events -> {
                    currentEvent = findEventById(events, eventId);

                    if (currentEvent == null) {
                        Log.e("EntrantEventDetail", "Could not find event: " + eventId);
                        return;
                    }

                    bindEvent(currentEvent);
                })
                .addOnFailureListener(e ->
                        Log.e("EntrantEventDetail", "Failed to load event", e));
    }

    /**
     * Finds the matching event in a list by event Id.
     *
     * @param events list of events
     * @param eventId event Id to match
     * @return matching event or null
     */
    @Nullable
    private Event findEventById(@NonNull List<Event> events, @NonNull String eventId) {
        for (Event event : events) {
            if (event != null && eventId.equals(event.getEventId())) {
                return event;
            }
        }
        return null;
    }

    /**
     * Returns the entrant's current state for this event.
     *
     * @param event event to inspect
     * @return entrant state or null if not present
     */
    @Nullable
    private EventEntrantStatus getCurrentEntrantState(@NonNull Event event) {
        if (entrantId == null || entrantId.trim().isEmpty() || event.getEntrantStatuses() == null) {
            return null;
        }

        for (Event.EntrantStatusEntry entry : event.getEntrantStatuses()) {
            if (entry != null
                    && entry.getEntrantProfileId() != null
                    && entrantId.equals(entry.getEntrantProfileId())) {
                return entry.getStatus();
            }
        }

        return null;
    }

    /**
     * Fills the page with event data and updates the action area for the entrant state.
     *
     * This keeps the same detail page but changes the bottom action area so it can
     * support join, waitlisted, invited, and enrolled states.
     *
     * @param event event to show
     *
     */
    private void bindEvent(@NonNull Event event) {
        if (!isAdded()) {
            return;
        }

        eventName.setText(event.getName());
        eventOrganization.setText("Organization Name");
        eventDescription.setText(event.getDescription());

        eventDate.setText(formatDate(event.getEventStartEpochSec()));
        eventTime.setText(formatTimeRange(event.getEventStartEpochSec(), event.getEventEndEpochSec()));

        eventSpots.setText("•  " + event.getMaxWinnersToSample() + " event spots");
        eventWaitlisted.setText("•  " + event.getWaitingListCount() + " on the wait list");
        eventRegistered.setText("•  " + event.getEnrolledCount() + " registered");

        lotteryCloseDate.setText(formatDate(event.getRegistrationCloseEpochSec()));
        lotteryCloseTime.setText(formatTime(event.getRegistrationCloseEpochSec()));
        registrationCloseDate.setText(formatDate(event.getRegistrationCloseEpochSec()));
        registrationCloseTime.setText(formatTime(event.getRegistrationCloseEpochSec()));

        eventLocationName.setText(event.getLocationName());
        eventLocationCity.setText("");
        eventGuidelines.setText("");

        eventStatusIcon.setImageResource(getEventStatusDrawable(event));

        EventEntrantStatus currentState = getCurrentEntrantState(event);

        boolean eventOver = hasEventStarted(event);
        boolean eventClosed = isRegistrationClosed(event);

        detailStatusText.setVisibility(View.GONE);
        eventActionRow.setVisibility(View.VISIBLE);
        eventSecondaryButton.setVisibility(View.GONE);

        if (currentState == EventEntrantStatus.INVITED) {
            detailStatusText.setVisibility(View.VISIBLE);
            detailStatusText.setText("Action Required");
            detailStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));

            eventPrimaryButton.setText("Accept");
            eventPrimaryButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.moss_green))
            );

            eventSecondaryButton.setVisibility(View.VISIBLE);
            eventSecondaryButton.setText("Decline");
            eventSecondaryButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            );

            boolean acceptEnabled = !eventOver && !eventClosed;
            boolean declineEnabled = !eventOver;

            eventPrimaryButton.setEnabled(acceptEnabled);
            eventPrimaryButton.setAlpha(acceptEnabled ? 1.0f : 0.5f);

            eventSecondaryButton.setEnabled(declineEnabled);
            eventSecondaryButton.setAlpha(declineEnabled ? 1.0f : 0.5f);

            return;
        }

        if (currentState == EventEntrantStatus.WAITLISTED) {
            detailStatusText.setVisibility(View.VISIBLE);
            detailStatusText.setText("Waitlisted");
            detailStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.moss_green));

            eventPrimaryButton.setText("Leave");
            eventPrimaryButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            );

            boolean leaveEnabled = !eventOver;
            eventPrimaryButton.setEnabled(leaveEnabled);
            eventPrimaryButton.setAlpha(leaveEnabled ? 1.0f : 0.5f);

            return;
        }

        if (currentState == EventEntrantStatus.ENROLLED) {
            detailStatusText.setVisibility(View.VISIBLE);
            detailStatusText.setText("Registered");
            detailStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.moss_green));

            eventPrimaryButton.setText("Leave");
            eventPrimaryButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            );

            boolean leaveEnabled = !eventOver;
            eventPrimaryButton.setEnabled(leaveEnabled);
            eventPrimaryButton.setAlpha(leaveEnabled ? 1.0f : 0.5f);

            return;
        }

        if (currentState == EventEntrantStatus.LOST_LOTTERY) {
            detailStatusText.setVisibility(View.VISIBLE);
            detailStatusText.setText("Not Selected");
            detailStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        }

        eventPrimaryButton.setText("Join");
        eventPrimaryButton.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.moss_green))
        );

        boolean joinEnabled = !eventOver && !eventClosed && event.isRegistrationOpenNow() && !event.isWaitingListFull();
        eventPrimaryButton.setEnabled(joinEnabled);
        eventPrimaryButton.setAlpha(joinEnabled ? 1.0f : 0.5f);
    }

    /**
     * Checks whether the event has already started.
     *
     * @param event event to check
     * @return true if the event has started
     */
    private boolean hasEventStarted(@NonNull Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= event.getEventStartEpochSec();
    }

    /**
     * Checks whether registration is already closed.
     *
     * @param event event to check
     * @return true if registration is closed
     */
    private boolean isRegistrationClosed(@NonNull Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec > event.getRegistrationCloseEpochSec();
    }

    /**
     * Returns the correct status icon for the event state.
     *
     * @param event event to check
     * @return drawable resource id for the status icon
     */
    private int getEventStatusDrawable(@NonNull Event event) {
        if (hasEventStarted(event)) {
            return R.drawable.event_over;
        }
        if (isRegistrationClosed(event)) {
            return R.drawable.event_closed;
        }
        return R.drawable.event_open;
    }

    /**
     * Formats an epoch time into a short date.
     *
     * @param epochSeconds epoch time in seconds
     * @return formatted date string
     *
     */
    private String formatDate(long epochSeconds) {
        Date date = new Date(epochSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Formats an epoch time into a short time.
     *
     * @param epochSeconds epoch time in seconds
     * @return formatted time string
     *
     */
    private String formatTime(long epochSeconds) {
        Date date = new Date(epochSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Formats a start and end time into one time range.
     *
     * @param startEpochSeconds start time in epoch seconds
     * @param endEpochSeconds end time in epoch seconds
     * @return formatted time range
     *
     */
    private String formatTimeRange(long startEpochSeconds, long endEpochSeconds) {
        return formatTime(startEpochSeconds) + " – " + formatTime(endEpochSeconds);
    }

    /**
     * Registers the current entrant in an event waitlist, optionally capturing
     * their geolocation if the event requires it.
     *
     * @param revertState the entrant's previous status used to restore their state if the join operation fails
     * @return none
     */
    private void joinEvent(@Nullable EventEntrantStatus revertState) {
        if (currentEvent == null || entrantId == null || entrantId.trim().isEmpty()) {
            Log.e("EntrantEventDetail", "Join failed: missing event or entrant Id");
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000L;
        EntrantDatabaseManager db = new EntrantDatabaseManager();

        if (currentEvent.isGeolocationRequired()) {
            com.eventwise.Location.getCurrentLocation(requireContext(), location -> {
                currentEvent.addOrUpdateEntrantStatus(
                        entrantId,
                        EventEntrantStatus.WAITLISTED,
                        timestamp,
                        location
                );
                bindEvent(currentEvent);

                db.registerEntrantInEvent(entrantId, currentEvent.getEventId(), timestamp, location)
                        .addOnSuccessListener(unused -> {
                            Log.d("EntrantEventDetail", "Successfully joined event with location");
                            loadEvent();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EntrantEventDetail", "Join failed", e);

                            if (revertState != null) {
                                currentEvent.addOrUpdateEntrantStatus(entrantId, revertState, timestamp);
                            }
                            bindEvent(currentEvent);
                        });
            });
        } else {
            currentEvent.addOrUpdateEntrantStatus(
                    entrantId,
                    EventEntrantStatus.WAITLISTED,
                    timestamp
            );
            bindEvent(currentEvent);

            db.registerEntrantInEvent(entrantId, currentEvent.getEventId(), timestamp, null)
                    .addOnSuccessListener(unused -> {
                        Log.d("EntrantEventDetail", "Successfully joined event");
                        loadEvent();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EntrantEventDetail", "Join failed", e);

                        if (revertState != null) {
                            currentEvent.addOrUpdateEntrantStatus(entrantId, revertState, timestamp);
                        }
                        bindEvent(currentEvent);
                    });
        }
    }
}
