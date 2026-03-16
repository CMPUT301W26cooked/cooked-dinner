package com.eventwise.fragments;

/**
 * Simple frontend model for one entrant row in the entrants list.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class EntrantListItem {

    /**
     * TODO (EntrantListItem.java)
     * - Replace this with a real entrant model later if need
     * - Add more fields later if the entrant widget grows.
     */

    private final String deviceId;
    private final String name;
    private final String email;
    private final String phone;

    /**
     * Makes one entrant list row item.
     *
     * @param deviceId entrant device id
     * @param name entrant name if available
     * @param email entrant email if available
     * @param phone entrant phone if available
     */
    public EntrantListItem(String deviceId, String name, String email, String phone) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }


    /**
     * @return device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return entrant name
     */
    public String getName() {
        return name;
    }

    /**
     * @return entrant email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return entrant phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return true if name exists
     */
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * @return true if email exists
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * @return true if phone exists
     */
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }
}
