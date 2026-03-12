package com.eventwise.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStore {
    private final SharedPreferences sp;

    public SessionStore(Context context) {
        this.sp = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public String getToken() {
        return sp.getString("token", null);
    }


    public void setDeviceID(String deviceId) {
        sp.edit().putString("deviceId", deviceId).apply();
    }

    public String getDeviceID() {
        return sp.getString("deviceId", null);
    }



    public String getEntrantId() {
        return sp.getString("entrantId", null);
    }

    public void clear() {
        sp.edit().clear().apply();
    }

}


