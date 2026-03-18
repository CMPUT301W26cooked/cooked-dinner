package com.eventwise.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.Entrant;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;

/**
 * Reusable entrant profile form fragment.
 * Supports create mode and update mode.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-12
 */
public class EntrantProfileExistsFormFragment extends Fragment {

    /**
     * TODO
     * - Revisit validation rules later if the team changes profile requirements.
     * - Add tests for create mode, update mode, and clear profile flows.
     */

    private static final long VALIDATION_DELAY_MS = 800;

    private static final String ARG_MODE = "mode";
    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NOTIFICATIONS = "notifications";

    private static final String MODE_CREATE = "create";
    private static final String MODE_UPDATE = "update";

    private Button submitProfileButton;
    private Button cancelProfileButton;
    private Button deleteProfileButton;

    private View deleteProfileButtonContainer;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;

    private ImageView nameStatusImage;
    private ImageView emailStatusImage;
    private ImageView phoneStatusImage;

    private TextView nameErrorText;
    private TextView emailErrorText;
    private TextView phoneErrorText;

    private TextView instructionText;

    private View receiveNotificationsLayout;
    private ImageView receiveNotificationsCheckboxImage;

    private boolean receiveNotificationsEnabled = true;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingNameValidation;
    private Runnable pendingEmailValidation;
    private Runnable pendingPhoneValidation;

    private boolean nameTouched = false;
    private boolean emailTouched = false;
    private boolean phoneTouched = false;

    private String mode = MODE_CREATE;

    private String originalName = "";
    private String originalEmail = "";
    private String originalPhone = "";
    private boolean originalNotificationsEnabled = true;

    public EntrantProfileExistsFormFragment() {
    }

    /**
     * Makes a new profile form fragment in create mode.
     *
     * @return create mode fragment
     */
    public static EntrantProfileExistsFormFragment newCreateInstance() {
        return newCreateInstance(true);
    }

