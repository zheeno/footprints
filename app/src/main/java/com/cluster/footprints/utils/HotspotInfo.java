package com.cluster.footprints.utils;

public class HotspotInfo {
    private String title;
    private String description;
    private String date;
    private String time;
    private int threatLevel;
    private boolean isContaminated;
    private UserLocation location;

    public HotspotInfo(){

    }

    public HotspotInfo(String title, String description, String date, String time, int threatLevel, boolean isContaminated, UserLocation location) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.threatLevel = threatLevel;
        this.isContaminated = isContaminated;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = threatLevel;
    }

    public boolean isContaminated() {
        return isContaminated;
    }

    public void setContaminated(boolean contaminated) {
        isContaminated = contaminated;
    }

    public UserLocation getLocation() {
        return location;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }
}
