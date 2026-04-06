package com.eventwise.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.eventwise.Event;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Task;

/**
 * Admin page for entrant event actions.
 *
 * This is the admin version of entrant actions and does not include
 * Advanced Developer Actions.
 *
 * @author Becca Irving
 * @since March 26 2026
 */
public class AdminEntrantActionsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "arg_event_id";

    private String eventId = "";

    private Button viewEntrantsButton;
    private Button drawEntrantsButton;
    private Button redrawEntrantsButton;
    private Button cancelInviteesButton;

    private TextView drawEntrantsReasonText;
    private TextView redrawEntrantsReasonText;
    private TextView cancelInviteesReasonText;

    public AdminEntrantActionsFragment() {
    }

    /**
     * Makes a new admin entrant actions fragment for one event.
     *
     * @param eventId event id
     * @return configured fragment
     */
    public static AdminEntrantActionsFragment newInstance(@NonNull String eventId) {
        AdminEntrantActionsFragment fragment = new AdminEntrantActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID, "");
        }

        View backButton = view.findViewById(R.id.button_back);
        viewEntrantsButton = view.findViewById(R.id.button_view_entrants);
        drawEntrantsButton = view.findViewById(R.id.button_draw_entrants);
        redrawEntrantsButton = view.findViewById(R.id.button_redraw_entrants);
        cancelInviteesButton = view.findViewById(R.id.button_cancel_invitees);
        Button advancedDeveloperActionsButton = view.findViewById(R.id.button_advanced_developer_actions);

        drawEntrantsReasonText = view.findViewById(R.id.text_draw_entrants_reason);
        redrawEntrantsReasonText = view.findViewById(R.id.text_redraw_entrants_reason);
        cancelInviteesReasonText = view.findViewById(R.id.text_cancel_invitees_reason);
        advancedDeveloperActionsButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        viewEntrantsButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, ViewEntrantsFragment.newInstance(eventId))
                        .addToBackStack(null)
                        .commit()
        );

        drawEntrantsButton.setOnClickListener(v ->
                runAction("Draw Entrants", "Draw entrants for this event?",
                        new ActionRunner() {
                            @Override
                            public Task<Void> run() {
                                OrganizerDatabaseManager organizerDatabaseManager =
                                        new OrganizerDatabaseManager();
                                return organizerDatabaseManager.drawEntrants(eventId);
                            }
                        }, "Draw Complete", "Entrants were drawn successfully."
                )
        );

        redrawEntrantsButton.setOnClickListener(v ->
                runAction("Re-draw Entrants", "Re-draw entrants to fill open spots?",
                        new ActionRunner() {
                            @Override
                            public Task<Void> run() {
                                OrganizerDatabaseManager organizerDatabaseManager =
                                        new OrganizerDatabaseManager();
                                return organizerDatabaseManager.redrawEntrants(eventId);
                            }
                        }, "Re-draw Complete", "Open spots were filled from the waitlist."
                )
        );

        cancelInviteesButton.setOnClickListener(v ->
                runAction("Cancel Invitees", "Cancel all currently invited entrants for this event?",
                        new ActionRunner() {
                            @Override
                            public Task<Void> run() {
                                OrganizerDatabaseManager organizerDatabaseManager =
                                        new OrganizerDatabaseManager();
                                return organizerDatabaseManager.cancelInvitees(eventId);
                            }
                        }, "Invitees Cancelled", "All currently invited entrants were cancelled."
                )
        );

        refreshActionAvailability();

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshActionAvailability();
    }

    /**
     * Runs one admin action with confirmation and a result popup.
     *
     * @param confirmTitle confirmation title
     * @param confirmMessage confirmation message
     * @param actionRunner action to run
     * @param successTitle success popup title
     * @param successMessage success popup message
     */
    private void runAction(String confirmTitle, String confirmMessage, ActionRunner actionRunner, String successTitle, String successMessage) {

        if (eventId == null || eventId.trim().isEmpty()) {
            showStyledMessageDialog("Error", "Missing event Id.");
            return;
        }

        showStyledDialog(confirmTitle, confirmMessage, "Yes", "No",
                new Runnable() {
                    @Override
                    public void run() {
                        setButtonsEnabled(false);

                        actionRunner.run()
                                .addOnSuccessListener(unused -> {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    setButtonsEnabled(true);
                                    refreshActionAvailability();
                                    showStyledMessageDialog(successTitle, successMessage);
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    setButtonsEnabled(true);
                                    refreshActionAvailability();

                                    String message = "Something went wrong.";
                                    if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
                                        message = e.getMessage();
                                    }

                                    showStyledMessageDialog("Action Failed", message);
                                });
                    }
                }
        );
    }

    /**
     * Enables or disables the action buttons while backend work runs.
     *
     * @param enabled true to enable buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        viewEntrantsButton.setEnabled(enabled);
        drawEntrantsButton.setEnabled(enabled);
        redrawEntrantsButton.setEnabled(enabled);
        cancelInviteesButton.setEnabled(enabled);
        viewEntrantsButton.setAlpha(enabled ? 1.0f : 0.5f);
        drawEntrantsButton.setAlpha(enabled ? 1.0f : 0.5f);
        redrawEntrantsButton.setAlpha(enabled ? 1.0f : 0.5f);
        cancelInviteesButton.setAlpha(enabled ? 1.0f : 0.5f);
    }


    /**
     * Refreshes which entrant action buttons are currently allowed for this event.
     */
    private void refreshActionAvailability() {
        if (eventId == null || eventId.trim().isEmpty()) {
            return;
        }

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        organizerDatabaseManager.getEventById(eventId)
                .addOnSuccessListener(event -> {
                    if (!isAdded()) {
                        return;
                    }

                    updateActionAvailability(event);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }

                    drawEntrantsReasonText.setVisibility(View.GONE);
                    redrawEntrantsReasonText.setVisibility(View.GONE);
                    cancelInviteesReasonText.setVisibility(View.GONE);
                });
    }

    /**
     * Applies the entrant action button rules for the current event state.
     *
     * @param event current event
     */
    private void updateActionAvailability(@NonNull Event event) {
        int waitlistedCount = event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).size();
        int invitedCount = event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).size();
        int enrolledCount = event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).size();
        int attendanceLimit = event.getMaxWinnersToSample();
        int filledSpots = invitedCount + enrolledCount;
        int openSpots = attendanceLimit - filledSpots;

        viewEntrantsButton.setEnabled(true);
        viewEntrantsButton.setAlpha(1.0f);

        boolean drawEnabled;
        String drawReason = null;
        boolean redrawEnabled;
        String redrawReason = null;

        if (event.isPrivateEvent()) {
            drawEnabled = false;
            redrawEnabled = false;
            drawReason = "* private events do not use waitlist drawing";
            redrawReason = "* private events do not use waitlist drawing";
        } else {
            drawEnabled = waitlistedCount > 0 && invitedCount == 0 && enrolledCount == 0;

            if (!drawEnabled) {
                if (invitedCount > 0 || enrolledCount > 0) {
                    drawReason = "* cannot draw with invited / registered entrants";
                } else if (waitlistedCount == 0) {
                    drawReason = "* no entrants in waitlist";
                }
            }

            redrawEnabled = waitlistedCount > 0 && filledSpots > 0 && openSpots > 0;

            if (!redrawEnabled) {
                if (filledSpots == 0) {
                    redrawReason = "* you must draw first before re-draw";
                } else if (openSpots <= 0) {
                    redrawReason = "* no open spots in this event";
                } else if (waitlistedCount == 0) {
                    redrawReason = "* no entrants in waitlist";
                }
            }
        }

        boolean cancelInviteesEnabled = invitedCount > 0;
        String cancelInviteesReason = cancelInviteesEnabled ? null : "* no invited entrants at this time";


        applyManagedButtonState(drawEntrantsButton, drawEntrantsReasonText, drawEnabled, drawReason);
        applyManagedButtonState(redrawEntrantsButton, redrawEntrantsReasonText, redrawEnabled, redrawReason);
        applyManagedButtonState(cancelInviteesButton, cancelInviteesReasonText, cancelInviteesEnabled, cancelInviteesReason);
    }

    /**
     * Applies one button enabled state and optional reason text.
     *
     * @param button target button
     * @param reasonText target reason text
     * @param enabled true if the button should be clickable
     * @param reason reason to show when disabled
     */
    private void applyManagedButtonState(@NonNull Button button, @NonNull TextView reasonText, boolean enabled, @Nullable String reason) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);

        if (enabled || reason == null || reason.trim().isEmpty()) {
            reasonText.setVisibility(View.GONE);
        } else {
            reasonText.setText(reason);
            reasonText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows popup with acknowledgement button.
     *
     * @param title popup title
     * @param message popup message
     */
    private void showStyledMessageDialog(String title, String message) {
        showStyledDialog(title, message, "OK", null, null);
    }

    /**
     * Shows popup dialog widget.
     *
     * @param title dialog title
     * @param message dialog message
     * @param positiveText positive button text
     * @param negativeText negative button text, or null to hide
     * @param onPositive action to run when the positive button is pressed
     */
    private void showStyledDialog(String title, String message, String positiveText, @Nullable String negativeText, @Nullable Runnable onPositive) {

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.widget_custom_dialog, null, false);

        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);
        Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);

        titleText.setText(title);
        messageText.setText(message);
        positiveButton.setText(positiveText);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        if (negativeText == null || negativeText.trim().isEmpty()) {
            negativeButton.setVisibility(View.GONE);
        } else {
            negativeButton.setText(negativeText);
            negativeButton.setOnClickListener(v -> dialog.dismiss());
        }

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

    /**
     * Small helper for backend.
     */
    private interface ActionRunner {
        Task<Void> run();
    }
}
