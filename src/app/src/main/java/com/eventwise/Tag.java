package com.eventwise;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a category keyword tag for an event that can be used for filtering / searching.
 * @author Becca Irving
 * @since 2026-03-09
 */
public class Tag {

    /**
     * TODO (Tag.java)
     * - Decide later whether tags should become their own Firestore collection.
     * - Add validation to prevent duplicate tags on one event if needed.
     * - Add organizer UI for choosing tags from pre-populated defaults.
     * - Add unit tests.
     */

    /** Category that this tag belongs to. */
    private String category;

    /** Keyword text for this tag. */
    private String keyword;

    /**
     * Required for Firestore.
     */
    public Tag() {}

    /**
     * Creates a tag.
     *
     * @param category tag category
     * @param keyword tag keyword
     */
    public Tag(String category, String keyword) {
        this.category = category;
        this.keyword = keyword;
    }

    /** @return tag category */
    public String getCategory() {
        return category;
    }

    /** @param category tag category */
    public void setCategory(String category) {
        this.category = category;
    }

    /** @return tag keyword */
    public String getKeyword() {
        return keyword;
    }

    /** @param keyword tag keyword */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns pre-populated default tags.
     *
     * @return default tags
     */
    public static ArrayList<Tag> getDefaultTags() {
        return new ArrayList<>(Arrays.asList(
                new Tag("Academic", "Engineering"),
                new Tag("Academic", "Study Group"),
                new Tag("Academic", "Exam Prep"),
                new Tag("Academic", "Workshop"),

                new Tag("Technology", "Programming"),
                new Tag("Technology", "AI"),
                new Tag("Technology", "Hackathon"),
                new Tag("Technology", "Robotics"),

                new Tag("Social", "Networking"),
                new Tag("Social", "Club"),
                new Tag("Social", "Meetup"),
                new Tag("Social", "Community"),

                new Tag("Sports", "Soccer"),
                new Tag("Sports", "Basketball"),
                new Tag("Sports", "Running"),
                new Tag("Sports", "Fitness"),

                new Tag("Arts", "Music"),
                new Tag("Arts", "Photography"),
                new Tag("Arts", "Painting"),
                new Tag("Arts", "Design")
        ));
    }
}