    /**
     * Makes a new profile form fragment in create mode.
     *
     * @param receiveNotificationsEnabled starting notifications setting
     * @return create mode fragment
     */
    public static EntrantProfileExistsFormFragment newCreateInstance(boolean receiveNotificationsEnabled) {
        EntrantProfileExistsFormFragment fragment = new EntrantProfileExistsFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_CREATE);
        args.putString(ARG_NAME, "");
        args.putString(ARG_EMAIL, "");
        args.putString(ARG_PHONE, "");
        args.putBoolean(ARG_NOTIFICATIONS, receiveNotificationsEnabled);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Makes a new profile form fragment in update mode.
     *
     * @param name current name
     * @param email current email
     * @param phone current phone
     * @param receiveNotificationsEnabled current notifications setting
     * @return update mode fragment
     */
    public static EntrantProfileExistsFormFragment newUpdateInstance(
            String name,
            String email,
            String phone,
            boolean receiveNotificationsEnabled
    ) {
        EntrantProfileExistsFormFragment fragment = new EntrantProfileExistsFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_UPDATE);
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PHONE, phone);
        args.putBoolean(ARG_NOTIFICATIONS, receiveNotificationsEnabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_profile_exists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        submitProfileButton = view.findViewById(R.id.btn_submit_profile);
        cancelProfileButton = view.findViewById(R.id.btn_cancel_profile);
        deleteProfileButton = view.findViewById(R.id.btn_delete_profile);
        deleteProfileButtonContainer = view.findViewById(R.id.delete_profile_button_container);

        nameEditText = view.findViewById(R.id.edit_text_profile_name);
        emailEditText = view.findViewById(R.id.edit_text_profile_email);
        phoneEditText = view.findViewById(R.id.edit_text_profile_phone);

        nameStatusImage = view.findViewById(R.id.image_name_validation_status);
        emailStatusImage = view.findViewById(R.id.image_email_validation_status);
        phoneStatusImage = view.findViewById(R.id.image_phone_validation_status);

        nameErrorText = view.findViewById(R.id.text_name_error);
        emailErrorText = view.findViewById(R.id.text_email_error);
        phoneErrorText = view.findViewById(R.id.text_phone_error);

        instructionText = view.findViewById(R.id.profile_form_instruction_text);

        receiveNotificationsLayout = view.findViewById(R.id.layout_receive_notifications);
        receiveNotificationsCheckboxImage = view.findViewById(R.id.image_receive_notifications_checkbox);

        loadArguments();
        populateInitialValues();
        configureModeUi();

        setupReceiveNotificationsToggle();
        setupFieldValidation();
        setupButtons();

        clearAllValidationVisuals();
        updateActionButtonsState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (pendingNameValidation != null) {
            handler.removeCallbacks(pendingNameValidation);
        }
        if (pendingEmailValidation != null) {
            handler.removeCallbacks(pendingEmailValidation);
        }
        if (pendingPhoneValidation != null) {
            handler.removeCallbacks(pendingPhoneValidation);
        }
    }

    /**
     * Reads mode and starting values from fragment arguments.
     */
    private void loadArguments() {
        Bundle args = getArguments();
        if (args == null) {
            mode = MODE_CREATE;
            originalName = "";
            originalEmail = "";
            originalPhone = "";
            originalNotificationsEnabled = true;
            return;
        }

        mode = args.getString(ARG_MODE, MODE_CREATE);
        originalName = args.getString(ARG_NAME, "");
        originalEmail = args.getString(ARG_EMAIL, "");
        originalPhone = args.getString(ARG_PHONE, "");
        originalNotificationsEnabled = args.getBoolean(ARG_NOTIFICATIONS, true);
    }

    /**
     * Loads the starting field values into the form.
     */
    private void populateInitialValues() {
        nameEditText.setText(originalName);
        emailEditText.setText(originalEmail);
        phoneEditText.setText(originalPhone);

        receiveNotificationsEnabled = originalNotificationsEnabled;
        updateNotificationsCheckboxIcon();

        nameTouched = false;
        emailTouched = false;
        phoneTouched = false;
    }

    /**
     * Adjusts button labels and layout for create vs update mode.
     */
    private void configureModeUi() {
        if (isCreateMode()) {
            instructionText.setText("Fill in your details below.\nValidate at each step.");
            submitProfileButton.setText("Create Profile");
            cancelProfileButton.setText("Cancel");
            deleteProfileButtonContainer.setVisibility(View.GONE);
        } else {
            instructionText.setText("Update your details below.\nValidate at each step.");
            submitProfileButton.setText("Update Profile");
            cancelProfileButton.setText("Cancel Changes");
            deleteProfileButtonContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets up the receive notifications toggle row.
     */
    private void setupReceiveNotificationsToggle() {
        receiveNotificationsLayout.setOnClickListener(v -> {
            receiveNotificationsEnabled = !receiveNotificationsEnabled;
            updateNotificationsCheckboxIcon();
            updateActionButtonsState();
        });
    }

    /**
     * Updates the checkbox icon for notifications.
     */
    private void updateNotificationsCheckboxIcon() {
        if (receiveNotificationsEnabled) {
            receiveNotificationsCheckboxImage.setImageResource(R.drawable.profile_checkbox_checked);
        } else {
            receiveNotificationsCheckboxImage.setImageResource(R.drawable.profile_checkbox_unchecked);
        }
    }

    /**
     * Hooks field validation to typing and focus changes.
     */
    private void setupFieldValidation() {
        nameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                nameTouched = true;
                clearFieldState(nameStatusImage, nameErrorText);
                scheduleNameValidation();
                updateActionButtonsState();
            }
        });

        emailEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                emailTouched = true;
                clearFieldState(emailStatusImage, emailErrorText);
                scheduleEmailValidation();
                updateActionButtonsState();
            }
        });

        phoneEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                phoneTouched = true;
                clearFieldState(phoneStatusImage, phoneErrorText);
                schedulePhoneValidation();
                updateActionButtonsState();
            }
        });

        nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                nameTouched = true;
                validateNameAndDisplay();
            }
        });

        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                emailTouched = true;
                validateEmailAndDisplay();
            }
        });

        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                phoneTouched = true;
                validatePhoneAndDisplay();
            }
        });
    }

    /**
     * Hooks up submit, cancel, and delete buttons.
     */
    private void setupButtons() {
        submitProfileButton.setOnClickListener(v -> {
            nameTouched = true;
            emailTouched = true;
            phoneTouched = true;

            boolean nameValid = validateNameAndDisplay();
            boolean emailValid = validateEmailAndDisplay();
            boolean phoneValid = validatePhoneAndDisplay();

            updateActionButtonsState();

            if (!(nameValid && emailValid && phoneValid)) {
                return;
            }

            persistProfileToBackend();
        });

        cancelProfileButton.setOnClickListener(v -> {
            if (isCreateMode()) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.entrant_fragment_container,
                                EntrantProfileEmptyFragment.newInstance(receiveNotificationsEnabled)
                        )
                        .commit();
            } else {
                restoreOriginalValues();
            }
        });

        deleteProfileButton.setOnClickListener(v -> {
            SessionStore sessionStore = new SessionStore(requireContext());
            String entrantId = sessionStore.getOrCreateDeviceId();

            EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();
            entrantDatabaseManager.clearEntrantProfile(entrantId)
                    .addOnSuccessListener(unused -> {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.entrant_fragment_container,
                                        EntrantProfileEmptyFragment.newInstance(receiveNotificationsEnabled)
                                )
                                .commit();
                    })
                    .addOnFailureListener(e ->
                            Log.e("EntrantProfileForm", "Failed to clear entrant profile", e));
        });
    }

    /**
     * Saves the current profile form to Firestore.
     */
    private void persistProfileToBackend() {
        SessionStore sessionStore = new SessionStore(requireContext());
        String entrantId = sessionStore.getOrCreateDeviceId();

        EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();

        entrantDatabaseManager.getEntrantFromId(entrantId)
                .addOnSuccessListener(existingEntrant -> {
                    if (existingEntrant == null) {
                        createNewEntrantProfile(entrantDatabaseManager);
                        return;
                    }


                    existingEntrant.setName(getTrimmedText(nameEditText));
                    existingEntrant.setEmail(getTrimmedText(emailEditText));
                    existingEntrant.setPhone(getTrimmedText(phoneEditText));
                    existingEntrant.setNotificationsEnabled(receiveNotificationsEnabled);

                    entrantDatabaseManager.updateEntrantInfo(existingEntrant)
                            .addOnSuccessListener(unused -> handleSuccessfulProfileSave())
                            .addOnFailureListener(e ->
                                    Log.e("EntrantProfileForm", "Failed to update entrant profile", e));
                })
                .addOnFailureListener(e -> createNewEntrantProfile(entrantDatabaseManager));
    }

    /**
     * Creates a new entrant profile in Firestore.
     */
    private void createNewEntrantProfile(EntrantDatabaseManager entrantDatabaseManager) {
        Entrant entrant = new Entrant(
                getTrimmedText(nameEditText),
                getTrimmedText(emailEditText),
                getTrimmedText(phoneEditText),
                receiveNotificationsEnabled,
                requireContext()
        );

        entrantDatabaseManager.addEntrant(entrant)
                .addOnSuccessListener(unused -> handleSuccessfulProfileSave())
                .addOnFailureListener(err ->
                        Log.e("EntrantProfileForm", "Failed to create entrant profile", err));
    }

    /**
     * Handles UI state after a successful save.
     */
    private void handleSuccessfulProfileSave() {
        if (isCreateMode()) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.entrant_fragment_container,
                            EntrantProfileExistsFormFragment.newUpdateInstance(
                                    getTrimmedText(nameEditText),
                                    getTrimmedText(emailEditText),
                                    getTrimmedText(phoneEditText),
                                    receiveNotificationsEnabled
                            )
                    )
                    .commit();
            return;
        }

        originalName = getTrimmedText(nameEditText);
        originalEmail = getTrimmedText(emailEditText);
        originalPhone = getTrimmedText(phoneEditText);
        originalNotificationsEnabled = receiveNotificationsEnabled;

        nameTouched = false;
        emailTouched = false;
        phoneTouched = false;

        clearAllValidationVisuals();
        updateActionButtonsState();
    }

    /**
     * Restores the original field values in update mode.
     */
    private void restoreOriginalValues() {
        nameEditText.setText(originalName);
        emailEditText.setText(originalEmail);
        phoneEditText.setText(originalPhone);

        receiveNotificationsEnabled = originalNotificationsEnabled;
        updateNotificationsCheckboxIcon();

        nameTouched = false;
        emailTouched = false;
        phoneTouched = false;

        clearAllValidationVisuals();
        updateActionButtonsState();
    }

    /**
     * Starts delayed name validation.
     */
    private void scheduleNameValidation() {
        if (pendingNameValidation != null) {
            handler.removeCallbacks(pendingNameValidation);
        }
        pendingNameValidation = this::validateNameAndDisplay;
        handler.postDelayed(pendingNameValidation, VALIDATION_DELAY_MS);
    }

    /**
     * Starts delayed email validation.
     */
    private void scheduleEmailValidation() {
        if (pendingEmailValidation != null) {
            handler.removeCallbacks(pendingEmailValidation);
        }
        pendingEmailValidation = this::validateEmailAndDisplay;
        handler.postDelayed(pendingEmailValidation, VALIDATION_DELAY_MS);
    }

    /**
     * Starts delayed phone validation.
     */
    private void schedulePhoneValidation() {
        if (pendingPhoneValidation != null) {
            handler.removeCallbacks(pendingPhoneValidation);
        }
        pendingPhoneValidation = this::validatePhoneAndDisplay;
        handler.postDelayed(pendingPhoneValidation, VALIDATION_DELAY_MS);
    }

    /**
     * Validates the name field and updates the UI.
     *
     * @return true if valid
     */
    private boolean validateNameAndDisplay() {
        String name = getTrimmedText(nameEditText);

        if (!nameTouched) {
            return false;
        }

        if (name.isEmpty()) {
            showInvalidState(nameStatusImage, nameErrorText, "Name is required.");
            updateActionButtonsState();
            return false;
        }

        if (name.length() < 2) {
            showInvalidState(nameStatusImage, nameErrorText, "Name must be at least 2 characters.");
            updateActionButtonsState();
            return false;
        }

        if (!name.matches("[A-Za-z .'-]+")) {
            showInvalidState(nameStatusImage, nameErrorText, "Name contains invalid characters.");
            updateActionButtonsState();
            return false;
        }

        showValidState(nameStatusImage, nameErrorText);
        updateActionButtonsState();
        return true;
    }

    /**
     * Validates the email field and updates the UI.
     *
     * @return true if valid
     */
    private boolean validateEmailAndDisplay() {
        String email = getTrimmedText(emailEditText);

        if (!emailTouched) {
            return false;
        }

        if (email.isEmpty()) {
            showInvalidState(emailStatusImage, emailErrorText, "Email is required.");
            updateActionButtonsState();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showInvalidState(emailStatusImage, emailErrorText, "Email format must look like abc@ex.com.");
            updateActionButtonsState();
            return false;
        }

        showValidState(emailStatusImage, emailErrorText);
        updateActionButtonsState();
        return true;
    }

    /**
     * Validates the phone field and updates the UI.
     *
     * @return true if valid
     */
    private boolean validatePhoneAndDisplay() {
        String phone = getTrimmedText(phoneEditText);

        if (!phoneTouched && phone.isEmpty()) {
            return true;
        }

        if (phone.isEmpty()) {
            clearFieldState(phoneStatusImage, phoneErrorText);
            updateActionButtonsState();
            return true;
        }

        if (!phone.matches("^[0-9()\\- +]{10,18}$")) {
            showInvalidState(
                    phoneStatusImage,
                    phoneErrorText,
                    "Phone must use a valid length and allowed symbols."
            );
            updateActionButtonsState();
            return false;
        }

        showValidState(phoneStatusImage, phoneErrorText);
        updateActionButtonsState();
        return true;
    }

    /**
     * Shows a valid check icon and clears any error text.
     *
     * @param statusImage validation icon
     * @param errorText error message text
     */
    private void showValidState(ImageView statusImage, TextView errorText) {
        statusImage.setVisibility(View.VISIBLE);
        statusImage.setImageResource(R.drawable.profile_valid_check);

        errorText.setText("");
        errorText.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows an invalid icon and an error message.
     *
     * @param statusImage validation icon
     * @param errorText error message text
     * @param message error message
     */
    private void showInvalidState(ImageView statusImage, TextView errorText, String message) {
        statusImage.setVisibility(View.VISIBLE);
        statusImage.setImageResource(R.drawable.profile_invalid_x);

        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    /**
     * Clears one field's validation UI.
     *
     * @param statusImage validation icon
     * @param errorText error message text
     */
    private void clearFieldState(ImageView statusImage, TextView errorText) {
        statusImage.setVisibility(View.INVISIBLE);
        errorText.setText("");
        errorText.setVisibility(View.INVISIBLE);
    }

    /**
     * Clears all validation icons and messages.
     */
    private void clearAllValidationVisuals() {
        clearFieldState(nameStatusImage, nameErrorText);
        clearFieldState(emailStatusImage, emailErrorText);
        clearFieldState(phoneStatusImage, phoneErrorText);
    }

    /**
     * Updates the enabled or disabled state of action buttons.
     */
    private void updateActionButtonsState() {
        boolean allValid = isNameCurrentlyValid() && isEmailCurrentlyValid() && isPhoneCurrentlyValid();
        boolean hasChanges = hasChangesFromOriginal();

        if (isCreateMode()) {
            if (allValid) {
                setButtonEnabled(submitProfileButton);
            } else {
                setButtonDisabled(submitProfileButton);
            }

            setButtonEnabled(cancelProfileButton);
            return;
        }

        if (allValid && hasChanges) {
            setButtonEnabled(submitProfileButton);
        } else {
            setButtonDisabled(submitProfileButton);
        }

        if (hasChanges) {
            setButtonEnabled(cancelProfileButton);
        } else {
            setButtonDisabled(cancelProfileButton);
        }

        setButtonEnabled(deleteProfileButton);
    }

    /**
     * Makes a button enabled and fully visible.
     *
     * @param button button to enable
     */
    private void setButtonEnabled(Button button) {
        button.setEnabled(true);
        button.setTextColor(getResources().getColor(R.color.lighter_green, null));
    }

    /**
     * Makes a button disabled and faded.
     *
     * @param button button to disable
     */
    private void setButtonDisabled(Button button) {
        button.setEnabled(false);
        button.setTextColor(0x80F3F6E5);
    }

    /**
     * Checks whether the current values differ from the original values.
     *
     * @return true if changed
     */
    private boolean hasChangesFromOriginal() {
        return !getTrimmedText(nameEditText).equals(originalName)
                || !getTrimmedText(emailEditText).equals(originalEmail)
                || !getTrimmedText(phoneEditText).equals(originalPhone)
                || receiveNotificationsEnabled != originalNotificationsEnabled;
    }

    /**
     * Checks whether the current name is valid.
     *
     * @return true if valid
     */
    private boolean isNameCurrentlyValid() {
        String name = getTrimmedText(nameEditText);
        return !name.isEmpty()
                && name.length() >= 2
                && name.matches("[A-Za-z .'-]+");
    }

    /**
     * Checks whether the current email is valid.
     *
     * @return true if valid
     */
    private boolean isEmailCurrentlyValid() {
        String email = getTrimmedText(emailEditText);
        return !email.isEmpty()
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Checks whether the current phone is valid.
     *
     * @return true if valid
     */
    private boolean isPhoneCurrentlyValid() {
        String phone = getTrimmedText(phoneEditText);
        return phone.isEmpty() || phone.matches("^[0-9()\\- +]{10,18}$");
    }

    /**
     * @return true if this fragment is in create mode
     */
    private boolean isCreateMode() {
        return MODE_CREATE.equals(mode);
    }

    /**
     * Gets trimmed text from an EditText.
     *
     * @param editText field to read
     * @return trimmed string
     */
    private String getTrimmedText(EditText editText) {
        return editText.getText().toString().trim();
    }

    /**
     * Small helper watcher so we only override what we need.
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
