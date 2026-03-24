package com.eventwise.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eventwise.R;
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

            return false;
        });
    }
}