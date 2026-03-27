package com.eventwise.database;

import com.eventwise.Tag;

import java.util.ArrayList;
import java.util.EnumSet;

// Any field can be null! Be careful!
    public class EventFilter{
        private Long startTimestamp = null;
        private Long endTimestamp = null;

        //Number of free spots (e.g. group of 5 needs 5 spots)
        private Integer eventCapacity = null;

        private final EnumSet<FilterType> filterTypes;

        private ArrayList<String> keywords = null;

        private Tag tag = null;

        public EventFilter(Long startTimestamp, Long endTimestamp, Integer eventCapacity, ArrayList<String> keywords, Tag tag){
            filterTypes = EnumSet.noneOf(FilterType.class);
            if (startTimestamp != null){
                filterTypes.add(FilterType.START_TIMESTAMP);
                this.startTimestamp = startTimestamp;
            }
            if (endTimestamp != null) {
                filterTypes.add(FilterType.END_TIMESTAMP);
                this.endTimestamp = endTimestamp;
            }
            if (eventCapacity != null) {
                filterTypes.add(FilterType.EVENT_CAPACITY);
                this.eventCapacity = eventCapacity;
            }
            if (keywords != null) {
                filterTypes.add(FilterType.KEYWORDS);
                this.keywords = keywords;
            }
            if (tag != null){
                filterTypes.add(FilterType.TAG);
                this.tag = tag;
            }
        }

        public Long getStartTimestamp() {
            return startTimestamp;
        }
        public Long getEndTimestamp() {
            return endTimestamp;
        }
        public Integer getEventCapacity() {
            return eventCapacity;
        }

        public ArrayList<String> getKeywords() {
            return keywords;
        }

        public EnumSet<FilterType> getFilterTypes() {
            return filterTypes;
        }

        public Tag getTag() { return tag; }
    }
