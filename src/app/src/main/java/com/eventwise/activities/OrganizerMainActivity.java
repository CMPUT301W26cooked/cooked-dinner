package com.eventwise.activities;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eventwise.Entrant;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.database.SessionStore;
import com.eventwise.fragments.OrganizerProfileEmptyFragment;
import com.eventwise.fragments.OrganizerProfileExistsFormFragment;
import com.eventwise.fragments.OrganizerNotificationsFragment;
import com.eventwise.fragments.OrganizerYourEventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This is the landing page for the organizer profile.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

public class OrganizerMainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerYourEventsFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.organizer_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.events_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.organizer_fragment_container, new OrganizerYourEventsFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.notifications_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.organizer_fragment_container, new OrganizerNotificationsFragment())
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
        String organizerId = sessionStore.getOrCreateDeviceId();

        EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();

        entrantDatabaseManager.getEntrantFromId(organizerId)
                .addOnSuccessListener(entrant -> {
                    Fragment fragmentToShow;

                    if (entrant != null && entrant.hasCompletedProfile()) {
                        fragmentToShow = OrganizerProfileExistsFormFragment.newUpdateInstance(
                                entrant.getName(),
                                entrant.getEmail(),
                                entrant.getPhone(),
                                entrant.getNotificationsEnabled()
                        );
                    } else {
                        boolean notificationsEnabled = entrant == null || entrant.getNotificationsEnabled();
                        fragmentToShow = OrganizerProfileEmptyFragment.newInstance(notificationsEnabled);
                    }

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.organizer_fragment_container, fragmentToShow)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.organizer_fragment_container,
                                    OrganizerProfileEmptyFragment.newInstance(true)
                            )
                            .commit();
                });
    }

    private void ensureOrganizerExists() {
        SessionStore sessionStore = new SessionStore(this);
        String deviceId = sessionStore.getOrCreateDeviceId();

        if (deviceId == null || deviceId.trim().isEmpty()) {
            Log.e("EntrantMainActivity", "Failed to create device Id");
            return;
        }

        EntrantDatabaseManager db = new EntrantDatabaseManager();

        db.getEntrantFromId(deviceId)
                .addOnSuccessListener(entrant -> {
                    if (entrant != null){
                        Log.d("EntrantMainActivity", "Organizer already exists: " + deviceId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("EntrantMainActivity", "Organizer not found, creating new Organizer: " + deviceId);

                    Entrant newEntrant = new Entrant(
                            "SuperCoolOrganizer",
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

    private void askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}