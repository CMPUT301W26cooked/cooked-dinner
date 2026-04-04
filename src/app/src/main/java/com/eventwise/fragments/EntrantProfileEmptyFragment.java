package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.Entrant;
import com.eventwise.ProfileDropdownHelper;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;

/**
 * This class is responsible for the entrant empty profile view.
 * It is shown when the entrant has not yet created a profile.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-11
 */
public class EntrantProfileEmptyFragment extends Fragment {

    /**
     * TODO
     * - Add tests for stub entrant creation and notification toggle persistence.
     */

    private static final String ARG_NOTIFICATIONS_ENABLED = "arg_notifications_enabled";

    private Button createProfileButton;
    private View receiveNotificationsLayout;
    private ImageView receiveNotificationsCheckboxImage;
    private boolean receiveNotificationsEnabled = true;

    public EntrantProfileEmptyFragment() {
    }

    public static EntrantProfileEmptyFragment newInstance(boolean receiveNotificationsEnabled) {
        EntrantProfileEmptyFragment fragment = new EntrantProfileEmptyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NOTIFICATIONS_ENABLED, receiveNotificationsEnabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_profile_empty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createProfileButton = view.findViewById(R.id.btn_create_profile);
        receiveNotificationsLayout = view.findViewById(R.id.layout_receive_notifications);
        receiveNotificationsCheckboxImage = view.findViewById(R.id.image_receive_notifications_checkbox);

        /**
         * Switch profile type dropdowm
         */
        LinearLayout profileSwitcher = view.findViewById(R.id.profile_switcher);
        TextView topBarTitle = view.findViewById(R.id.top_bar_title);

        ProfileDropdownHelper.setupDropdown(
                this,
                profileSwitcher,
                topBarTitle,
                "Entrant"
        );

        Bundle args = getArguments();
        if (args != null) {
            receiveNotificationsEnabled = args.getBoolean(ARG_NOTIFICATIONS_ENABLED, true);
        }

        updateNotificationsCheckboxIcon();

        createProfileButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.entrant_fragment_container,
                            EntrantProfileExistsFormFragment.newCreateInstance(receiveNotificationsEnabled)
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
        String entrantId = sessionStore.getOrCreateDeviceId();

        EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();

        entrantDatabaseManager.getEntrantFromId(entrantId)
                .addOnSuccessListener(entrant -> {
                    if (entrant == null) {
                        createStubEntrantWithNotifications(entrantDatabaseManager);
                        return;
                    }

                    entrant.setNotificationsEnabled(receiveNotificationsEnabled);
                    entrantDatabaseManager.updateEntrantInfo(entrant)
                            .addOnFailureListener(e ->
                                    Log.e("EntrantProfileEmpty", "Failed to update notification preference", e));
                })
                .addOnFailureListener(e -> createStubEntrantWithNotifications(entrantDatabaseManager));
    }

    private void createStubEntrantWithNotifications(EntrantDatabaseManager entrantDatabaseManager){
        Entrant entrant = new Entrant("", "", "", receiveNotificationsEnabled, requireContext());
        entrantDatabaseManager.addEntrant(entrant)
                .addOnFailureListener(err ->
                        Log.e("EntrantProfileEmpty", "Failed to create stub entrant", err));
    }
}
