package com.eventwise.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.eventwise.Event;
import com.eventwise.R;
import com.eventwise.Tag;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.database.SessionStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * This is the Fragment for organizers to create events.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

public class CreateEventFragment extends Fragment {

    private static final String ARG_EDIT_EVENT_ID = "arg_edit_event_id";

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
    private CheckBox checkPrivateEvent;

    private ImageView returnArrow;
    private ImageView inputEventPoster;
    private Button buttonCreateEvent;
    private TextView headerTitle;

    private byte[] selectedImageBytes = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private long eventStartEpochSec = 0;
    private long eventEndEpochSec = 0;
    private long registrationCloseEpochSec = 0;

    private boolean isEditMode = false;
    private String editEventId = "";
    private Event editingEvent;

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault());

    public CreateEventFragment() {
    }

    /**
     * Makes a create event fragment in edit mode for one existing event.
     *
     * @param eventId event id to edit
     * @return configured fragment
     */
    public static CreateEventFragment newEditInstance(@NonNull String eventId) {
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EDIT_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            editEventId = args.getString(ARG_EDIT_EVENT_ID, "");
            isEditMode = editEventId != null && !editEventId.trim().isEmpty();
        }

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.Source source = ImageDecoder.createSource(
                                        requireContext().getContentResolver(), uri);
                                bitmap = ImageDecoder.decodeBitmap(source);
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                        requireContext().getContentResolver(), uri);
                            }
                            inputEventPoster.setImageBitmap(bitmap);
                            inputEventPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 67, baos);
                            selectedImageBytes = baos.toByteArray();
                        } catch (IOException e) {
                            Log.d("CreateEvent", "Failed to read selected image", e);
                        }
                    }
                });
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
        checkPrivateEvent = view.findViewById(R.id.check_private_event);

        returnArrow = view.findViewById(R.id.return_arrow);
        inputEventPoster = view.findViewById(R.id.input_event_poster);
        buttonCreateEvent = view.findViewById(R.id.button_create_event);
        headerTitle = view.findViewById(R.id.header_title);

        checkLimitWaitlist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            limitWaitlist.setEnabled(isChecked);
            if (!isChecked) {
                limitWaitlist.setText("");
            }
            updateSubmitButtonState();
        });

        checkPrivateEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePrivateEventUiState();
            updateSubmitButtonState();
        });

        inputEventStart.setOnClickListener(v -> showDateTimePicker(inputEventStart, calendar -> {
            eventStartEpochSec = calendar.getTimeInMillis() / 1000L;
            updateSubmitButtonState();
        }));

        inputEventEnd.setOnClickListener(v -> showDateTimePicker(inputEventEnd, calendar -> {
            eventEndEpochSec = calendar.getTimeInMillis() / 1000L;
            updateSubmitButtonState();
        }));

        inputRegistrationEnd.setOnClickListener(v -> showDateTimePicker(inputRegistrationEnd, calendar -> {
            registrationCloseEpochSec = calendar.getTimeInMillis() / 1000L;
            updateSubmitButtonState();
        }));

        returnArrow.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inputEventPoster.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        if (isEditMode) {
            headerTitle.setText("Edit Event");
            loadEventForEdit();
        } else {
            headerTitle.setText("Create Event");
        }

        updatePrivateEventUiState();
        setupFormStateTracking();
        updateSubmitButtonState();

        buttonCreateEvent.setOnClickListener(v -> uploadEventToFirebase());
    }

    private void loadEventForEdit() {
        if (editEventId == null || editEventId.trim().isEmpty()) {
            Log.d("CreateEvent", "Missing event Id for edit mode");
            return;
        }

        OrganizerDatabaseManager organizerDBMan = new OrganizerDatabaseManager();
        organizerDBMan.getEventById(editEventId)
                .addOnSuccessListener(event -> {
                    editingEvent = event;
                    populateFieldsForEdit(event);
                })
                .addOnFailureListener(e ->
                        Log.d("CreateEvent", "Failed to load event for edit", e));
    }

    private void populateFieldsForEdit(@NonNull Event event) {
        inputEventName.setText(event.getName());
        inputEventDescription.setText(event.getDescription());
        inputEventLocation.setText(event.getLocationName());

        eventStartEpochSec = event.getEventStartEpochSec();
        eventEndEpochSec = event.getEventEndEpochSec();
        registrationCloseEpochSec = event.getRegistrationCloseEpochSec();

        inputEventStart.setText(displayDateFormat.format(eventStartEpochSec * 1000L));
        inputEventEnd.setText(displayDateFormat.format(eventEndEpochSec * 1000L));
        inputRegistrationEnd.setText(displayDateFormat.format(registrationCloseEpochSec * 1000L));

        inputAttendanceLimit.setText(String.valueOf(event.getMaxWinnersToSample()));

        Integer maxWaitingListSize = event.getMaxWaitingListSize();
        if (maxWaitingListSize != null) {
            checkLimitWaitlist.setChecked(true);
            limitWaitlist.setEnabled(true);
            limitWaitlist.setText(String.valueOf(maxWaitingListSize));
        } else {
            checkLimitWaitlist.setChecked(false);
            limitWaitlist.setEnabled(false);
            limitWaitlist.setText("");
        }

        checkGeoRequired.setChecked(event.isGeolocationRequired());
        checkPrivateEvent.setChecked(event.isPrivateEvent());
        checkPrivateEvent.setEnabled(false);
        checkPrivateEvent.setAlpha(0.5f);

        if (event.getTags() != null && !event.getTags().isEmpty() && event.getTags().get(0) != null) {
            String criteriaText = event.getTags().get(0).getKeyword();
            if ("General".equals(criteriaText)) {
                criteriaText = "";
            }
            inputCriteria.setText(criteriaText);
        }

        if (event.getPosterPath() != null && !event.getPosterPath().trim().isEmpty()) {
            Glide.with(requireContext())
                    .load(event.getPosterPath())
                    .centerCrop()
                    .into(inputEventPoster);
        }

        updatePrivateEventUiState();
        updateSubmitButtonState();
    }

    private void uploadEventToFirebase() {
        String name = inputEventName.getText().toString().trim();
        String description = inputEventDescription.getText().toString().trim();
        String criteria = inputCriteria.getText().toString().trim();
        String location = inputEventLocation.getText().toString().trim();
        String attendanceLimitString = inputAttendanceLimit.getText().toString().trim();
        String waitListLimitString = limitWaitlist.getText().toString().trim();

        boolean geolocationRequired = checkGeoRequired.isChecked();
        boolean limitWaitListChecked = checkLimitWaitlist.isChecked();
        boolean privateEvent = checkPrivateEvent.isChecked();

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

        if (eventStartEpochSec == 0) {
            inputEventStart.setError("Required");
            return;
        }

        if (eventEndEpochSec == 0) {
            inputEventEnd.setError("Required");
            return;
        }

        if (registrationCloseEpochSec == 0) {
            inputRegistrationEnd.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(attendanceLimitString)) {
            inputAttendanceLimit.setError("Required");
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

        SessionStore sessionStore = new SessionStore(requireContext());
        String organizerProfileId = sessionStore.getOrganizerProfileId();
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

        OrganizerDatabaseManager organizerDBMan = new OrganizerDatabaseManager();

        if (isEditMode) {
            if (editingEvent == null) {
                Log.d("CreateEvent", "Cannot update because editing event was not loaded");
                return;
            }

            editingEvent.setName(name);
            editingEvent.setDescription(description);
            editingEvent.setLocationName(location);
            editingEvent.setTags(tags);
            editingEvent.setEventStartEpochSec(eventStartEpochSec);
            editingEvent.setEventEndEpochSec(eventEndEpochSec);
            editingEvent.setRegistrationCloseEpochSec(registrationCloseEpochSec);
            editingEvent.setGeolocationRequired(geolocationRequired);
            editingEvent.setMaxWaitingListSize(maxWaitingListSize);
            editingEvent.setMaxWinnersToSample(maxWinnersToSample);
            editingEvent.setPrivateEvent(privateEvent);

            organizerDBMan.updateEvent(editingEvent)
                    .addOnSuccessListener(unused -> {
                        Log.d("CreateEvent", "Event updated successfully");

                        if (selectedImageBytes != null) {
                            organizerDBMan.updateEventPoster(editingEvent.getEventId(), selectedImageBytes)
                                    .addOnSuccessListener(path -> {
                                        Log.d("CreateEvent", "Updated poster uploaded: " + path);
                                        finishAfterSave(editingEvent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("CreateEvent", "Updated poster upload failed", e);
                                        finishAfterSave(editingEvent);
                                    });
                        } else {
                            finishAfterSave(editingEvent);
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.d("CreateEvent", "Event failed to update", e));

            return;
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
        event.setPrivateEvent(privateEvent);

        organizerDBMan.addEvent(event)
                .addOnSuccessListener(param -> {
                    Log.d("CreateEvent", "Event added successfully to Firebase");

                    if (selectedImageBytes != null) {
                        organizerDBMan.uploadEventPoster(event.getEventId(), selectedImageBytes)
                                .addOnSuccessListener(path -> {
                                    Log.d("CreateEvent", "Poster uploaded: " + path);
                                    finishAfterSave(event);
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("CreateEvent", "Poster upload failed", e);
                                    finishAfterSave(event);
                                });
                    } else {
                        finishAfterSave(event);
                    }
                })
                .addOnFailureListener(param ->
                        Log.d("CreateEvent", "Event failed to add to Firebase"));
    }

    private void finishAfterSave(@NonNull Event event) {
        if (!isAdded()) {
            return;
        }

        showCoOrganizerPrompt(event);
    }

    private void showCoOrganizerPrompt(@NonNull Event event) {
        showStyledDecisionDialog("Invite Co-organizer","Do you want to invite a co-organizer?","Yes","No",
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.organizer_fragment_container,
                                        InviteProfileSelectionFragment.newCoOrganizerInviteInstance(event.getEventId(),event.isPrivateEvent()))
                                .addToBackStack(null)
                                .commit();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        continueAfterCoOrganizerChoice(event);
                    }
                }
        );
    }

    private void continueAfterCoOrganizerChoice(@NonNull Event event) {
        if (!isAdded()) {
            return;
        }

        if (event.isPrivateEvent()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.organizer_fragment_container,
                            InviteProfileSelectionFragment.newPrivateInviteInstance(event.getEventId())
                    )
                    .addToBackStack(null)
                    .commit();
            return;
        }

        getParentFragmentManager().popBackStack();
    }

    private void setupFormStateTracking() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        inputEventName.addTextChangedListener(watcher);
        inputEventDescription.addTextChangedListener(watcher);
        inputCriteria.addTextChangedListener(watcher);
        inputEventLocation.addTextChangedListener(watcher);
        inputAttendanceLimit.addTextChangedListener(watcher);
        limitWaitlist.addTextChangedListener(watcher);
    }

    private void updateSubmitButtonState() {
        boolean enabled = isFormReadyForSubmit() && (!isEditMode || hasFormChangedFromOriginal());
        buttonCreateEvent.setEnabled(enabled);
        buttonCreateEvent.setAlpha(enabled ? 1.0f : 0.5f);
        buttonCreateEvent.setTextColor(ContextCompat.getColor(requireContext(), R.color.lighter_green));
    }

    private void updatePrivateEventUiState() {
        if (isEditMode) {
            if (checkPrivateEvent.isChecked()) {
                buttonCreateEvent.setText("Update Private Event");
            } else {
                buttonCreateEvent.setText("Update Event");
            }
            return;
        }

        if (checkPrivateEvent.isChecked()) {
            buttonCreateEvent.setText("Create Private Event");
        } else {
            buttonCreateEvent.setText("Create Event");
        }
    }

    private boolean isFormReadyForSubmit() {
        String name = inputEventName.getText().toString().trim();
        String description = inputEventDescription.getText().toString().trim();
        String location = inputEventLocation.getText().toString().trim();
        String attendanceLimitString = inputAttendanceLimit.getText().toString().trim();
        String waitListLimitString = limitWaitlist.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            return false;
        }

        if (TextUtils.isEmpty(location)) {
            return false;
        }

        if (eventStartEpochSec == 0) {
            return false;
        }

        if (eventEndEpochSec == 0) {
            return false;
        }

        if (registrationCloseEpochSec == 0) {
            return false;
        }

        if (TextUtils.isEmpty(attendanceLimitString)) {
            return false;
        }

        try {
            int attendanceLimit = Integer.parseInt(attendanceLimitString);
            if (attendanceLimit <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        if (checkLimitWaitlist.isChecked()) {
            if (TextUtils.isEmpty(waitListLimitString)) {
                return false;
            }

            try {
                int waitListLimit = Integer.parseInt(waitListLimitString);
                if (waitListLimit < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private boolean hasFormChangedFromOriginal() {
        if (!isEditMode || editingEvent == null) {
            return false;
        }

        String currentName = inputEventName.getText().toString().trim();
        String currentDescription = inputEventDescription.getText().toString().trim();
        String currentCriteria = inputCriteria.getText().toString().trim();
        String currentLocation = inputEventLocation.getText().toString().trim();
        String currentAttendanceLimit = inputAttendanceLimit.getText().toString().trim();
        String currentWaitlistLimit = limitWaitlist.getText().toString().trim();

        String originalCriteria = "";
        if (editingEvent.getTags() != null
                && !editingEvent.getTags().isEmpty()
                && editingEvent.getTags().get(0) != null
                && editingEvent.getTags().get(0).getKeyword() != null) {
            originalCriteria = editingEvent.getTags().get(0).getKeyword().trim();
            if ("General".equals(originalCriteria)) {
                originalCriteria = "";
            }
        }

        String originalAttendanceLimit = String.valueOf(editingEvent.getMaxWinnersToSample());

        Integer originalMaxWaitingListSize = editingEvent.getMaxWaitingListSize();
        boolean originalWaitlistLimited = originalMaxWaitingListSize != null;
        String originalWaitlistLimit = originalWaitlistLimited
                ? String.valueOf(originalMaxWaitingListSize)
                : "";

        if (!currentName.equals(editingEvent.getName() == null ? "" : editingEvent.getName().trim())) {
            return true;
        }

        if (!currentDescription.equals(editingEvent.getDescription() == null ? "" : editingEvent.getDescription().trim())) {
            return true;
        }

        if (!currentLocation.equals(editingEvent.getLocationName() == null ? "" : editingEvent.getLocationName().trim())) {
            return true;
        }

        if (!currentCriteria.equals(originalCriteria)) {
            return true;
        }

        if (eventStartEpochSec != editingEvent.getEventStartEpochSec()) {
            return true;
        }

        if (eventEndEpochSec != editingEvent.getEventEndEpochSec()) {
            return true;
        }

        if (registrationCloseEpochSec != editingEvent.getRegistrationCloseEpochSec()) {
            return true;
        }

        if (!currentAttendanceLimit.equals(originalAttendanceLimit)) {
            return true;
        }

        if (checkGeoRequired.isChecked() != editingEvent.isGeolocationRequired()) {
            return true;
        }

        if (checkPrivateEvent.isChecked() != editingEvent.isPrivateEvent()) {
            return true;
        }

        if (checkLimitWaitlist.isChecked() != originalWaitlistLimited) {
            return true;
        }

        if (checkLimitWaitlist.isChecked() && !currentWaitlistLimit.equals(originalWaitlistLimit)) {
            return true;
        }

        return selectedImageBytes != null;
    }

    private interface OnDateTimeSelectedListener {
        void onDateTimeSelected(Calendar calendar);
    }

    private void showDateTimePicker(EditText dateField, OnDateTimeSelectedListener listener) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selected.set(Calendar.MINUTE, minute);
                                selected.set(Calendar.SECOND, 0);

                                dateField.setText(displayDateFormat.format(selected.getTime()));
                                dateField.setError(null);
                                listener.onDateTimeSelected(selected);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false);
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showStyledDecisionDialog(String title,String message,String positiveText,String negativeText,@Nullable Runnable onPositive, @Nullable Runnable onNegative) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.widget_custom_dialog, null, false);

        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);
        Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);

        titleText.setText(title);
        messageText.setText(message);
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        negativeButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNegative != null) {
                onNegative.run();
            }
        });

        positiveButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) {
                onPositive.run();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
