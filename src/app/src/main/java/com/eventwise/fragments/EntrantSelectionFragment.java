package com.eventwise.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.Enum.EventEntrantStatus;
import com.eventwise.R;
import com.eventwise.adapters.EntrantSelectionAdapter;
import com.eventwise.adapters.InviteProfileAdapter;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.items.AdminProfileItem;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Lets organizer select many entrant ids for one developer action,
 * or search and invite profiles for private events and co-organizers.
 *
 * @author Becca Irving
 * @since Mar 29 2026
 */
public class EntrantSelectionFragment extends Fragment {

    public static final String ACTION_WAITLIST = "action_waitlist";
    public static final String ACTION_REMOVE_WAITLIST = "action_remove_waitlist";
    public static final String ACTION_INVITE = "action_invite";
    public static final String ACTION_REMOVE_INVITE = "action_remove_invite";
    public static final String ACTION_ENROLL = "action_enroll";
    public static final String ACTION_REMOVE_ENROLL = "action_remove_enroll";
    public static final String ACTION_SYNC_PRIVATE_SELECTION = "action_sync_private_selection";
    public static final String ACTION_PRIVATE_INVITE_SEARCH = "action_private_invite_search";
    public static final String ACTION_CO_ORGANIZER_INVITE = "action_co_organizer_invite";

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_ACTION = "arg_action";
    private static final String ARG_RETURN_TO_ORGANIZER_EVENTS = "arg_return_to_organizer_events";
    private static final String ARG_CONTINUE_TO_PRIVATE_SELECTION = "arg_continue_to_private_selection";

    private String eventId = "";
    private String action = "";
    private boolean returnToOrganizerEvents = false;
    private boolean continueToPrivateSelection = false;

    private TextView titleText;
    private TextView subtitleText;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Button actionButton;
    private EditText searchInput;

    private EntrantSelectionAdapter checkboxAdapter;
    private InviteProfileAdapter inviteProfileAdapter;

    private final ArrayList<String> eligibleEntrantIds = new ArrayList<>();
    private final ArrayList<AdminProfileItem> allProfiles = new ArrayList<>();
    private final ArrayList<AdminProfileItem> filteredProfiles = new ArrayList<>();
    private final Set<String> disabledInviteProfileIds = new HashSet<>();

