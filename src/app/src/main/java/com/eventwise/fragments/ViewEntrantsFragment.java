package com.eventwise.fragments;

import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
    private Button exportCsvButton;
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
        exportCsvButton = view.findViewById(R.id.button_export_csv);
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
        exportCsvButton.setOnClickListener(v -> exportRegisteredEntrantsCsv());

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        clearSelectedState();
        showEmptyStateOnly();
        updateExportCsvButtonVisibility();
    }

    /**
     * Changes selected entrant filter
     *
     * @param filter filter
     */
    private void selectFilter(String filter) {
        selectedFilter = filter;
        updateButtonColors();
        updateExportCsvButtonVisibility();
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

    /**
     * Shows the export CSV button only for the registered filter.
     */
    private void updateExportCsvButtonVisibility() {
        if (exportCsvButton == null) {
            return;
        }

        if (FILTER_REGISTERED.equals(selectedFilter)) {
            exportCsvButton.setVisibility(View.VISIBLE);
        } else {
            exportCsvButton.setVisibility(View.GONE);
        }
    }

    /**
     * Exports the currently loaded registered entrant list as a CSV in Downloads.
     */
    private void exportRegisteredEntrantsCsv() {
        if (!FILTER_REGISTERED.equals(selectedFilter)) {
            return;
        }

        if (displayedEntrants.isEmpty()) {
            Toast.makeText(requireContext(), "No registered entrants to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(requireContext(), "CSV export requires Android 10 or newer.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = buildCsvFileName();
        String csvContent = buildRegisteredEntrantsCsvContent();

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = null;

        try {
            uri = requireContext().getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
            );

            if (uri == null) {
                throw new IllegalStateException("Could not create CSV file.");
            }

            try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    throw new IllegalStateException("Could not open CSV file.");
                }

                outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
            }

            ContentValues completedValues = new ContentValues();
            completedValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
            requireContext().getContentResolver().update(uri, completedValues, null, null);

            Toast.makeText(requireContext(), "CSV exported to Downloads.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ViewEntrants", "Failed to export CSV", e);

            if (uri != null) {
                requireContext().getContentResolver().delete(uri, null, null);
            }

            Toast.makeText(requireContext(), "Failed to export CSV.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Builds the export CSV file name.
     */
    private String buildCsvFileName() {
        String eventName = "";

        if (currentEvent != null) {
            eventName = safeText(currentEvent.getName());
        }

        if (eventName.trim().isEmpty()) {
            eventName = safeText(eventId);
        }

        eventName = eventName.replaceAll("[\\\\/:*?\"<>|]", "_").trim();

        if (eventName.isEmpty()) {
            eventName = "event";
        }

        return "event_" + eventName + "_registered_entrants.csv";
    }

    /**
     * Builds the CSV content for registered entrants.
     */
    private String buildRegisteredEntrantsCsvContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("User Id,Name,Email,Phone\n");

        for (EntrantListItem entrant : displayedEntrants) {
            builder.append(escapeCsv(removeUserIdPrefix(entrant.getDeviceId()))).append(",");
            builder.append(escapeCsv(entrant.getName())).append(",");
            builder.append(escapeCsv(entrant.getEmail())).append(",");
            builder.append(escapeCsv(entrant.getPhone())).append("\n");
        }

        return builder.toString();
    }

    /**
     * Escapes one CSV value.
     */
    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        safeValue = safeValue.replace("\"", "\"\"");
        return "\"" + safeValue + "\"";
    }

    /**
     * Removes the display prefix from the entrant id for CSV export.
     */
    private String removeUserIdPrefix(String value) {
        String safeValue = safeText(value);

        if (safeValue.startsWith("User Id: ")) {
            return safeValue.substring("User Id: ".length());
        }

        return safeValue;
    }
}
