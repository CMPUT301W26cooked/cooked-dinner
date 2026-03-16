package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.R;

/**
 * Admin users page with profiles and organizations tabs.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminUsersFragment extends Fragment {

    /**
     * TODO
     * - Keep this as the tab host for admin users.
     * - Revisit default tab behavior later if organizations first.
     */

    private TextView profilesTab;
    private TextView organizationsTab;

    public AdminUsersFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profilesTab = view.findViewById(R.id.tab_profiles);
        organizationsTab = view.findViewById(R.id.tab_organizations);

        profilesTab.setOnClickListener(v -> showProfilesTab());
        organizationsTab.setOnClickListener(v -> showOrganizationsTab());

        if (savedInstanceState == null) {
            showProfilesTab();
        }
    }

    /**
     * Shows the profiles tab and updates tab colors.
     */
    private void showProfilesTab() {
        profilesTab.setTextColor(getResources().getColor(R.color.weird_piss, null));
        organizationsTab.setTextColor(getResources().getColor(R.color.forest_green, null));

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_users_content_container, new AdminProfilesFragment())
                .commit();
    }

    /**
     * Shows the organizations tab and updates tab colors.
     */
    private void showOrganizationsTab(){
        profilesTab.setTextColor(getResources().getColor(R.color.forest_green, null));
        organizationsTab.setTextColor(getResources().getColor(R.color.weird_piss, null));

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_users_content_container, new AdminOrganizationsFragment())
                .commit();
    }
}
