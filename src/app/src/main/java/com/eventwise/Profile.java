package com.eventwise;

import java.util.ArrayList;
import java.util.UUID;

public class Profile {
    private String name;
    private String email;
    private String phone;
    private final String profileID;
    private boolean notificationsEnabled;
    private final ArrayList<EventHistoryEntry> eventHistory;

    public Profile(){
        this.profileID = UUID.randomUUID().toString();
        this.notificationsEnabled = true;
        this.eventHistory = new ArrayList<>();
    }


    //Getters and setters for the fields
    public String getProfileID() {
        return profileID;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }
    public ArrayList<EventHistoryEntry> getEventHistory() {
        return eventHistory;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }


    //General Methods
    public void toggleNotifications() {
        this.notificationsEnabled = !this.notificationsEnabled;
    }
    public void addEntry(EventHistoryEntry entry) {
        this.eventHistory.add(entry);
    }
    public void updateProfile(String name, String email, String phone) {
        setName(name);
        setEmail(email);
        setPhone(phone);
    }
}


