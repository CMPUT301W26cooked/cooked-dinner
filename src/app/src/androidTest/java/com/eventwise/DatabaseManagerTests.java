package com.eventwise;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;

public class DatabaseManagerTests {

    public static FirebaseFirestore testDb;

    @BeforeClass
    public static void FirebaseSetup() {
        Context context = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(context).stream().noneMatch(app -> app.getName().equals("test"))) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId("cooked-dinner-test")
                    .setApplicationId("1:511463316367:android:6b69ce8f5da3f0c4aac9fa")
                    .setApiKey("AIzaSyDAJGtkpLkM9-zbQT4sOsAX0LMhuD9AZog")
                    .build();
            FirebaseApp.initializeApp(context, options, "test");
        }

        testDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("test"));
    }
}