    private Event currentEvent;

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
        args.putString(ARG_ACTION, ACTION_PRIVATE_INVITE_SEARCH);
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
        args.putString(ARG_ACTION, ACTION_PRIVATE_INVITE_SEARCH);
        args.putBoolean(ARG_RETURN_TO_ORGANIZER_EVENTS, true);
        fragment.setArguments(args);
        return fragment;
    }

    public static EntrantSelectionFragment newCoOrganizerInviteInstance(@NonNull String eventId,
                                                                        boolean continueToPrivateSelection) {
        EntrantSelectionFragment fragment = new EntrantSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ACTION, ACTION_CO_ORGANIZER_INVITE);
        args.putBoolean(ARG_RETURN_TO_ORGANIZER_EVENTS, true);
        args.putBoolean(ARG_CONTINUE_TO_PRIVATE_SELECTION, continueToPrivateSelection);
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
            continueToPrivateSelection = args.getBoolean(ARG_CONTINUE_TO_PRIVATE_SELECTION, false);
        }

        View backButton = view.findViewById(R.id.button_back);
        titleText = view.findViewById(R.id.text_selection_title);
        subtitleText = view.findViewById(R.id.text_selection_subtitle);
        recyclerView = view.findViewById(R.id.recycler_view_entrant_selection);
        emptyText = view.findViewById(R.id.text_empty_selection);
        actionButton = view.findViewById(R.id.button_apply_selection);
        searchInput = view.findViewById(R.id.input_search_profiles);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        titleText.setText(getActionTitle());
        subtitleText.setText(getActionSubtitle());

        if (isSearchInviteMode()) {
            setupSearchInviteMode();
        } else {
            setupCheckboxMode();
        }

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        loadEligibleEntrants();
    }

    private void setupCheckboxMode() {
        searchInput.setVisibility(View.GONE);

        checkboxAdapter = new EntrantSelectionAdapter(eligibleEntrantIds);
        recyclerView.setAdapter(checkboxAdapter);

        actionButton.setText(getActionButtonLabel());
        actionButton.setEnabled(false);

        checkboxAdapter.setOnSelectionChangedListener(selectedCount ->
                actionButton.setEnabled(selectedCount > 0 || ACTION_SYNC_PRIVATE_SELECTION.equals(action))
        );

        actionButton.setOnClickListener(v -> applySelectedEntrants());
    }

    private void setupSearchInviteMode() {
        searchInput.setVisibility(View.VISIBLE);

        inviteProfileAdapter = new InviteProfileAdapter( filteredProfiles, disabledInviteProfileIds,this::inviteProfile );
        recyclerView.setAdapter(inviteProfileAdapter);
        actionButton.setText("Done");
        actionButton.setEnabled(true);
        actionButton.setOnClickListener(v -> finishSearchInviteFlow());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean isSearchInviteMode() {
        return ACTION_PRIVATE_INVITE_SEARCH.equals(action)|| ACTION_SYNC_PRIVATE_SELECTION.equals(action)|| ACTION_CO_ORGANIZER_INVITE.equals(action);
    }

    private void loadEligibleEntrants() {
        if (TextUtils.isEmpty(eventId) || TextUtils.isEmpty(action)) {
            showEmptyState();
            return;
        }

        if (isSearchInviteMode()) {
            loadSearchProfiles();
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

    private void loadSearchProfiles() {
        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        organizerDatabaseManager.getEventById(eventId)
                .addOnSuccessListener(event -> {
                    currentEvent = event;

                    organizerDatabaseManager.getAllEntrants()
                            .addOnSuccessListener(this::bindSearchProfiles)
                            .addOnFailureListener(e -> showEmptyState());
                })
                .addOnFailureListener(e -> showEmptyState());
    }

    private void bindSearchProfiles(@Nullable ArrayList<Entrant> entrants) {
        allProfiles.clear();
        filteredProfiles.clear();
        disabledInviteProfileIds.clear();

        if (entrants == null || currentEvent == null) {
            showEmptyState();
            return;
        }

        for (Entrant entrant : entrants) {
            if (entrant == null || entrant.getProfileId() == null || entrant.getProfileId().trim().isEmpty()) {
                continue;
            }

            allProfiles.add(new AdminProfileItem(safeText(entrant.getProfileId()),safeText(entrant.getName()),safeText(entrant.getEmail()), safeText(entrant.getPhone()) ));
        }

        Collections.sort(allProfiles, new Comparator<AdminProfileItem>() {
            @Override
            public int compare(AdminProfileItem first, AdminProfileItem second) {
                String firstName = first.getName() == null ? "" : first.getName().toLowerCase();
                String secondName = second.getName() == null ? "" : second.getName().toLowerCase();

                int byName = firstName.compareTo(secondName);
                if (byName != 0) {
                    return byName;
                }

                String firstId = first.getProfileId() == null ? "" : first.getProfileId().toLowerCase();
                String secondId = second.getProfileId() == null ? "" : second.getProfileId().toLowerCase();
                return firstId.compareTo(secondId);
            }
        });

        if (ACTION_CO_ORGANIZER_INVITE.equals(action)) {
            disabledInviteProfileIds.add(entrantProfileIdFromOrganizerId(currentEvent.getOrganizerProfileId()));

            for (String coOrganizerProfileId : currentEvent.getCoOrganizerProfileIds()) {
                disabledInviteProfileIds.add(entrantProfileIdFromOrganizerId(coOrganizerProfileId));
            }
        } else {
            disabledInviteProfileIds.addAll(currentEvent.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
            disabledInviteProfileIds.addAll(currentEvent.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));
        }

        if (inviteProfileAdapter != null) {
            inviteProfileAdapter.setDisabledProfileIds(disabledInviteProfileIds);
        }

        filterProfiles(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    private void filterProfiles(@NonNull String query) {
        filteredProfiles.clear();

        String normalizedQuery = query.trim().toLowerCase();

        for (AdminProfileItem item : allProfiles) {
            if (matchesSearch(item, normalizedQuery)) {
                filteredProfiles.add(item);
            }
        }

        if (inviteProfileAdapter != null) {
            inviteProfileAdapter.notifyDataSetChanged();
        }

        if (filteredProfiles.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No matching entrants found.");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private boolean matchesSearch(@NonNull AdminProfileItem item, @NonNull String query) {
        if (query.isEmpty()) {
            return true;
        }

        return containsIgnoreCase(item.getName(), query)|| containsIgnoreCase(item.getProfileId(), query) || containsIgnoreCase(item.getEmail(), query) || containsIgnoreCase(item.getPhone(), query);
    }

    private boolean containsIgnoreCase(@Nullable String value, @NonNull String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void inviteProfile(@NonNull AdminProfileItem item) {
        if (currentEvent == null || item.getProfileId() == null || item.getProfileId().trim().isEmpty()) {
            return;
        }

        if (disabledInviteProfileIds.contains(item.getProfileId())) {
            return;
        }

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        if (ACTION_CO_ORGANIZER_INVITE.equals(action)) {
            organizerDatabaseManager.inviteCoOrganizer(eventId, item.getProfileId())
                    .addOnSuccessListener(unused -> {
                        disabledInviteProfileIds.add(item.getProfileId());
                        if (inviteProfileAdapter != null) {
                            inviteProfileAdapter.setDisabledProfileIds(disabledInviteProfileIds);
                        }
                    })
                    .addOnFailureListener(e ->
                            showStyledMessageDialog("Invite Failed", "Could not invite that co-organizer.")
                    );
            return;
        }

        ArrayList<String> selectedIds = new ArrayList<>();
        selectedIds.add(item.getProfileId());

        organizerDatabaseManager.inviteEntrants(eventId, selectedIds)
                .addOnSuccessListener(unused -> {
                    disabledInviteProfileIds.add(item.getProfileId());
                    if (inviteProfileAdapter != null) {
                        inviteProfileAdapter.setDisabledProfileIds(disabledInviteProfileIds);
                    }
                })
                .addOnFailureListener(e ->
                        showStyledMessageDialog("Invite Failed", "Could not invite that entrant.")
                );
    }

    private void finishSearchInviteFlow() {
        if (!isAdded()) {
            return;
        }

        if (ACTION_CO_ORGANIZER_INVITE.equals(action) && continueToPrivateSelection) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.organizer_fragment_container,
                            EntrantSelectionFragment.newPrivateInviteInstance(eventId)
                    )
                    .addToBackStack(null)
                    .commit();
            return;
        }

        if (returnToOrganizerEvents) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerYourEventsFragment())
                    .commit();
            return;
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void bindEligibleEntrants(@NonNull Event event, @Nullable ArrayList<String> allEntrantIds) {
        eligibleEntrantIds.clear();

        if (allEntrantIds == null) {
            if (checkboxAdapter != null) {
                checkboxAdapter.notifyDataSetChanged();
            }
            showEmptyState();
            return;
        }

        if (ACTION_SYNC_PRIVATE_SELECTION.equals(action)) {
            eligibleEntrantIds.addAll(allEntrantIds);
            Collections.sort(eligibleEntrantIds);

            ArrayList<String> preselectedIds = new ArrayList<>();
            preselectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.INVITED));
            preselectedIds.addAll(event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED));

            if (checkboxAdapter != null) {
                checkboxAdapter.notifyDataSetChanged();
                checkboxAdapter.setSelectedEntrantIds(preselectedIds);
            }

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

        if (checkboxAdapter != null) {
            checkboxAdapter.clearSelection();
            checkboxAdapter.notifyDataSetChanged();
        }

        if (eligibleEntrantIds.isEmpty()) {
            showEmptyState();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private void applySelectedEntrants() {
        if (checkboxAdapter == null) {
            return;
        }

        ArrayList<String> selectedEntrantIds = checkboxAdapter.getSelectedEntrantIds();

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
            case ACTION_PRIVATE_INVITE_SEARCH:
                return "Invite Entrants";
            case ACTION_CO_ORGANIZER_INVITE:
                return "Invite Co-organizer";
            default:
                return "Entrant Selection";
        }
    }

    /**
     * Gets the page sub for the current action.
     */
    private String getActionSubtitle() {
        switch (action) {
            case ACTION_PRIVATE_INVITE_SEARCH:
            case ACTION_SYNC_PRIVATE_SELECTION:
                return "Search by name, id, email, or phone and invite entrants to this private event.";
            case ACTION_CO_ORGANIZER_INVITE:
                return "Search by name, id, email, or phone and invite one or more co-organizers.";
            default:
                if (isAddAction()) {
                    return "Select entrant ids to add to this event state.";
                }
                return "Select entrant ids to remove from this event state.";
        }
    }

    /**
     * Gets the floating button label.
     */
    private String getActionButtonLabel() {
        return isAddAction() ? "Add" : "Remove";
    }

    /**
     * Shows the empty list message.
     */
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);

        if (isSearchInviteMode()) {
            emptyText.setText("No matching entrants found.");
        } else {
            emptyText.setText("No eligible entrants found.");
        }
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

        TextView dialogTitleText = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessageText = dialogView.findViewById(R.id.dialog_message);
        Button negativeButton = dialogView.findViewById(R.id.dialog_negative_button);
        Button positiveButton = dialogView.findViewById(R.id.dialog_positive_button);

        dialogTitleText.setText(title);
        dialogMessageText.setText(message);
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

    private String organizerProfileIdFromEntrantId(@Nullable String entrantProfileId) {
        if (entrantProfileId == null || entrantProfileId.trim().isEmpty()) {
            return "";
        }

        if (entrantProfileId.endsWith("_entrant")) {
            return entrantProfileId.substring(0, entrantProfileId.length() - "_entrant".length()) + "_organizer";
        }

        return entrantProfileId + "_organizer";
    }

    private String entrantProfileIdFromOrganizerId(@Nullable String organizerProfileId) {
        if (organizerProfileId == null || organizerProfileId.trim().isEmpty()) {
            return "";
        }

        if (organizerProfileId.endsWith("_organizer")) {
            return organizerProfileId.substring(0, organizerProfileId.length() - "_organizer".length()) + "_entrant";
        }

        return organizerProfileId + "_entrant";
    }

    private String safeText(@Nullable String value) {
        return value == null ? "" : value;
    }
}
