package com.eventwise.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eventwise.R;
import com.eventwise.fragments.EntrantEventsCommunityFragment;
import com.eventwise.fragments.EntrantProfileEmptyFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;

import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.fragments.EntrantProfileExistsFormFragment;

/**
 * This is the landing page for the entrant profile.
 *
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-09
 * Updated By Becca Irving on 2026-03-13
 */

// TODO (EntrantMainActivity.java)
// - Replace the placeholder notifications page later.
// - Replace the placeholder QR page later.
// - Swap the profile tab to the full profile flow once backend is wired up.

public class EntrantMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_entrant);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.entrant_fragment_container, new EntrantEventsCommunityFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.entrant_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.events_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.entrant_fragment_container, new EntrantEventsCommunityFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.notifications_icon) {
                // TODO: replace with NotificationsFragment later
                return true;
            }

            if (item.getItemId() == R.id.qr_scanner_icon) {
                // TODO: replace with QRScannerFragment later
                return true;
            }

            if (item.getItemId() == R.id.profile_icon) {
                openProfileTab();
                return true;
            }

            return false;
        });
    }

    private void openProfileTab() {
        SessionStore sessionStore = new SessionStore(this);
        String entrantId = sessionStore.getOrCreateDeviceId();

        EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();

        entrantDatabaseManager.getEntrantFromId(entrantId)
                .addOnSuccessListener(entrant -> {
                    Fragment fragmentToShow;

                    if (entrant != null && entrant.hasCompletedProfile()) {
                        fragmentToShow = EntrantProfileExistsFormFragment.newUpdateInstance(
                                entrant.getName(),
                                entrant.getEmail(),
                                entrant.getPhone(),
                                entrant.getNotificationsEnabled()
                        );
                    } else {
                        boolean notificationsEnabled = entrant == null || entrant.getNotificationsEnabled();
                        fragmentToShow = EntrantProfileEmptyFragment.newInstance(notificationsEnabled);
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.entrant_fragment_container, fragmentToShow)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.entrant_fragment_container,
                                    EntrantProfileEmptyFragment.newInstance(true)
                            )
                            .commit();
                });
    }
}
