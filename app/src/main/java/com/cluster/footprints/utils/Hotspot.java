package com.cluster.footprints.utils;

import java.util.ArrayList;

public class Hotspot {
    private String title;
    private String description;
    private String startDate; // holds the min date for which people could have been infected
    private String endDate; // holds the max date for which people could have been infected
    private ArrayList<HotspotInfo> hotspotInfos;

    public Hotspot() {
    }

    public Hotspot(String title, String description, String startDate, String endDate, ArrayList<HotspotInfo> hotspotInfos) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hotspotInfos = hotspotInfos;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public ArrayList<HotspotInfo> getHotspotInfos() {
        return hotspotInfos;
    }

    public void setHotspotInfos(ArrayList<HotspotInfo> hotspotInfos) {
        this.hotspotInfos = hotspotInfos;
    }
}
