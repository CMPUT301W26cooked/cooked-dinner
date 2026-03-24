package com.eventwise.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eventwise.R;
import com.eventwise.fragments.EntrantEventsCommunityFragment;
import com.eventwise.fragments.EntrantEventsFragment;
import com.eventwise.fragments.EntrantNotificationsFragment;
import com.eventwise.fragments.EntrantProfileEmptyFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;

import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.fragments.EntrantProfileExistsFormFragment;
import android.util.Log;

import com.eventwise.Entrant;

/**
 * This is the landing page for the entrant profile.
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-09
 * Updated By Becca Irving on 2026-03-13
 * Updated By Becca Irving on 2026-03-16
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
        setContentView(R.layout.activity_entrant);

        ensureEntrantExists();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.entrant_fragment_container, new EntrantEventsFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.entrant_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.events_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.entrant_fragment_container, new EntrantEventsFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.notifications_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.entrant_fragment_container, new EntrantNotificationsFragment())
                        .commit();
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

    private void ensureEntrantExists() {
        SessionStore sessionStore = new SessionStore(this);
        String deviceId = sessionStore.getOrCreateDeviceId();

        if (deviceId == null || deviceId.trim().isEmpty()) {
            Log.e("EntrantMainActivity", "Failed to create device Id");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        db.getEntrantFromId(deviceId)
                .addOnSuccessListener(entrant -> {
                    Log.d("EntrantMainActivity", "Entrant already exists: " + deviceId);
                })
                .addOnFailureListener(e -> {
                    Log.d("EntrantMainActivity", "Entrant not found, creating new entrant: " + deviceId);

                    Entrant newEntrant = new Entrant(
                            "New Entrant",
                            "",
                            "",
                            true,
                            this
                    );

                    db.addEntrant(newEntrant)
                            .addOnSuccessListener(unused ->
                                    Log.d("EntrantMainActivity", "Entrant created successfully"))
                            .addOnFailureListener(createError ->
                                    Log.e("EntrantMainActivity", "Failed to create entrant", createError));
                });
    }
}
