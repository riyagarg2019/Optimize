package com.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Destination implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long destinationId;

    public Destination(String location, Double lat, Double lng) {
        this.location = location;
        this.lat = lat;
        this.lng = lng;
    }

    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "lat")
    private Double lat;
    @ColumnInfo(name = "lng")
    private Double lng;

    public long getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(long destinationId) {
        this.destinationId = destinationId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Destination{" +
                "destinationId=" + destinationId +
                ", location='" + location + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
