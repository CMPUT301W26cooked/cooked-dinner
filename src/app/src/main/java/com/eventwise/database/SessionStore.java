package com.eventwise.database;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A utility class responsible for managing persistent local storage of session-related data
 * using Android's {@link SharedPreferences}.
 *
 * <p>This class provides a centralized way to handle device-specific identifiers,
 * authentication tokens, and entrant information required to maintain user sessions
 * across application restarts.</p>
 *
 * @author Hao
 * @version 1.0
 * @since 2026-03-11
 * Updated By Pablo Osorio on 2026-03-12
 */
public class SessionStore {
    private final SharedPreferences sp;

    /**
     * Constructs a new SessionStore and initializes the SharedPreferences using the application context.
     *
     * @param context The context used to access the SharedPreferences.
     */
    public SessionStore(Context context) {
        this.sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }


    /**
     * Retrieves the stored authentication token from the session preferences.
     *
     * @return The stored token string if available; {@code null} otherwise.
     */
    public String getToken() {
        return sp.getString("token", null);
    }


    /**
     * Stores the device-specific identifier in the local shared preferences.
     *
     * @param deviceId The unique identifier string for the device.
     */
    public void setDeviceID(String deviceId) {
        sp.edit().putString("deviceId", deviceId).apply();
    }

    /**
     * Retrieves the stored device ID from the session preferences.
     *
     * @return The stored device ID string if available; {@code null} otherwise.
     */
    public String getDeviceID() {
        return sp.getString("deviceId", null);
    }



    /**
     * Retrieves the stored entrant ID from the session.
     *
     * @return The entrant ID string if it exists; null otherwise.
     */
    public String getEntrantId() {
        return sp.getString("entrantId", null);
    }

    /**
     * Clears all data currently stored in the session preferences.
     */
    public void clear() {
        sp.edit().clear().apply();
    }

}


