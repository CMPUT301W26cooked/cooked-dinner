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

import com.bumptech.glide.Glide;
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

    private String eventId;
    private Event currentEvent;

    public OrganizerEventDetailFragment() {
    }

    /**
     * Makes a new organizer detail fragment for one event.
     *
     */
    public static OrganizerEventDetailFragment newInstance(@NonNull String eventId) {
        OrganizerEventDetailFragment fragment = new OrganizerEventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
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
        loadEvent();
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
    }

    /**
     * Fills the detail page views with event data.
     *
     * @param view root view
     */
    private void bindViews(@NonNull View view) {
        ImageView backButton = view.findViewById(R.id.back_button);
        ImageView qrCode = view.findViewById(R.id.qr_code);
        ImageView eventPoster = view.findViewById(R.id.event_poster);

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

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        viewEntrantsButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, OrganizerEntrantActionsFragment.newInstance(eventId))
                    .addToBackStack(null)
                    .commit();
        });

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

        if (currentEvent == null) {
            return;
        }

        boolean eventOver = hasEventStarted();

        detailTitle.setText(currentEvent.isPrivateEvent() ? "Private Event Details" : "Event Details");
        eventNameText.setText(currentEvent.getName());
        eventOrganization.setText("Organization Name");
        eventStatus.setText("Open");
        eventDescriptionText.setText(currentEvent.getDescription());

        try {
            android.graphics.Bitmap qrBitmap =
                    new com.eventwise.QRCodeEncoder(currentEvent.getEventId(), 600, 600, null)
                            .encodeAsBitmap();
            qrCode.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            android.util.Log.e("OrganizerEventDetail", "Failed to generate QR code", e);
            qrCode.setImageDrawable(null);
        }

        eventDate.setText(formatDate(currentEvent.getEventStartEpochSec()));
        eventTime.setText(formatTimeRange(
                currentEvent.getEventStartEpochSec(),
                currentEvent.getEventEndEpochSec()
        ));
        eventSpots.setText("•  " + currentEvent.getMaxWinnersToSample() + " event spots");
        eventWaitlisted.setText("•  " + currentEvent.getWaitingListCount() + " on the wait list");
        eventRegistered.setText("•  " + currentEvent.getEnrolledCount() + " registered");

        //Update Poster
        Glide.with(eventPoster.getContext())
                .load(currentEvent.getPosterPath())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(eventPoster);

        lotteryCloseDate.setText(formatDate(currentEvent.getRegistrationCloseEpochSec()));
        lotteryCloseTime.setText(formatTime(currentEvent.getRegistrationCloseEpochSec()));
        registrationCloseDate.setText(formatDate(currentEvent.getRegistrationCloseEpochSec()));
        registrationCloseTime.setText(formatTime(currentEvent.getRegistrationCloseEpochSec()));

        eventLocationName.setText(TextUtils.isEmpty(currentEvent.getLocationName()) ? "No location" : currentEvent.getLocationName());
        eventLocationCity.setText("");
        eventGuidelines.setText("Rules and regulations limitations and considerations and other such things");

        editEventButton.setEnabled(!eventOver);
        editEventButton.setAlpha(eventOver ? 0.5f : 1.0f);

        cancelEventButton.setEnabled(!eventOver);
        cancelEventButton.setAlpha(eventOver ? 0.5f : 1.0f);
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
        if (currentEvent == null) {
            return false;
        }

        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= currentEvent.getEventStartEpochSec();
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

    private void loadEvent() {
        if (eventId == null || eventId.trim().isEmpty() || getView() == null) {
            return;
        }

        EventSearcherDatabaseManager db = new EventSearcherDatabaseManager();
        db.getEvents()
                .addOnSuccessListener(events -> {
                    currentEvent = null;

                    for (Event event : events) {
                        if (event != null && eventId.equals(event.getEventId())) {
                            currentEvent = event;
                            break;
                        }
                    }

                    if (currentEvent != null && getView() != null) {
                        bindViews(getView());
                    }
                });
    }
}
