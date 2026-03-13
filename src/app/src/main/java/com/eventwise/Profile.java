package com.eventwise;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
/**
 * The profile class is a basic abstract 
 * 
 * 
 * @author Luke Forster
 * @version 1
 * @since 2026-03-02
 */


public abstract class Profile {
    private String profileID;
    private String name;
    private String email;
    private String phone;
    private boolean notificationsEnabled;
    private ProfileType profileType;
    public Profile(){}

    public Profile(String name, String email, String phone, boolean notificationsEnabled, ProfileType profileType) {
        this.profileID = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notificationsEnabled = notificationsEnabled;
        this.profileType = profileType;
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
    public ProfileType getProfileType() {
        return profileType;
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
    public void updateProfile(String name, String email, String phone) {
        setName(name);
        setEmail(email);
        setPhone(phone);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile that = (Profile) o;
        return Objects.equals(profileID, that.profileID)
                && Objects.equals(name, that.name)
                && Objects.equals(email, that.email)
                && Objects.equals(phone, that.phone)
                && Objects.equals(notificationsEnabled, that.notificationsEnabled)
                && Objects.equals(profileType, that.profileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileID, name, email, phone, notificationsEnabled, profileType);
    }
}


