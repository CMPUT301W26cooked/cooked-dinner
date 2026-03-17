package com.eventwise.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
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
 * This page lets an entrant view event details and join or leave the waitlist.
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
    private TextView waitlistedStatusText;

    private Button eventActionButton;

    /**
     * Makes the entrant event detail fragment.
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     *
     * @author Becca Irving
     * @since Mar 16 2026
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);

        eventActionButton.setEnabled(false);
        eventActionButton.setAlpha(0.5f);

        wireStaticActions();
        loadEvent();
    }

    /**
     * Finds and stores all view references on the page.
     *
     * @param view fragment root view
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
        waitlistedStatusText = view.findViewById(R.id.waitlisted_status_text);

        eventActionButton = view.findViewById(R.id.event_action_button);
    }

    /**
     * Wires the back button and join or leave button.
     *
     * @author Becca Irving
     * @since Mar 16 2026
     */
    private void wireStaticActions() {
        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        eventActionButton.setOnClickListener(v -> {
            if (currentEvent == null || entrantId == null || entrantId.trim().isEmpty()) {
                Log.e("EntrantEventDetail", "Cannot act on event: missing event or entrant Id");
                return;
            }

            if (!eventActionButton.isEnabled()) {
                return;
            }

            long timestamp = System.currentTimeMillis() / 1000L;
            EntrantDatabaseManager db = new EntrantDatabaseManager();

            boolean isWaitlisted = currentEvent.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED)
                    .contains(entrantId);

            if (isWaitlisted) {
                currentEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                bindEvent(currentEvent);

                db.unregisterEntrantInEvent(entrantId, currentEvent.getEventId(), timestamp)
                        .addOnSuccessListener(unused -> {
                            Log.d("EntrantEventDetail", "Successfully left waitlist");
                            loadEvent();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EntrantEventDetail", "Failed to leave waitlist", e);
                            currentEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                            bindEvent(currentEvent);
                        });
            } else {
                currentEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.WAITLISTED, timestamp);
                bindEvent(currentEvent);

                db.registerEntrantInEvent(entrantId, currentEvent.getEventId(), timestamp)
                        .addOnSuccessListener(unused -> {
                            Log.d("EntrantEventDetail", "Successfully joined waitlist");
                            loadEvent();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EntrantEventDetail", "Failed to join waitlist", e);
                            currentEvent.addOrUpdateEntrantStatus(entrantId, EventEntrantStatus.LEFT_WAITLIST, timestamp);
                            bindEvent(currentEvent);
                        });
            }
        });
    }

    /**
     * Loads the event from the database using the stored event Id.
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     * Fills the page with event data and updates the button state.
     *
     * @param event event to show
     *
     * @author Becca Irving
     * @since Mar 16 2026
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

        boolean isWaitlisted = entrantId != null
                && event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(entrantId);

        boolean disableButton = shouldDisablePrimaryButton(event);

        if (isWaitlisted) {
            waitlistedStatusText.setVisibility(View.VISIBLE);
            eventActionButton.setText("Leave");
            eventActionButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
            );
        } else {
            waitlistedStatusText.setVisibility(View.GONE);
            eventActionButton.setText("Join");
            eventActionButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.moss_green))
            );
        }

        eventActionButton.setEnabled(!disableButton);
        eventActionButton.setAlpha(eventActionButton.isEnabled() ? 1.0f : 0.5f);
    }

    /**
     * Checks whether the event has already started.
     *
     * @param event event to check
     * @return true if the event has started
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     *
     * @author Becca Irving
     * @since Mar 16 2026
     */
    private boolean isRegistrationClosed(@NonNull Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec > event.getRegistrationCloseEpochSec();
    }

    /**
     * Checks whether the action button should be disabled.
     *
     * @param event event to check
     * @return true if the button should be disabled
     *
     * @author Becca Irving
     * @since Mar 16 2026
     */
    private boolean shouldDisablePrimaryButton(@NonNull Event event) {
        return hasEventStarted(event) || isRegistrationClosed(event);
    }

    /**
     * Returns the correct status icon for the event state.
     *
     * @param event event to check
     * @return drawable resource id for the status icon
     *
     * @author Becca Irving
     * @since Mar 16 2026
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
     * @author Becca Irving
     * @since Mar 16 2026
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
     * @author Becca Irving
     * @since Mar 16 2026
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
     * @author Becca Irving
     * @since Mar 16 2026
     */
    private String formatTimeRange(long startEpochSeconds, long endEpochSeconds) {
        return formatTime(startEpochSeconds) + " – " + formatTime(endEpochSeconds);
    }
}
