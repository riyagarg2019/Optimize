package com.data.directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Distance {

    @SerializedName("text")
    @Expose
    private String timeString;
    @SerializedName("value")
    @Expose
    private int distanceValue;

    /**
     * No args constructor for use in serialization
     */
    public Distance() {
    }

    /**
     * @param timeString
     * @param distanceValue
     */
    public Distance(String timeString, int distanceValue) {
        super();
        this.timeString = timeString;
        this.distanceValue = distanceValue;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public int getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(int value) {
        this.distanceValue = value;
    }
}
