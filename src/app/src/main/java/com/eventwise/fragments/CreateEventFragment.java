package com.eventwise.fragments;
import android.os.Bundle;
import android.text.TextUtils;
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

/**
 * This is the Fragment for organizers to create events.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 */

/*TODO: THis class still needs firebase upload logic. It also still needs Error handling for blank
 *  fields and error handling. We also need to implement a better way to deal with dates then typing
 * epoch
 */
public class CreateEventFragment extends Fragment {

    private ImageView returnArrow;
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

        returnArrow = view.findViewById(R.id.return_arrow);
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
        inputEventPoster = view.findViewById(R.id.event_poster);
        buttonCreateEvent = view.findViewById(R.id.button_create_event);

        returnArrow.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        checkLimitWaitlist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            limitWaitlist.setEnabled(isChecked);
            if (!isChecked) {
                limitWaitlist.setText("");
            }
        });

        buttonCreateEvent.setOnClickListener(v -> {
            String title = inputEventName.getText().toString().trim();
            String description = inputEventDescription.getText().toString().trim();
            String criteria = inputCriteria.getText().toString().trim();
            String location = inputEventLocation.getText().toString().trim();
            String eventStart = inputEventStart.getText().toString().trim();
            String eventEnd = inputEventEnd.getText().toString().trim();
            String registrationEnd = inputRegistrationEnd.getText().toString().trim();
            String attendanceLimit = inputAttendanceLimit.getText().toString().trim();
            String waitlistLimit = limitWaitlist.getText().toString().trim();
            boolean limitWaitListChecked = checkLimitWaitlist.isChecked();
            boolean geoRequired = checkGeoRequired.isChecked();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location)) {
                inputEventName.setError(TextUtils.isEmpty(title) ? "Required" : null);
                inputEventDescription.setError(TextUtils.isEmpty(description) ? "Required" : null);
                inputEventLocation.setError(TextUtils.isEmpty(location) ? "Required" : null);
                return;
            }
        });
    }
}