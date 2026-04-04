package com.eventwise.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.adapters.EntrantSelectionAdapter;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Lets organizer select many entrant ids for one developer action.
 *
 * @author Becca Irving
 * @since Mar 29 2026
 */
public class EntrantSelectionFragment extends Fragment {

    /**
     * TODO (EntrantSelectionFragment.java)
     * - Add tests later.
     */

    public static final String ACTION_WAITLIST = "action_waitlist";
    public static final String ACTION_REMOVE_WAITLIST = "action_remove_waitlist";
    public static final String ACTION_INVITE = "action_invite";
    public static final String ACTION_REMOVE_INVITE = "action_remove_invite";
    public static final String ACTION_ENROLL = "action_enroll";
    public static final String ACTION_REMOVE_ENROLL = "action_remove_enroll";
    public static final String ACTION_SYNC_PRIVATE_SELECTION = "action_sync_private_selection";

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_ACTION = "arg_action";
    private static final String ARG_RETURN_TO_ORGANIZER_EVENTS = "arg_return_to_organizer_events";

    private String eventId = "";
    private String action = "";
    private boolean returnToOrganizerEvents = false;

    private TextView titleText;
    private TextView subtitleText;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Button actionButton;

    private EntrantSelectionAdapter adapter;
    private final ArrayList<String> eligibleEntrantIds = new ArrayList<>();

    public EntrantSelectionFragment() {
    }

