package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.Entrant;
import com.eventwise.Organizer;
import com.eventwise.R;
import com.eventwise.database.DatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.OrganizerDatabaseManager;
import com.eventwise.database.SessionStore;

/**
 * This class is responsible for the Organizer empty profile view.
 * It is shown when the entrant has not yet created a profile.
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-03
 */
public class OrganizerProfileEmptyFragment extends Fragment {

    private static final String ARG_NOTIFICATIONS_ENABLED = "arg_notifications_enabled";

    private Button createProfileButton;
    private View receiveNotificationsLayout;
    private ImageView receiveNotificationsCheckboxImage;
    private boolean receiveNotificationsEnabled = true;

    public OrganizerProfileEmptyFragment() {
    }

    public static OrganizerProfileEmptyFragment newInstance(boolean receiveNotificationsEnabled) {
        OrganizerProfileEmptyFragment fragment = new OrganizerProfileEmptyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NOTIFICATIONS_ENABLED, receiveNotificationsEnabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_profile_empty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createProfileButton = view.findViewById(R.id.btn_create_profile);
        receiveNotificationsLayout = view.findViewById(R.id.layout_receive_notifications);
        receiveNotificationsCheckboxImage = view.findViewById(R.id.image_receive_notifications_checkbox);

        Bundle args = getArguments();
        if (args != null) {
            receiveNotificationsEnabled = args.getBoolean(ARG_NOTIFICATIONS_ENABLED, true);
        }

        updateNotificationsCheckboxIcon();

        createProfileButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.organizer_fragment_container,
                            OrganizerProfileExistsFormFragment.newCreateInstance(receiveNotificationsEnabled)
                    )
                    .commit();
        });

        receiveNotificationsLayout.setOnClickListener(v -> {
            receiveNotificationsEnabled = !receiveNotificationsEnabled;
            updateNotificationsCheckboxIcon();
            persistNotificationPreference();
        });
    }

    private void updateNotificationsCheckboxIcon() {
        if (receiveNotificationsEnabled) {
            receiveNotificationsCheckboxImage.setImageResource(R.drawable.profile_checkbox_checked);
        } else {
            receiveNotificationsCheckboxImage.setImageResource(R.drawable.profile_checkbox_unchecked);
        }
    }

    private void persistNotificationPreference() {
        SessionStore sessionStore = new SessionStore(requireContext());
        String organizerId = sessionStore.getOrCreateDeviceId();

        OrganizerDatabaseManager organizerDatabaseManager = new OrganizerDatabaseManager();
        organizerDatabaseManager.getOrganizerFromId(organizerId)
                .addOnSuccessListener(organizer -> {
                    if (organizer == null) {
                        createStubEntrantWithNotifications(organizerDatabaseManager);
                        return;
                    }

                    organizer.setNotificationsEnabled(receiveNotificationsEnabled);
                    organizerDatabaseManager.updateOrganizerInfo(organizer)
                            .addOnFailureListener(e ->
                                    Log.e("EntrantProfileEmpty", "Failed to update notification preference", e));
                })
                .addOnFailureListener(e -> createStubEntrantWithNotifications(organizerDatabaseManager));
    }

    private void createStubEntrantWithNotifications(OrganizerDatabaseManager organizerDatabaseManager){
        Organizer organizer = new Organizer("", "", "", receiveNotificationsEnabled, requireContext());
        organizerDatabaseManager.addOrganizer(organizer)
                .addOnFailureListener(err ->
                        Log.e("EntrantProfileEmpty", "Failed to create stub entrant", err));
    }


}

