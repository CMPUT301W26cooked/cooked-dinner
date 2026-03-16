package com.eventwise.database;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class SessionStore {
    private final SharedPreferences sp;

    public SessionStore(Context context) {
        this.sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public String getToken() {
        return sp.getString("token", null);
    }


    public void setDeviceId(String deviceId) {
        sp.edit().putString("deviceId", deviceId).apply();
    }

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



    public String getEntrantId() {
        return sp.getString("entrantId", null);
    }

    public void clear() {
        sp.edit().clear().apply();
    }

}


