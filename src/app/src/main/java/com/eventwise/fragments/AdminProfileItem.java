package com.eventwise.fragments;

/**
 * Simple frontend model for one admin profile row.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminProfileItem {

    /**
     * TODO
     * - Add more fields later if the widget changes.
     */

    private final String profileId;
    private final String name;
    private final String email;
    private final String phone;

    /**
     * Makes one simple profile row item.
     *
     * @param profileId profile id
     * @param name profile name
     * @param email profile email
     * @param phone profile phone
     */
    public AdminProfileItem(String profileId, String name, String email, String phone) {
        this.profileId = profileId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * @return profile id
     */
    public String getProfileId() {
        return profileId;

    }

    /**
     * @return profile name
     */
    public String getName() {
        return name;
    }

    /**
     * @return profile email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return profile phone
     */
    public String getPhone() {
        return phone;
    }
}
