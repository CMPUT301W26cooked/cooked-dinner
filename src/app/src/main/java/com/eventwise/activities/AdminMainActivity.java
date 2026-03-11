package com.eventwise.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eventwise.R;
import com.eventwise.fragments.AdminEventsFragment;
import com.eventwise.fragments.OrganizerYourEventsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This is the landing page for the admin profile.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

//TODO: need to switch the list item type to an admin unique option that allows you to delete events

public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_admin);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminEventsFragment())
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.events_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, new AdminEventsFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.notifications_icon) {
                // TODO: replace with NotificationsFragment later
                return true;
            }

            if (item.getItemId() == R.id.image_icon) {
                // TODO: replace with BannersFragment later
                return true;
            }

            if (item.getItemId() == R.id.users_icon) {
                // TODO: replace with UsersFragment later
                return true;
            }

            return false;
        });
    }
}