package com.eventwise.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.CommentBottomSheet;
import com.eventwise.Event;
import com.eventwise.ProfileType;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Detail page for an organizer viewing one event.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class OrganizerEventDetailFragment extends Fragment {

    /**
     * TODO (OrganizerEventDetailFragment.java)
     * - Replace placeholder organization and guideline text with real event data later.
     * - Hook the edit button into the edit event page later.
     * - Add tests for cancel and view entrants navigation later.
     */

    public static final String REQUEST_KEY_EVENT_CANCELLED = "organizer_request_key_event_cancelled";
    public static final String BUNDLE_KEY_DELETED_EVENT_ID = "organizer_bundle_key_deleted_event_id";

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_EVENT_NAME = "arg_event_name";
    private static final String ARG_EVENT_DESCRIPTION = "arg_event_description";
    private static final String ARG_EVENT_LOCATION = "arg_event_location";
    private static final String ARG_EVENT_START = "arg_event_start";
    private static final String ARG_EVENT_END = "arg_event_end";
    private static final String ARG_REG_CLOSE = "arg_reg_close";
    private static final String ARG_MAX_SPOTS = "arg_max_spots";
    private static final String ARG_WAITLISTED = "arg_waitlisted";
    private static final String ARG_REGISTERED = "arg_registered";

    private String eventId;
    private String eventName;
    private String eventDescription;
    private String eventLocation;
    private long eventStart;
    private long eventEnd;
    private long registrationClose;
    private int maxSpots;
    private int waitlistedCount;
    private int registeredCount;

    public OrganizerEventDetailFragment() {
    }

    /**
     * Makes a new organizer detail fragment for one event.
     *
     * @param event event to display
     * @return configured fragment
     */
    public static OrganizerEventDetailFragment newInstance(Event event) {
        OrganizerEventDetailFragment fragment = new OrganizerEventDetailFragment();
        Bundle args = new Bundle();

        args.putString(ARG_EVENT_ID, event.getEventId());
        args.putString(ARG_EVENT_NAME, event.getName());
        args.putString(ARG_EVENT_DESCRIPTION, event.getDescription());
        args.putString(ARG_EVENT_LOCATION, event.getLocationName());
        args.putLong(ARG_EVENT_START, event.getEventStartEpochSec());
        args.putLong(ARG_EVENT_END, event.getEventEndEpochSec());
        args.putLong(ARG_REG_CLOSE, event.getRegistrationCloseEpochSec());
        args.putInt(ARG_MAX_SPOTS, event.getMaxWinnersToSample());
        args.putInt(ARG_WAITLISTED, event.getWaitingListCount());
        args.putInt(ARG_REGISTERED, event.getEnrolledCount());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readArgs();
        bindViews(view);
    }

    /**
     * Reads arguments passed into this fragment.
     */
    private void readArgs() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        eventId = args.getString(ARG_EVENT_ID, "");
        eventName = args.getString(ARG_EVENT_NAME, "");
        eventDescription = args.getString(ARG_EVENT_DESCRIPTION, "");
        eventLocation = args.getString(ARG_EVENT_LOCATION, "");
        eventStart = args.getLong(ARG_EVENT_START, 0L);
        eventEnd = args.getLong(ARG_EVENT_END, 0L);
        registrationClose = args.getLong(ARG_REG_CLOSE, 0L);
        maxSpots = args.getInt(ARG_MAX_SPOTS, 0);
        waitlistedCount = args.getInt(ARG_WAITLISTED, 0);
        registeredCount = args.getInt(ARG_REGISTERED, 0);
    }

    /**
     * Fills the detail page views with event data.
     *
     * @param view root view
     */
    private void bindViews(@NonNull View view) {
        ImageView backButton = view.findViewById(R.id.back_button);

        TextView detailTitle = view.findViewById(R.id.event_detail_title);
        TextView eventNameText = view.findViewById(R.id.event_name);
        TextView eventOrganization = view.findViewById(R.id.event_organization);
        TextView eventStatus = view.findViewById(R.id.event_status);
        TextView eventDescriptionText = view.findViewById(R.id.event_description);

        TextView eventDate = view.findViewById(R.id.event_date);
        TextView eventTime = view.findViewById(R.id.event_time);
        TextView eventSpots = view.findViewById(R.id.event_spots);
        TextView eventWaitlisted = view.findViewById(R.id.event_waitlisted);
        TextView eventRegistered = view.findViewById(R.id.event_registered);

        TextView lotteryCloseDate = view.findViewById(R.id.lottery_close_date);
        TextView lotteryCloseTime = view.findViewById(R.id.lottery_close_time);
        TextView registrationCloseDate = view.findViewById(R.id.registration_close_date);
        TextView registrationCloseTime = view.findViewById(R.id.registration_close_time);

        TextView eventLocationName = view.findViewById(R.id.event_location_name);
        TextView eventLocationCity = view.findViewById(R.id.event_location_city);
        TextView eventGuidelines = view.findViewById(R.id.event_guidelines);

        Button viewEntrantsButton = view.findViewById(R.id.view_entrants_button);
        Button editEventButton = view.findViewById(R.id.edit_event_button);
        Button cancelEventButton = view.findViewById(R.id.cancel_event_button);
        Button eventCommentButton = view.findViewById(R.id.event_comment_button);

        boolean eventOver = hasEventStarted();

        detailTitle.setText("Event Details");
        eventNameText.setText(eventName);
        eventOrganization.setText("Organization Name");
        eventStatus.setText("Open");
        eventDescriptionText.setText(eventDescription);

        eventDate.setText(formatDate(eventStart));
        eventTime.setText(formatTimeRange(eventStart, eventEnd));
        eventSpots.setText("•  " + maxSpots + " event spots");
        eventWaitlisted.setText("•  " + waitlistedCount + " on the wait list");
        eventRegistered.setText("•  " + registeredCount + " registered");

        lotteryCloseDate.setText(formatDate(registrationClose));
        lotteryCloseTime.setText(formatTime(registrationClose));
        registrationCloseDate.setText(formatDate(registrationClose));
        registrationCloseTime.setText(formatTime(registrationClose));

        eventLocationName.setText(TextUtils.isEmpty(eventLocation) ? "No location" : eventLocation);
        eventLocationCity.setText("");
        eventGuidelines.setText("Rules and regulations limitations and considerations and other such things");

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        viewEntrantsButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, OrganizerEntrantActionsFragment.newInstance(eventId))
                    .addToBackStack(null)
                    .commit();
        });

        editEventButton.setEnabled(!eventOver);
        editEventButton.setAlpha(eventOver ? 0.5f : 1.0f);

        cancelEventButton.setEnabled(!eventOver);
        cancelEventButton.setAlpha(eventOver ? 0.5f : 1.0f);

        editEventButton.setOnClickListener(v -> {
            if (eventId == null || eventId.trim().isEmpty()) {
                return;
            }

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.organizer_fragment_container,
                            CreateEventFragment.newEditInstance(eventId)
                    )
                    .addToBackStack(null)
                    .commit();
        });

        eventCommentButton.setOnClickListener(v -> {

            if (getParentFragmentManager().findFragmentByTag("CommentBottomSheet") == null) {
                CommentBottomSheet sheet = new CommentBottomSheet();
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                args.putString("profileType", ProfileType.ORGANIZER.name());
                sheet.setArguments(args);
                sheet.show(getParentFragmentManager(), "CommentBottomSheet");
            }

        });
        cancelEventButton.setOnClickListener(v -> cancelEvent());
    }

    /**
     * Deletes the event from the detail page and sends the result back.
     */
    private void cancelEvent() {
        if (eventId == null || eventId.trim().isEmpty()) {
            getParentFragmentManager().popBackStack();
            return;
        }

        Event eventToDelete = new Event();
        eventToDelete.setEventId(eventId);

        EventSearcherDatabaseManager eventSearcherDatabaseManager = new EventSearcherDatabaseManager();
        eventSearcherDatabaseManager.deleteEvent(eventToDelete)
                .addOnSuccessListener(unused -> {
                    Bundle result = new Bundle();
                    result.putString(BUNDLE_KEY_DELETED_EVENT_ID, eventId);
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY_EVENT_CANCELLED, result);
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> getParentFragmentManager().popBackStack());
    }

    /**
     * Returns true when this event should be treated as over should be same as module behavior
     *
     * @return true if the event has started
     */
    private boolean hasEventStarted() {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= eventStart;
    }

    /**
     * Formats epoch seconds as a date string.
     *
     * @param epochSeconds time in epoch seconds
     * @return formatted date
     */
    private String formatDate(long epochSeconds) {
        if (epochSeconds <= 0) {
            return "";
        }
        Date date = new Date(epochSeconds * 1000L);
        return new SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(date);
    }

    /**
     * Formats epoch seconds as a time string.
     *
     * @param epochSeconds time in epoch seconds
     * @return formatted time
     */
    private String formatTime(long epochSeconds) {
        if (epochSeconds <= 0) {
            return "";
        }
        Date date = new Date(epochSeconds * 1000L);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(date);
    }

    /**
     * Formats a start and end time into one range string.
     *
     * @param startEpochSeconds start time
     * @param endEpochSeconds end time
     * @return formatted time range
     */
    private String formatTimeRange(long startEpochSeconds, long endEpochSeconds) {
        return formatTime(startEpochSeconds) + " – " + formatTime(endEpochSeconds);
    }
}
