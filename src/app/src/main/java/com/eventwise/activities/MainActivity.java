package com.eventwise.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the MainActivity class for the EventWise app.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
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
