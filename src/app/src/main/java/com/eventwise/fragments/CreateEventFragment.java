package com.eventwise.fragments;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.eventwise.R;
import com.eventwise.Tag;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.Event;

import java.util.ArrayList;

/**
 * This is the Fragment for organizers to create events.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

/*TODO: Need to fix the date format so its more usable has to be EPOCH or whatever.
 */
public class CreateEventFragment extends Fragment {

    private EditText inputEventName;
    private EditText inputEventDescription;
    private EditText inputCriteria;
    private EditText inputEventLocation;
    private EditText inputEventStart;
    private EditText inputEventEnd;
    private EditText inputRegistrationEnd;
    private EditText inputAttendanceLimit;
    private EditText limitWaitlist;

    private CheckBox checkLimitWaitlist;
    private CheckBox checkGeoRequired;

    private ImageView returnArrow;
    private ImageView inputEventPoster;
    private Button buttonCreateEvent;

    public CreateEventFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputEventName = view.findViewById(R.id.input_event_name);
        inputEventDescription = view.findViewById(R.id.input_event_description);
        inputCriteria = view.findViewById(R.id.input_criteria);
        inputEventLocation = view.findViewById(R.id.input_event_location);
        inputEventStart = view.findViewById(R.id.input_event_start);
        inputEventEnd = view.findViewById(R.id.input_event_end);
        inputRegistrationEnd = view.findViewById(R.id.input_registration_end);
        inputAttendanceLimit = view.findViewById(R.id.input_attendance_limit);
        limitWaitlist = view.findViewById(R.id.limit_waitlist);

        checkLimitWaitlist = view.findViewById(R.id.check_limit_waitlist);
        checkGeoRequired = view.findViewById(R.id.check_geo_required);

        returnArrow = view.findViewById(R.id.return_arrow);
        inputEventPoster = view.findViewById(R.id.input_event_poster);
        buttonCreateEvent = view.findViewById(R.id.button_create_event);

        checkLimitWaitlist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            limitWaitlist.setEnabled(isChecked);
            if (!isChecked) {
                limitWaitlist.setText("");
            }
        });

        returnArrow.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        buttonCreateEvent.setOnClickListener(v -> uploadEventToFirebase());
    }

        private void uploadEventToFirebase() {
            String name = inputEventName.getText().toString().trim();
            String description = inputEventDescription.getText().toString().trim();
            String criteria = inputCriteria.getText().toString().trim();
            String location = inputEventLocation.getText().toString().trim();
            String eventStartString = inputEventStart.getText().toString().trim();
            String eventEndString = inputEventEnd.getText().toString().trim();
            String registrationEndString = inputRegistrationEnd.getText().toString().trim();
            String attendanceLimitString = inputAttendanceLimit.getText().toString().trim();
            String waitListLimitString = limitWaitlist.getText().toString().trim();

            boolean geolocationRequired = checkGeoRequired.isChecked();
            boolean limitWaitListChecked = checkLimitWaitlist.isChecked();

            if (TextUtils.isEmpty(name)) {
                inputEventName.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(description)) {
                inputEventDescription.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(location)) {
                inputEventLocation.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(eventStartString)) {
                inputEventStart.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(eventEndString)) {
                inputEventEnd.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(registrationEndString)) {
                inputRegistrationEnd.setError("Required");
                return;
            }

            if (TextUtils.isEmpty(attendanceLimitString)) {
                inputAttendanceLimit.setError("Required");
                return;
            }

            long eventStartEpochSec;
            long eventEndEpochSec;
            long registrationCloseEpochSec;

            try {
                eventStartEpochSec = Long.parseLong(eventStartString);
                eventEndEpochSec = Long.parseLong(eventEndString);
                registrationCloseEpochSec = Long.parseLong(registrationEndString);
            } catch (NumberFormatException e) {
                Log.d("CreateEvent", "Date fields must be epoch seconds");
                inputEventStart.setError("Use epoch seconds");
                inputEventEnd.setError("Use epoch seconds");
                inputRegistrationEnd.setError("Use epoch seconds");
                return;
            }

            int maxWinnersToSample;
            try {
                maxWinnersToSample = Integer.parseInt(attendanceLimitString);
            } catch (NumberFormatException e) {
                inputAttendanceLimit.setError("Enter a number");
                return;
            }

            Integer maxWaitingListSize = null;
            if (limitWaitListChecked) {
                if (TextUtils.isEmpty(waitListLimitString)) {
                    limitWaitlist.setError("Required if wait list is limited");
                    return;
                }

                try {
                    maxWaitingListSize = Integer.parseInt(waitListLimitString);
                } catch (NumberFormatException e) {
                    limitWaitlist.setError("Enter a number");
                    return;
                }
            }

            // Placeholder values for fields not yet wired from UI/user profile
            String organizerProfileId = "TEMP_ORGANIZER_ID";
            double price = 0.00;
            long registrationOpenEpochSec = System.currentTimeMillis() / 1000L;
            String posterPath = null;
            String qrCodeId = null;

            ArrayList<Tag> tags = new ArrayList<>();
            if (criteria.isEmpty()) {
                tags.add(new Tag("General", "General"));
            } else {
                tags.add(new Tag("General", criteria));
            }

            Event event = new Event(
                    organizerProfileId,
                    name,
                    description,
                    price,
                    location,
                    tags,
                    eventStartEpochSec,
                    eventEndEpochSec,
                    registrationOpenEpochSec,
                    registrationCloseEpochSec,
                    geolocationRequired,
                    maxWaitingListSize,
                    maxWinnersToSample,
                    posterPath,
                    qrCodeId
            );
            OrganizerDatabaseManager organizerDBMan = new OrganizerDatabaseManager();
            organizerDBMan.addEvent(event)
                    .addOnSuccessListener(param -> {
                        Log.d("CreateEvent", "Event added successfully to Firebase");
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(param -> {
                        Log.d("CreateEvent", "Event failed to add to Firebase");
                    });
    }
}
