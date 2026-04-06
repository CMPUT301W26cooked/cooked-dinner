package com.eventwise.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.R;
import com.eventwise.adapters.InviteProfileAdapter;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.items.AdminProfileItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Search-based invite screen for private entrant invites and co-organizer invites.
 *
 * @author Becca Irving
 * @since 2026-04-06
 */
public class InviteProfileSelectionFragment extends Fragment {

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_MODE = "arg_mode";
    private static final String ARG_CONTINUE_TO_PRIVATE_ENTRANTS = "arg_continue_to_private_entrants";

    private static final String MODE_PRIVATE_ENTRANTS = "mode_private_entrants";
    private static final String MODE_CO_ORGANIZER = "mode_co_organizer";
    private String eventId = "";
    private String mode = MODE_PRIVATE_ENTRANTS;
    private boolean continueToPrivateEntrants = false;

    private TextView titleText;
    private TextView subtitleText;
    private EditText searchInput;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Button doneButton;
    private final ArrayList<AdminProfileItem> allProfiles = new ArrayList<>();
    private final ArrayList<AdminProfileItem> filteredProfiles = new ArrayList<>();
    private final Set<String> originalUnavailableProfileIds = new HashSet<>();
    private final Set<String> unavailableProfileIds = new HashSet<>();

    private InviteProfileAdapter adapter;


    public InviteProfileSelectionFragment() {
    }

