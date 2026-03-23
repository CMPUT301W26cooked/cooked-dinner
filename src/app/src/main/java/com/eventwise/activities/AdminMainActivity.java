package com.eventwise.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eventwise.R;
import com.eventwise.fragments.AdminEventsFragment;
import com.eventwise.fragments.AdminImagesFragment;
import com.eventwise.fragments.AdminUsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.eventwise.fragments.AdminNotificationsFragment;

/**
 * This is the landing page for the admin profile.
 *
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-09
 * Updated By Becca Irving on 2026-03-13
 */

// TODO:
// - Replace the placeholder image page when that view is ready.
// - Revisit admin event item handling later if a more admin-specific setup is needed.

public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

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
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, new AdminNotificationsFragment())
                        .commit();
                    return true;
            }

            if (item.getItemId() == R.id.image_icon) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.admin_fragment_container, new AdminImagesFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.users_icon) {
                // TODO: replace with UsersFragment later
                if (item.getItemId() == R.id.users_icon) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.admin_fragment_container, new AdminUsersFragment())
                            .commit();
                    return true;
                }
            }

            return false;
        });
    }
}
