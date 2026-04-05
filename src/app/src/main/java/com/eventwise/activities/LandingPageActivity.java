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
 * Landing page for EventWise.
 * Lets the user choose which view to enter when the app starts.
 *
 * Author: Becca Irving
 * Date: Apr 3, 2026
 */
public class LandingPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.landing_page_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button entrantButton = findViewById(R.id.entrant_button);
        Button organizerButton = findViewById(R.id.organizer_button);
        Button adminButton = findViewById(R.id.admin_button);

        entrantButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, EntrantMainActivity.class);
            startActivity(intent);
        });

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, OrganizerMainActivity.class);
            startActivity(intent);
        });

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, AdminMainActivity.class);
            startActivity(intent);
        });
    }
}