    public static InviteProfileSelectionFragment newPrivateInviteInstance(@NonNull String eventId) {
        InviteProfileSelectionFragment fragment = new InviteProfileSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_MODE, MODE_PRIVATE_ENTRANTS);
        args.putBoolean(ARG_CONTINUE_TO_PRIVATE_ENTRANTS, false);
        fragment.setArguments(args);
        return fragment;
    }

    public static InviteProfileSelectionFragment newCoOrganizerInviteInstance(@NonNull String eventId, boolean continueToPrivateEntrants) {
        InviteProfileSelectionFragment fragment = new InviteProfileSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_MODE, MODE_CO_ORGANIZER);
        args.putBoolean(ARG_CONTINUE_TO_PRIVATE_ENTRANTS, continueToPrivateEntrants);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_profile_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID, "");
            mode = args.getString(ARG_MODE, MODE_PRIVATE_ENTRANTS);
            continueToPrivateEntrants = args.getBoolean(ARG_CONTINUE_TO_PRIVATE_ENTRANTS, false);
        }

        View backButton = view.findViewById(R.id.button_back);
        titleText = view.findViewById(R.id.text_selection_title);
        subtitleText = view.findViewById(R.id.text_selection_subtitle);
        searchInput = view.findViewById(R.id.input_profile_search);
        recyclerView = view.findViewById(R.id.recycler_view_profile_selection);
        emptyText = view.findViewById(R.id.text_empty_selection);
        doneButton = view.findViewById(R.id.button_done_selection);

        titleText.setText(getScreenTitle());
        subtitleText.setText(getScreenSubtitle());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InviteProfileAdapter(filteredProfiles, unavailableProfileIds,
                item -> {
                    if (item == null || item.getProfileId() == null) {
                        return;
                    }
                    unavailableProfileIds.add(item.getProfileId());
                    adapter.notifyDataSetChanged();
                }
        );
        recyclerView.setAdapter(adapter);

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        doneButton.setOnClickListener(v -> applyInvites());
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

        loadProfiles();
    }

    private void loadProfiles() {
        if (TextUtils.isEmpty(eventId)) {
            showEmptyState();
            return;
        }

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();
        organizerDatabaseManager.getEventById(eventId)
                .addOnSuccessListener(event ->
                        organizerDatabaseManager.getAllEntrants()
                                .addOnSuccessListener(entrants -> bindProfiles(event, entrants))
                                .addOnFailureListener(e -> showEmptyState())
                )
                .addOnFailureListener(e -> showEmptyState());
    }

    private void bindProfiles(@NonNull Event event, @Nullable ArrayList<Entrant> entrants) {
        allProfiles.clear();
        filteredProfiles.clear();
        originalUnavailableProfileIds.clear();
        unavailableProfileIds.clear();

        if (entrants == null) {
            showEmptyState();
            return;
        }

        for (Entrant entrant : entrants) {
            if (entrant == null || entrant.getProfileId() == null || entrant.getProfileId().trim().isEmpty()) {
                continue;
            }

            AdminProfileItem item = new AdminProfileItem(entrant.getProfileId(), safeName(entrant), safeText(entrant.getEmail()), safeText(entrant.getPhone()));

            allProfiles.add(item);

            if (MODE_PRIVATE_ENTRANTS.equals(mode)) {
                if (event.getEntrantIdsByStatus(com.eventwise.Enum.EventEntrantStatus.INVITED).contains(entrant.getProfileId())
                        || event.getEntrantIdsByStatus(com.eventwise.Enum.EventEntrantStatus.ENROLLED).contains(entrant.getProfileId())) {
                    originalUnavailableProfileIds.add(entrant.getProfileId());
                }
            } else if (MODE_CO_ORGANIZER.equals(mode)) {
                String organizerProfileId = organizerProfileIdFromEntrantId(entrant.getProfileId());
                if (event.hasOrganizerAccess(organizerProfileId)) {
                    originalUnavailableProfileIds.add(entrant.getProfileId());
                }
            }
        }

        unavailableProfileIds.addAll(originalUnavailableProfileIds);

        Collections.sort(allProfiles, (first, second) -> {
            String firstName = first.getName() == null ? "" : first.getName().toLowerCase(Locale.getDefault());
            String secondName = second.getName() == null ? "" : second.getName().toLowerCase(Locale.getDefault());
            int nameCompare = firstName.compareTo(secondName);
            if (nameCompare != 0) {
                return nameCompare;
            }

            String firstId = first.getProfileId() == null ? "" : first.getProfileId();
            String secondId = second.getProfileId() == null ? "" : second.getProfileId();
            return firstId.compareTo(secondId);
        });

        filterProfiles(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    private void filterProfiles(@NonNull String query) {
        filteredProfiles.clear();
        String normalizedQuery = query.trim().toLowerCase(Locale.getDefault());
        for (AdminProfileItem item : allProfiles) {
            if (matchesQuery(item, normalizedQuery)) {
                filteredProfiles.add(item);
            }
        }

        adapter.notifyDataSetChanged();
        if (filteredProfiles.isEmpty()) {
            showEmptyState();

        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private boolean matchesQuery(@NonNull AdminProfileItem item, @NonNull String query) {
        if (query.isEmpty()) {
            return true;
        }

        return containsValue(item.getProfileId(), query) || containsValue(item.getName(), query) || containsValue(item.getEmail(), query) || containsValue(item.getPhone(), query);
    }

    private boolean containsValue(@Nullable String value, @NonNull String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    private void applyInvites() {
        ArrayList<String> pendingInviteIds = new ArrayList<>();

        for (String profileId : unavailableProfileIds) {
            if (!originalUnavailableProfileIds.contains(profileId)) {
                pendingInviteIds.add(profileId);
            }
        }

        doneButton.setEnabled(false);
        doneButton.setAlpha(0.5f);

        if (pendingInviteIds.isEmpty()) {
            finishFlow();
            return;
        }

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();

        if (MODE_PRIVATE_ENTRANTS.equals(mode)) {
            organizerDatabaseManager.inviteEntrants(eventId, pendingInviteIds)
                    .addOnSuccessListener(unused -> finishFlow())
                    .addOnFailureListener(e -> {
                        doneButton.setEnabled(true);
                        doneButton.setAlpha(1.0f);
                    });
            return;
        }

        List<Task<Void>> inviteTasks = new ArrayList<>();
        for (String entrantProfileId : pendingInviteIds) {
            inviteTasks.add(organizerDatabaseManager.inviteCoOrganizer(eventId, entrantProfileId));
        }

        Tasks.whenAllComplete(inviteTasks)
                .addOnSuccessListener(results -> {
                    for (Task<?> task : results) {
                        if (!task.isSuccessful()) {
                            doneButton.setEnabled(true);
                            doneButton.setAlpha(1.0f);
                            return;
                        }
                    }
                    finishFlow();
                })
                .addOnFailureListener(e -> {
                    doneButton.setEnabled(true);
                    doneButton.setAlpha(1.0f);
                });
    }

    private void finishFlow() {
        if (!isAdded()) {
            return;
        }

        if (MODE_CO_ORGANIZER.equals(mode) && continueToPrivateEntrants) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.organizer_fragment_container,
                            InviteProfileSelectionFragment.newPrivateInviteInstance(eventId)
                    )
                    .addToBackStack(null)
                    .commit();
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.organizer_fragment_container, new OrganizerYourEventsFragment())
                .commit();
    }

    private String getScreenTitle() {
        if (MODE_CO_ORGANIZER.equals(mode)) {
            return "Invite Co-organizer";
        }
        return "Invite Entrants";
    }

    private String getScreenSubtitle() {
        if (MODE_CO_ORGANIZER.equals(mode)) {
            return "Search by name, Id, email, or phone to invite a co-organizer.";
        }
        return "Search by name, Id, email, or phone to invite entrants.";
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }

    private String safeName(@NonNull Entrant entrant) {
        if (entrant.getName() != null && !entrant.getName().trim().isEmpty()) {
            return entrant.getName();
        }
        return safeText(entrant.getProfileId());
    }

    private String safeText(@Nullable String value) {
        return value == null ? "" : value;
    }

    private String organizerProfileIdFromEntrantId(@NonNull String entrantProfileId) {
        if (entrantProfileId.endsWith("_entrant")) {
            return entrantProfileId.substring(0, entrantProfileId.length() - "_entrant".length()) + "_organizer";
        }
        return entrantProfileId + "_organizer";
    }
}
