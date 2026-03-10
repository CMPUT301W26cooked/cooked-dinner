package com.eventwise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eventwise.R;

/**
 * This is the MainActivity class for the EventWise app. It acts as a starting point for all profile
 * types and will likely eventually become the login page for the app.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 */

// TODO Implement organizer landing page and admin landing page

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button entrantButton = findViewById(R.id.entrant_button);
        Button organizerButton = findViewById(R.id.organizer_button);
        Button adminButton = findViewById(R.id.admin_button);

        entrantButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EntrantMainActivity.class);
            startActivity(intent);
        });

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrganizerMainActivity.class);
            startActivity(intent);
        });

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
            startActivity(intent);
        });
    }
}