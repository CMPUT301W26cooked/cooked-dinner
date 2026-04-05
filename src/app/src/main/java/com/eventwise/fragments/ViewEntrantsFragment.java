package com.eventwise.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.R;
import com.eventwise.adapters.EntrantListAdapter;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.items.EntrantListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Page for viewing entrant lists by status for one event.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class ViewEntrantsFragment extends Fragment {

    /**
     * TODO (ViewEntrantsFragment.java)
     * - Need waitlisted map and registered CSV features in l8r
     * - Add tests for all four filter states.
     */

    private static final String ARG_EVENT_ID = "arg_event_id";

    private static final String FILTER_WAITLISTED = "waitlisted";
    private static final String FILTER_INVITEES = "invitees";
    private static final String FILTER_CANCELED = "canceled";
    private static final String FILTER_REGISTERED = "registered";

    private Button waitlistedButton;
    private Button inviteesButton;
    private Button canceledButton;
    private Button registeredButton;
    private View backButton;
    private RecyclerView recyclerView;
    private TextView emptyText;

    private EntrantListAdapter adapter;
    private final List<EntrantListItem> displayedEntrants = new ArrayList<>();

    private OrganizerDatabaseManager organizerDatabaseManager;
    private EntrantDatabaseManager entrantDatabaseManager;

    private String selectedFilter = "";
    private String eventId = "";
    private Event currentEvent;
    private View mapContainer;

    public ViewEntrantsFragment() {
    }

    /**
     * Make a new entrants list fragmnt.
     *
     * @param eventId event id
     * @return new fragment
     */
    public static ViewEntrantsFragment newInstance(String eventId) {
        ViewEntrantsFragment fragment = new ViewEntrantsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_entrants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        organizerDatabaseManager = new OrganizerDatabaseManager();
        entrantDatabaseManager = new EntrantDatabaseManager();

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID, "");
        }
        organizerDatabaseManager.getEventById(eventId)
                .addOnSuccessListener(event -> currentEvent = event);

        waitlistedButton = view.findViewById(R.id.button_waitlisted);
        inviteesButton = view.findViewById(R.id.button_invitees);
        canceledButton = view.findViewById(R.id.button_canceled);
        registeredButton = view.findViewById(R.id.button_registered);
        backButton = view.findViewById(R.id.button_back);
        recyclerView = view.findViewById(R.id.recycler_view_entrants);
        emptyText = view.findViewById(R.id.text_empty_entrants);
        mapContainer = view.findViewById(R.id.map_container);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EntrantListAdapter(displayedEntrants);
        recyclerView.setAdapter(adapter);

        waitlistedButton.setOnClickListener(v -> selectFilter(FILTER_WAITLISTED));
        inviteesButton.setOnClickListener(v -> selectFilter(FILTER_INVITEES));
        canceledButton.setOnClickListener(v -> selectFilter(FILTER_CANCELED));
        registeredButton.setOnClickListener(v -> selectFilter(FILTER_REGISTERED));

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        clearSelectedState();
        showEmptyStateOnly();
    }

    /**
     * Changes selected entrant filter
     *
     * @param filter filter
     */
    private void selectFilter(String filter) {
        selectedFilter = filter;
        updateButtonColors();
        if (FILTER_WAITLISTED.equals(filter)) {

            mapContainer.setVisibility(View.VISIBLE);

            if (currentEvent != null) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.map_container,
                                OrganizerEventMapFragment.newInstance(currentEvent)
                        )
                        .commit();
            }

        } else {
            mapContainer.setVisibility(View.GONE);
        }
        loadSelectedFilterFromBackend();
    }

    /**
     * Loads the selected entrant status list from Firestore.
     */
    private void loadSelectedFilterFromBackend() {
        if (eventId == null || eventId.trim().isEmpty()) {
            displayedEntrants.clear();
            adapter.notifyDataSetChanged();
            showEmptyStateOnly();
            return;
        }

        switch (selectedFilter) {
            case FILTER_WAITLISTED:
                organizerDatabaseManager.getEntrantsIdsInWaitingListFromEventId(eventId)
                        .addOnSuccessListener(this::loadEntrantsFromIds)
                        .addOnFailureListener(e -> handleLoadFailure("waitlisted", e));
                break;

            case FILTER_INVITEES:
                organizerDatabaseManager.getEntrantsIdsInChosenList(eventId)
                        .addOnSuccessListener(this::loadEntrantsFromIds)
                        .addOnFailureListener(e -> handleLoadFailure("invitees", e));
                break;

            case FILTER_CANCELED:
                organizerDatabaseManager.getEntrantsIdsInCancelledListFromEventId(eventId)
                        .addOnSuccessListener(this::loadEntrantsFromIds)
                        .addOnFailureListener(e -> handleLoadFailure("canceled", e));
                break;

            case FILTER_REGISTERED:
                organizerDatabaseManager.getEntrantsIdsInConfirmedListFromEventId(eventId)
                        .addOnSuccessListener(this::loadEntrantsFromIds)
                        .addOnFailureListener(e -> handleLoadFailure("registered", e));
                break;

            default:
                displayedEntrants.clear();
                adapter.notifyDataSetChanged();
                showEmptyStateOnly();
                break;
        }
    }

    /**
     * Converts entrant ids into display-able items.
     *
     * @param entrantIds entrant ids from one event status list
     */
    private void loadEntrantsFromIds(List<String> entrantIds) {
        displayedEntrants.clear();

        if (entrantIds == null || entrantIds.isEmpty()) {
            adapter.notifyDataSetChanged();
            showEmptyStateOnly();
            return;
        }

        fetchEntrantsSequentially(entrantIds, 0, new ArrayList<>());
    }

    /**
     * loads entrant docs so display order is stable.
     */
    private void fetchEntrantsSequentially(List<String> entrantIds, int index, List<EntrantListItem> results) {
        if (index >= entrantIds.size()) {
            displayedEntrants.clear();
            displayedEntrants.addAll(results);
            adapter.notifyDataSetChanged();

            if (displayedEntrants.isEmpty()) {
                showEmptyStateOnly();
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
            }
            return;
        }

        String entrantId = entrantIds.get(index);

        entrantDatabaseManager.getEntrantFromId(entrantId)
                .addOnSuccessListener(entrant -> {
                    if (entrant != null) {
                        String displayId = safeEntrantId(entrant);
                        results.add(new EntrantListItem(
                                "User Id: " + displayId,
                                safeText(entrant.getName()),
                                safeText(entrant.getEmail()),
                                safeText(entrant.getPhone())
                        ));
                    } else {
                        results.add(new EntrantListItem(
                                "User Id: " + safeText(entrantId),
                                "",
                                "",
                                ""
                        ));
                    }

                    fetchEntrantsSequentially(entrantIds, index + 1, results);
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewEntrants", "Failed to load entrant " + entrantId, e);

                    results.add(new EntrantListItem(
                            "User Id: " + safeText(entrantId),
                            "",
                            "",
                            ""
                    ));

                    fetchEntrantsSequentially(entrantIds, index + 1, results);
                });
    }

    /**
     * Handles status list loading failures.
     */
    private void handleLoadFailure(String filterName, Exception e) {
        Log.e("ViewEntrants", "Failed to load " + filterName + " entrant list", e);
        displayedEntrants.clear();
        adapter.notifyDataSetChanged();
        showEmptyStateOnly();
    }

    /**
     * Shows only the empty state text.
     */
    private void showEmptyStateOnly() {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }

    /**
     * Clears the selected button styling.
     */
    private void clearSelectedState() {
        setButtonUnselected(waitlistedButton);
        setButtonUnselected(inviteesButton);
        setButtonUnselected(canceledButton);
        setButtonUnselected(registeredButton);
    }

    /**
     * Updates the selected and unselected button colors.
     */
    private void updateButtonColors() {
        clearSelectedState();

        switch (selectedFilter) {
            case FILTER_WAITLISTED:
                setButtonSelected(waitlistedButton);
                break;
            case FILTER_INVITEES:
                setButtonSelected(inviteesButton);
                break;
            case FILTER_CANCELED:
                setButtonSelected(canceledButton);
                break;
            case FILTER_REGISTERED:
                setButtonSelected(registeredButton);
                break;
        }
    }

    /**
     * Styles a selected filter button.
     *
     * @param button button to style
     */
    private void setButtonSelected(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.weird_piss, null)
        ));
        button.setTextColor(getResources().getColor(R.color.moss_green, null));
    }

    /**
     * Styles an unselected filter button.
     *
     * @param button button to style
     */
    private void setButtonUnselected(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.moss_green, null)
        ));
        button.setTextColor(getResources().getColor(R.color.lighter_green, null));
    }

    /**
     * Returns a non-null display string.
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }

    /**
     * Chooses the best available entrant identifier for display.
     */
    private String safeEntrantId(Entrant entrant) {
        if (entrant.getProfileId() != null && !entrant.getProfileId().trim().isEmpty()) {
            return entrant.getProfileId();
        }
        return safeText(entrant.getProfileId());
    }
}
