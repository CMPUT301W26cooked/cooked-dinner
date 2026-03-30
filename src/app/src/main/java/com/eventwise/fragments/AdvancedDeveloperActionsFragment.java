package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.R;

/**
 * organizer page for advanced developer actions.
 *
 * This page routes each action into the entrant picker flow.
 *
 * @author Becca Irving
 * @since Mar 29 2026
 */
public class AdvancedDeveloperActionsFragment extends Fragment {

    /**
     * TODO (AdvancedDeveloperActionsFragment.java)
     * - Add tests later.
     */

    private static final String ARG_EVENT_ID = "arg_event_id";

    private String eventId = "";

    public AdvancedDeveloperActionsFragment() {
    }

    /**
     * Makes a new advanced developer actions fragment for one event.
     *
     * @param eventId event id
     * @return configured fragment
     */
    public static AdvancedDeveloperActionsFragment newInstance(@NonNull String eventId) {
        AdvancedDeveloperActionsFragment fragment = new AdvancedDeveloperActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_advanced_developer_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID, "");
        }

        View backButton = view.findViewById(R.id.button_back);

        Button waitlistEntrantsButton = view.findViewById(R.id.button_waitlist_entrants);
        Button removeWaitlistEntrantsButton = view.findViewById(R.id.button_remove_waitlist_entrants);
        Button inviteEntrantsButton = view.findViewById(R.id.button_invite_entrants);
        Button removeInviteEntrantsButton = view.findViewById(R.id.button_remove_invite_entrants);
        Button enrollEntrantsButton = view.findViewById(R.id.button_enroll_entrants);
        Button removeEnrollEntrantsButton = view.findViewById(R.id.button_remove_enroll_entrants);

        backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        waitlistEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_WAITLIST)
        );

        removeWaitlistEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_REMOVE_WAITLIST)
        );

        inviteEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_INVITE)
        );

        removeInviteEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_REMOVE_INVITE)
        );

        enrollEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_ENROLL)
        );

        removeEnrollEntrantsButton.setOnClickListener(v ->
                openEntrantPicker(EntrantSelectionFragment.ACTION_REMOVE_ENROLL)
        );
    }

    /**
     * Opens the entrant picker for one ADA action.
     *
     * @param action action name
     */
    private void openEntrantPicker(String action) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.organizer_fragment_container,
                        EntrantSelectionFragment.newInstance(eventId, action)
                )
                .addToBackStack(null)
                .commit();
    }
}
