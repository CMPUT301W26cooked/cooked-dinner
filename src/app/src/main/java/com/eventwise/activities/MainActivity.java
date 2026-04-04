package com.eventwise.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry activity for the app.
 * Immediately forwards to the landing page.
 *
 * Author: Becca Irving
 * Date: Apr 3, 2026
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MainActivity.this, LandingPageActivity.class);
        startActivity(intent);
        finish();
    }
}
