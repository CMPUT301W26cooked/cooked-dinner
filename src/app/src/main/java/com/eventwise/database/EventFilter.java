package com.eventwise.database;

import com.eventwise.Enum.FilterType;
import com.eventwise.Tag;

import java.util.ArrayList;
import java.util.EnumSet;

//TODO
// Documentation and Tests

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

        public EventFilter(){
            filterTypes = EnumSet.noneOf(FilterType.class);
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

        public void resetFilter(){
            filterTypes.clear();
            startTimestamp = null;
            endTimestamp = null;
            eventCapacity = null;
            tag = null;
            keywords = null;
        }

        public void resetStartTimestamp(){
            filterTypes.remove(FilterType.START_TIMESTAMP);
            startTimestamp = null;
        }
        public void resetEndTimestamp(){
            filterTypes.remove(FilterType.END_TIMESTAMP);
            endTimestamp = null;
        }
        public void resetEventCapacity(){
            filterTypes.remove(FilterType.EVENT_CAPACITY);
            eventCapacity = null;
        }
        public void resetKeywords(){
            filterTypes.remove(FilterType.KEYWORDS);
            keywords = null;
        }
        public void resetTag(){
            filterTypes.remove(FilterType.TAG);
            tag = null;
        }



        public void setStartTimestamp(Long startTimestamp) {
            if (startTimestamp != null){
                this.startTimestamp = startTimestamp;
                filterTypes.add(FilterType.START_TIMESTAMP);
            }
            else{
                resetStartTimestamp();
            }

        }
        public void setEndTimestamp(Long endTimestamp) {
            if (endTimestamp != null){
                this.endTimestamp = endTimestamp;
                filterTypes.add(FilterType.END_TIMESTAMP);
            }
            else {
                resetEndTimestamp();
            }
        }
        public void setEventCapacity(Integer eventCapacity) {
            if (eventCapacity != null){
                this.eventCapacity = eventCapacity;
                filterTypes.add(FilterType.EVENT_CAPACITY);
            }
            else {
                resetEventCapacity();
            }
        }

        public void setKeywords(ArrayList<String> keywords) {
            if (keywords != null){
                this.keywords = keywords;
                filterTypes.add(FilterType.KEYWORDS);
            }
            else {
                resetKeywords();
            }
        }

        public void setTag(Tag tag) {
            if (tag != null){
                this.tag = tag;
                filterTypes.add(FilterType.TAG);
            }
            else {
                resetTag();
            }
        }
    }
