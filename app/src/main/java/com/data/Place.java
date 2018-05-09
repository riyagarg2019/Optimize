package com.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long placeId;

    public Place(String location, Double lat, Double lng, String description) {
        this.location = location;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
    }

    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "lat")
    private Double lat;
    @ColumnInfo(name = "lng")
    private Double lng;
    @ColumnInfo(name = "description")
    private String description;

    public long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(long placeId) {
        this.placeId = placeId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