    /**
     * Makes a new entrant selection fragment.
     *
     * @param eventId event id
     * @param action action type
     * @return configured fragment
     */
    public static EntrantSelectionFragment newInstance(@NonNull String eventId, @NonNull String action) {
        EntrantSelectionFragment fragment = new EntrantSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ACTION, action);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Makes a private invite selection fragment that returns to organizer events after apply.
     *
     * @param eventId event id
     * @return configured fragment
     */
    public static EntrantSelectionFragment newPrivateInviteInstance(@NonNull String eventId) {
        EntrantSelectionFragment fragment = new EntrantSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ACTION, ACTION_INVITE);
        args.putBoolean(ARG_RETURN_TO_ORGANIZER_EVENTS, true);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Makes a private edit selection fragment that shows all entrants and syncs selection.
     *
     * @param eventId event id
     * @return configured fragment
     */
    public static EntrantSelectionFragment newPrivateEditInstance(@NonNull String eventId) {
        EntrantSelectionFragment fragment = new EntrantSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ACTION, ACTION_SYNC_PRIVATE_SELECTION);
        args.putBoolean(ARG_RETURN_TO_ORGANIZER_EVENTS, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID, "");
            action = args.getString(ARG_ACTION, "");
            returnToOrganizerEvents = args.getBoolean(ARG_RETURN_TO_ORGANIZER_EVENTS, false);
        }

        View backButton = view.findViewById(R.id.button_back);
        titleText = view.findViewById(R.id.text_selection_title);
        subtitleText = view.findViewById(R.id.text_selection_subtitle);
        recyclerView = view.findViewById(R.id.recycler_view_entrant_selection);
        emptyText = view.findViewById(R.id.text_empty_selection);
        actionButton = view.findViewById(R.id.button_apply_selection);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntrantSelectionAdapter(eligibleEntrantIds);
        recyclerView.setAdapter(adapter);

        titleText.setText(getActionTitle());
        subtitleText.setText(getActionSubtitle());
        actionButton.setText(getActionButtonLabel());
        actionButton.setEnabled(false);

        adapter.setOnSelectionChangedListener(selectedCount ->
                actionButton.setEnabled(selectedCount > 0 || ACTION_SYNC_PRIVATE_SELECTION.equals(action))
        );

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        actionButton.setOnClickListener(v -> applySelectedEntrants());

        loadEligibleEntrants();
    }

    /**
     * Loads the eligible entrant ids for the current action.
     */
    private void loadEligibleEntrants() {
        if (TextUtils.isEmpty(eventId) || TextUtils.isEmpty(action)) {
            showEmptyState();
            return;
        }

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        organizerDatabaseManager.getEventById(eventId)
                .addOnSuccessListener(event ->
                        organizerDatabaseManager.getAllEntrantProfileIds()
                                .addOnSuccessListener(allEntrantIds ->
                                        bindEligibleEntrants(event, allEntrantIds))
                                .addOnFailureListener(e -> showEmptyState())
                )
                .addOnFailureListener(e -> showEmptyState());
    }

    /**
     * Filters the entrant ids shown for this action.
     *
     * @param event event context
     * @param allEntrantIds all entrant profile ids
     */
    private void bindEligibleEntrants(@NonNull Event event, @Nullable ArrayList<String> allEntrantIds) {
        eligibleEntrantIds.clear();

        if (allEntrantIds == null) {
            adapter.notifyDataSetChanged();
            showEmptyState();
            return;
        }

        if (ACTION_SYNC_PRIVATE_SELECTION.equals(action)) {
            eligibleEntrantIds.addAll(allEntrantIds);
            Collections.sort(eligibleEntrantIds);

            ArrayList<String> preselectedIds = new ArrayList<>();
            preselectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
            preselectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));

            adapter.notifyDataSetChanged();
            adapter.setSelectedEntrantIds(preselectedIds);

            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            return;
        }

        ArrayList<String> idsInTargetState = getIdsInRelevantState(event);

        if (isAddAction()) {
            eligibleEntrantIds.addAll(allEntrantIds);
            eligibleEntrantIds.removeAll(idsInTargetState);
        } else {
            eligibleEntrantIds.addAll(idsInTargetState);
        }

        Collections.sort(eligibleEntrantIds);

        adapter.clearSelection();
        adapter.notifyDataSetChanged();

        if (eligibleEntrantIds.isEmpty()) {
            showEmptyState();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    /**
     * Applies the current ADA action to the selected entrant ids.
     */
    private void applySelectedEntrants() {
        ArrayList<String> selectedEntrantIds = adapter.getSelectedEntrantIds();

        if (selectedEntrantIds.isEmpty() && !ACTION_SYNC_PRIVATE_SELECTION.equals(action)) {
            return;
        }

        showStyledDialog(
                getActionTitle(),
                "Apply this action to the selected entrants?",
                "Yes",
                "No",
                new Runnable() {
                    @Override
                    public void run() {
                        actionButton.setEnabled(false);

                        runAction(selectedEntrantIds)
                                .addOnSuccessListener(unused -> {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    if (returnToOrganizerEvents) {
                                        String message = ACTION_SYNC_PRIVATE_SELECTION.equals(action)
                                                ? "The private event entrant list was updated successfully."
                                                : "The selected entrants were invited successfully.";

                                        showStyledDialog(
                                                "Action Complete",
                                                message,
                                                "OK",
                                                null,
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
                                                                        new OrganizerYourEventsFragment()
                                                                )
                                                                .commit();
                                                    }
                                                }
                                        );
                                        return;
                                    }

                                    showStyledMessageDialog(
                                            "Action Complete",
                                            "The selected entrants were updated successfully."
                                    );

                                    loadEligibleEntrants();
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded()) {
                                        return;
                                    }

                                    String message = "Something went wrong.";
                                    if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
                                        message = e.getMessage();
                                    }

                                    showStyledMessageDialog("Action Failed", message);

                                    actionButton.setEnabled(true);
                                });
                    }
                }
        );
    }

    /**
     * Runs the chosen backend action.
     *
     * @param selectedEntrantIds selected entrant ids
     * @return task for the backend work
     */
    private Task<Void> runAction(ArrayList<String> selectedEntrantIds) {
        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        switch (action) {
            case ACTION_WAITLIST:
                return organizerDatabaseManager.waitlistEntrants(eventId, selectedEntrantIds);

            case ACTION_REMOVE_WAITLIST:
                return organizerDatabaseManager.removeWaitlistEntrants(eventId, selectedEntrantIds);

            case ACTION_INVITE:
                return organizerDatabaseManager.inviteEntrants(eventId, selectedEntrantIds);

            case ACTION_REMOVE_INVITE:
                return organizerDatabaseManager.removeInviteEntrants(eventId, selectedEntrantIds);

            case ACTION_ENROLL:
                return organizerDatabaseManager.enrollEntrants(eventId, selectedEntrantIds);

            case ACTION_REMOVE_ENROLL:
                return organizerDatabaseManager.removeEnrollEntrants(eventId, selectedEntrantIds);

            case ACTION_SYNC_PRIVATE_SELECTION:
                return organizerDatabaseManager.syncPrivateEventSelection(eventId, selectedEntrantIds);

            default:
                return organizerDatabaseManager.waitlistEntrants(eventId, new ArrayList<>());
        }
    }

    /**
     * Gets the ids already in the state.
     *
     * @param event event context
     * @return entrant ids
     */
    private ArrayList<String> getIdsInRelevantState(@NonNull Event event) {
        switch (action) {
            case ACTION_WAITLIST:
            case ACTION_REMOVE_WAITLIST:
                return event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED);

            case ACTION_INVITE:
            case ACTION_REMOVE_INVITE:
                return event.getEntrantIdsByStatus(EventEntrantStatus.INVITED);

            case ACTION_ENROLL:
            case ACTION_REMOVE_ENROLL:
                return event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED);

            default:
                return new ArrayList<>();
        }
    }

    /**
     * Returns true if this action adds entrants into a state.
     */
    private boolean isAddAction() {
        return ACTION_WAITLIST.equals(action)
                || ACTION_INVITE.equals(action)
                || ACTION_ENROLL.equals(action);
    }

    /**
     * Gets the page title for the current action.
     */
    private String getActionTitle() {
        switch (action) {
            case ACTION_WAITLIST:
                return "Waitlist Entrants";
            case ACTION_REMOVE_WAITLIST:
                return "Remove Waitlist Entrants";
            case ACTION_INVITE:
                return "Invite Entrants";
            case ACTION_REMOVE_INVITE:
                return "Remove Invite Entrants";
            case ACTION_ENROLL:
                return "Enroll Entrants";
            case ACTION_REMOVE_ENROLL:
                return "Remove Enroll Entrants";
            case ACTION_SYNC_PRIVATE_SELECTION:
                return "Invite Entrants";
            default:
                return "Entrant Selection";
        }
    }

    /**
     * Gets the page sub for the current action.
     */
    private String getActionSubtitle() {
        if (ACTION_SYNC_PRIVATE_SELECTION.equals(action)) {
            return "Select entrant ids for this private event.";
        }

        if (isAddAction()) {
            return "Select entrant ids to add to this event state.";
        }
        return "Select entrant ids to remove from this event state.";
    }

    /**
     * Gets the floating button label.
     */
    private String getActionButtonLabel() {
        if (ACTION_SYNC_PRIVATE_SELECTION.equals(action)) {
            return "Apply";
        }
        return isAddAction() ? "Add" : "Remove";
    }

    /**
     * Shows the empty list message.
     */
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }

    /**
     * Shows a popup with acknowledgement.
     *
     * @param title popup title
     * @param message popup message
     */
    private void showStyledMessageDialog(String title, String message) {
        showStyledDialog(title, message, "OK", null, null);
    }

    /**
     * Shows a popup dialog widget.
     *
     * @param title dialog title
     * @param message dialog message
     * @param positiveText positive button text
     * @param negativeText negative button text, or null to hide
     * @param onPositive action to run when the positive button is pressed
     */
    private void showStyledDialog(String title,
                                  String message,
                                  String positiveText,
                                  @Nullable String negativeText,
                                  @Nullable Runnable onPositive) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.widget_custom_dialog, null, false);

        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);
        Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);

        titleText.setText(title);
        messageText.setText(message);
        positiveButton.setText(positiveText);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

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
}
