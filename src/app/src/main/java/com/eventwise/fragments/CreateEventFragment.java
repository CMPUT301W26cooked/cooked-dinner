package com.eventwise.fragments;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.EventEntrantStatus;
import com.eventwise.Notification;
import com.eventwise.R;
import com.eventwise.Tag;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.NotificationDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.Event;
import com.eventwise.database.SessionStore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private byte[] selectedImageBytes = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private long eventStartEpochSec = 0;
    private long eventEndEpochSec = 0;
    private long registrationCloseEpochSec = 0;

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault());

    public CreateEventFragment() {
    }

    // reference: https://developer.android.com/training/data-storage/shared/photo-picker
    // used to pick an image from the gallery
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        returnArrow = view.findViewById(R.id.return_arrow);
        inputEventPoster = view.findViewById(R.id.input_event_poster);
        buttonCreateEvent = view.findViewById(R.id.button_create_event);

        checkLimitWaitlist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            limitWaitlist.setEnabled(isChecked);
            if (!isChecked) {
                limitWaitlist.setText("");
            }
        });

        inputEventStart.setOnClickListener(v -> showDateTimePicker(inputEventStart, calendar -> {
            eventStartEpochSec = calendar.getTimeInMillis() / 1000L;
        }));

        inputEventEnd.setOnClickListener(v -> showDateTimePicker(inputEventEnd, calendar -> {
            eventEndEpochSec = calendar.getTimeInMillis() / 1000L;
        }));

        inputRegistrationEnd.setOnClickListener(v -> showDateTimePicker(inputRegistrationEnd, calendar -> {
            registrationCloseEpochSec = calendar.getTimeInMillis() / 1000L;
        }));

        returnArrow.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inputEventPoster.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        buttonCreateEvent.setOnClickListener(v ->
                uploadEventToFirebase());
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

        // Placeholder values for fields not yet wired from UI/user profile
        SessionStore sessionStore = new SessionStore(requireContext());
        String organizerProfileId = sessionStore.getOrCreateDeviceId();
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
                    sendInviteNotifications(event);

                    // ====== New code start ======
                    Log.d("ProfileLinking", "Event created with organizer Id: " +
                            event.getOrganizerProfileId());
                    Log.d("ProfileLinking", "Event Id: " + event.getEventId());
                    Log.d("ProfileLinking", "Successfully linked profile to created event");
                    // ====== New code end ======

                    if (selectedImageBytes != null) {
                        organizerDBMan.uploadEventPoster(event.getEventId(), selectedImageBytes, requireContext())
                                .addOnSuccessListener(path -> {
                                    Log.d("CreateEvent", "Poster uploaded: " + path);
                                    getParentFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("CreateEvent", "Poster upload failed", e);
                                    getParentFragmentManager().popBackStack();
                                });
                    } else {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(param -> {
                    Log.d("CreateEvent", "Event failed to add to Firebase");
                });
    }

    private interface OnDateTimeSelectedListener {
        void onDateTimeSelected(Calendar calendar);
    }

    // reference: https://github.com/Pritish-git/date-and-time-picker-dialog
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

    private void sendInviteNotifications(Event event) {
        NotificationDatabaseManager notificationDB = new NotificationDatabaseManager();
        long now = System.currentTimeMillis() / 1000L;

        notificationDB.getAllEntrantProfileIds()
                .addOnSuccessListener(entrantIds -> {
                    if (entrantIds == null || entrantIds.isEmpty()) {
                        Log.d("Notification", "No entrant profiles found to notify");
                        return;
                    }

                    Notification entrantNotification = new Notification();
                    entrantNotification.setNotificationId(java.util.UUID.randomUUID().toString());
                    entrantNotification.setRecipientRole(Notification.RecipientRole.ENTRANT);
                    entrantNotification.setEntrantIds(entrantIds);
                    entrantNotification.setMessageTitle("Event Invite");
                    entrantNotification.setMessageBody("You've been invited to " + event.getName());
                    entrantNotification.setType(Notification.NotificationType.OTHER);
                    entrantNotification.setTimestamp(now);

                    notificationDB.createNotification(entrantNotification)
                            .addOnSuccessListener(unused ->
                                    Log.d("Notification", "Entrant invite notification created"))
                            .addOnFailureListener(e ->
                                    Log.e("Notification", "Bulk entrant notification failed", e));
                })
                .addOnFailureListener(e ->
                        Log.e("Notification", "Failed to fetch entrant profile IDs", e));
    }

    private void sendInvite(Event event) {
        NotificationDatabaseManager notificationDB = new NotificationDatabaseManager();
        OrganizerDatabaseManager organizerDBMan = new OrganizerDatabaseManager();
        long now = System.currentTimeMillis() / 1000L;

        notificationDB.getAllEntrantProfileIds()
                .addOnSuccessListener(entrantIds -> {
                    if (entrantIds == null || entrantIds.isEmpty()) {
                        Log.d("Notification", "No entrant profiles found to notify");
                        return;
                    }

                    for (String entrantId : entrantIds) {
                        boolean alreadyInvited =
                                event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(entrantId);

                        if (!alreadyInvited) {
                            organizerDBMan.updateEntrantStatusInEvent(
                                            entrantId,
                                            event.getEventId(),
                                            EventEntrantStatus.INVITED,
                                            now
                                    ).addOnSuccessListener(unused ->
                                            Log.d("Invite", "Invited entrant: " + entrantId))
                                    .addOnFailureListener(e ->
                                            Log.e("Invite", "Failed to invite entrant: " + entrantId, e));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Invite", "Failed to fetch entrant profile IDs", e));
    }
}
