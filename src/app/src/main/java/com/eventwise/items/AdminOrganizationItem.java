package com.eventwise.items;

/**
 * Simple frontend model for one admin organization row.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminOrganizationItem {

    /**
     * TODO
     * - Add more fields later if needed (probably?)
     */

    private final String profileId;
    private final String name;

    /**
     * Makes one simple organization item.
     *
     * @param profileId organization profile id
     * @param name organization name
     */
    public AdminOrganizationItem(String profileId, String name) {
        this.profileId = profileId;
        this.name = name;
    }

    /**
     * @return organization profile id
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * @return organization name
     */
    public String getName() {
        return name;
    }
}
