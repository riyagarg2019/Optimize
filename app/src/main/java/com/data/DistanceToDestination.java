package com.data;

public class DistanceToDestination {
    //represents the distance from an implicit source to this destination

    private Destination stop;
    private float distance;

    public DistanceToDestination(Destination stop, float distance) {
        this.stop = stop;
        this.distance = distance;
    }

    public Destination getStop() {
        return stop;
    }

    public void setStop(Destination stop) {
        this.stop = stop;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "DistanceToDestination{" +
                "stop=" + stop +
                ", distance=" + distance +
                '}';
    }
}
