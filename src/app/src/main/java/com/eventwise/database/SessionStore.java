package com.eventwise.database;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

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
    public void setDeviceId(String deviceId) {
        sp.edit().putString("deviceId", deviceId).apply();
    }

    /**
     * Retrieves the stored device Id from the session preferences.
     *
     * @return The stored device Id string if available; {@code null} otherwise.
     */
    public String getDeviceId() {
        return sp.getString("deviceId", null);
    }

    public String getOrCreateDeviceId() {
        String deviceId = getDeviceId();

        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = UUID.randomUUID().toString();
            setDeviceId(deviceId);
        }

        return deviceId;
    }



    /**
     * Retrieves the stored entrant Id from the session.
     *
     * @return The entrant Id string if it exists; null otherwise.
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


