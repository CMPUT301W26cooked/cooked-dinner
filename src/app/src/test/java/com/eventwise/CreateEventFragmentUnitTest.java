package com.eventwise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import org.robolectric.Robolectric;

import com.eventwise.Event;
import com.eventwise.Tag;
import com.eventwise.R;
import com.eventwise.fragments.CreateEventFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)

/**
 * Unit Tests for CreateEventFragment
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-06
 */
public class CreateEventFragmentUnitTest {

    private CreateEventFragment fragment;

    @Before
    public void setUp() {
        FragmentActivity activity =
                Robolectric.buildActivity(FragmentActivity.class).setup().get();

        FrameLayout container = new FrameLayout(activity);
        container.setId(R.id.organizer_fragment_container);
        activity.setContentView(container);

        fragment = new CreateEventFragment();

        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.organizer_fragment_container, fragment)
                .commitNow();
    }

    @Test
    public void isFormReadyForSubmit_returnsFalse_whenAllFieldsEmpty() throws Exception {
        boolean result = invokeIsFormReadyForSubmit();

        assertFalse(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsTrue_whenRequiredFieldsValid() throws Exception {
        fillValidCreateForm();

        boolean result = invokeIsFormReadyForSubmit();

        assertTrue(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsFalse_whenAttendanceLimitIsZero() throws Exception {
        fillValidCreateForm();
        ((EditText) getField("inputAttendanceLimit")).setText("0");

        boolean result = invokeIsFormReadyForSubmit();

        assertFalse(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsFalse_whenAttendanceLimitIsNotANumber() throws Exception {
        fillValidCreateForm();
        ((EditText) getField("inputAttendanceLimit")).setText("abc");

        boolean result = invokeIsFormReadyForSubmit();

        assertFalse(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsFalse_whenWaitlistCheckedButBlank() throws Exception {
        fillValidCreateForm();

        CheckBox checkLimitWaitlist = getCheckBox("checkLimitWaitlist");
        EditText limitWaitlist = getEditText("limitWaitlist");

        checkLimitWaitlist.setChecked(true);
        limitWaitlist.setText("");

        boolean result = invokeIsFormReadyForSubmit();

        assertFalse(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsFalse_whenWaitlistIsNegative() throws Exception {
        fillValidCreateForm();

        CheckBox checkLimitWaitlist = getCheckBox("checkLimitWaitlist");
        EditText limitWaitlist = getEditText("limitWaitlist");

        checkLimitWaitlist.setChecked(true);
        limitWaitlist.setText("-1");

        boolean result = invokeIsFormReadyForSubmit();

        assertFalse(result);
    }

    @Test
    public void isFormReadyForSubmit_returnsTrue_whenWaitlistCheckedWithValidNumber() throws Exception {
        fillValidCreateForm();

        CheckBox checkLimitWaitlist = getCheckBox("checkLimitWaitlist");
        EditText limitWaitlist = getEditText("limitWaitlist");

        checkLimitWaitlist.setChecked(true);
        limitWaitlist.setText("25");

        boolean result = invokeIsFormReadyForSubmit();

        assertTrue(result);
    }

    @Test
    public void updatePrivateEventUiState_setsCreatePrivateEventText_inCreateMode() throws Exception {
        setBooleanField("isEditMode", false);
        getCheckBox("checkPrivateEvent").setChecked(true);

        invokeUpdatePrivateEventUiState();

        assertEquals("Create Private Event", getButton("buttonCreateEvent").getText().toString());
    }

    @Test
    public void updatePrivateEventUiState_setsCreateEventText_inCreateMode() throws Exception {
        setBooleanField("isEditMode", false);
        getCheckBox("checkPrivateEvent").setChecked(false);

        invokeUpdatePrivateEventUiState();

        assertEquals("Create Event", getButton("buttonCreateEvent").getText().toString());
    }

    @Test
    public void updatePrivateEventUiState_setsUpdatePrivateEventText_inEditMode() throws Exception {
        setBooleanField("isEditMode", true);
        getCheckBox("checkPrivateEvent").setChecked(true);

        invokeUpdatePrivateEventUiState();

        assertEquals("Update Private Event", getButton("buttonCreateEvent").getText().toString());
    }

    @Test
    public void updatePrivateEventUiState_setsUpdateEventText_inEditMode() throws Exception {
        setBooleanField("isEditMode", true);
        getCheckBox("checkPrivateEvent").setChecked(false);

        invokeUpdatePrivateEventUiState();

        assertEquals("Update Event", getButton("buttonCreateEvent").getText().toString());
    }

    @Test
    public void hasFormChangedFromOriginal_returnsFalse_whenEditFormMatchesOriginal() throws Exception {
        Event originalEvent = buildSampleEvent();

        setBooleanField("isEditMode", true);
        setObjectField("editingEvent", originalEvent);

        populateFragmentFieldsFromEvent(originalEvent);
        setField("selectedImageBytes", null);

        boolean result = invokeHasFormChangedFromOriginal();

        assertFalse(result);
    }

    @Test
    public void hasFormChangedFromOriginal_returnsTrue_whenNameChanges() throws Exception {
        Event originalEvent = buildSampleEvent();

        setBooleanField("isEditMode", true);
        setObjectField("editingEvent", originalEvent);

        populateFragmentFieldsFromEvent(originalEvent);
        getEditText("inputEventName").setText("New Event Name");

        boolean result = invokeHasFormChangedFromOriginal();

        assertTrue(result);
    }

    @Test
    public void hasFormChangedFromOriginal_returnsTrue_whenGeoRequirementChanges() throws Exception {
        Event originalEvent = buildSampleEvent();

        setBooleanField("isEditMode", true);
        setObjectField("editingEvent", originalEvent);

        populateFragmentFieldsFromEvent(originalEvent);
        getCheckBox("checkGeoRequired").setChecked(!originalEvent.isGeolocationRequired());

        boolean result = invokeHasFormChangedFromOriginal();

        assertTrue(result);
    }

    @Test
    public void hasFormChangedFromOriginal_returnsTrue_whenSelectedImageExists() throws Exception {
        Event originalEvent = buildSampleEvent();

        setBooleanField("isEditMode", true);
        setObjectField("editingEvent", originalEvent);

        populateFragmentFieldsFromEvent(originalEvent);
        setField("selectedImageBytes", new byte[]{1, 2, 3});

        boolean result = invokeHasFormChangedFromOriginal();

        assertTrue(result);
    }

    private void fillValidCreateForm() throws Exception {
        getEditText("inputEventName").setText("Spring Concert");
        getEditText("inputEventDescription").setText("A great concert.");
        getEditText("inputEventLocation").setText("School Gym");
        getEditText("inputAttendanceLimit").setText("100");
        getEditText("limitWaitlist").setText("");

        getCheckBox("checkLimitWaitlist").setChecked(false);
        getCheckBox("checkGeoRequired").setChecked(false);
        getCheckBox("checkPrivateEvent").setChecked(false);

        setLongField("eventStartEpochSec", 1000L);
        setLongField("eventEndEpochSec", 2000L);
        setLongField("registrationCloseEpochSec", 900L);
    }

    private Event buildSampleEvent() {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("General", "Students only"));

        Event event = new Event(
                "organizer-1",
                "Original Event",
                "Original Description",
                0.0,
                "Original Location",
                tags,
                1000L,
                2000L,
                500L,
                900L,
                true,
                25,
                100,
                null,
                null
        );
        event.setPrivateEvent(true);
        return event;
    }

    private void populateFragmentFieldsFromEvent(Event event) throws Exception {
        getEditText("inputEventName").setText(event.getName());
        getEditText("inputEventDescription").setText(event.getDescription());
        getEditText("inputCriteria").setText("Students only");
        getEditText("inputEventLocation").setText(event.getLocationName());
        getEditText("inputAttendanceLimit").setText(String.valueOf(event.getMaxWinnersToSample()));
        getEditText("limitWaitlist").setText(String.valueOf(event.getMaxWaitingListSize()));

        getCheckBox("checkLimitWaitlist").setChecked(true);
        getCheckBox("checkGeoRequired").setChecked(event.isGeolocationRequired());
        getCheckBox("checkPrivateEvent").setChecked(event.isPrivateEvent());

        setLongField("eventStartEpochSec", event.getEventStartEpochSec());
        setLongField("eventEndEpochSec", event.getEventEndEpochSec());
        setLongField("registrationCloseEpochSec", event.getRegistrationCloseEpochSec());
    }

    private boolean invokeIsFormReadyForSubmit() throws Exception {
        Method method = CreateEventFragment.class.getDeclaredMethod("isFormReadyForSubmit");
        method.setAccessible(true);
        return (boolean) method.invoke(fragment);
    }

    private boolean invokeHasFormChangedFromOriginal() throws Exception {
        Method method = CreateEventFragment.class.getDeclaredMethod("hasFormChangedFromOriginal");
        method.setAccessible(true);
        return (boolean) method.invoke(fragment);
    }

    private void invokeUpdatePrivateEventUiState() throws Exception {
        Method method = CreateEventFragment.class.getDeclaredMethod("updatePrivateEventUiState");
        method.setAccessible(true);
        method.invoke(fragment);
    }

    private Object getField(String fieldName) throws Exception {
        Field field = CreateEventFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(fragment);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = CreateEventFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
    }

    private void setObjectField(String fieldName, Object value) throws Exception {
        setField(fieldName, value);
    }

    private void setBooleanField(String fieldName, boolean value) throws Exception {
        Field field = CreateEventFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setBoolean(fragment, value);
    }

    private void setLongField(String fieldName, long value) throws Exception {
        Field field = CreateEventFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setLong(fragment, value);
    }

    private EditText getEditText(String fieldName) throws Exception {
        return (EditText) getField(fieldName);
    }

    private CheckBox getCheckBox(String fieldName) throws Exception {
        return (CheckBox) getField(fieldName);
    }

    private Button getButton(String fieldName) throws Exception {
        return (Button) getField(fieldName);
    }
}