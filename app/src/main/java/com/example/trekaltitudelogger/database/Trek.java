package com.example.trekaltitudelogger.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "treks")
public class Trek {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String trekId;
    private String name;
    private long startTime;
    private double totalDistance; // in km
    private double totalClimb; // in meters
    private double highestAltitude; // in meters
    private double lowestAltitude; // in meters
    private int waypointCount;
    private boolean isFinished;

    public Trek(String trekId, String name, long startTime) {
        this.trekId = trekId;
        this.name = name;
        this.startTime = startTime;
        this.totalDistance = 0.0;
        this.totalClimb = 0.0;
        this.highestAltitude = 0.0;
        this.lowestAltitude = 0.0;
        this.waypointCount = 0;
        this.isFinished = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrekId() {
        return trekId;
    }

    public void setTrekId(String trekId) {
        this.trekId = trekId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalClimb() {
        return totalClimb;
    }

    public void setTotalClimb(double totalClimb) {
        this.totalClimb = totalClimb;
    }

    public double getHighestAltitude() {
        return highestAltitude;
    }

    public void setHighestAltitude(double highestAltitude) {
        this.highestAltitude = highestAltitude;
    }

    public double getLowestAltitude() {
        return lowestAltitude;
    }

    public void setLowestAltitude(double lowestAltitude) {
        this.lowestAltitude = lowestAltitude;
    }

    public int getWaypointCount() {
        return waypointCount;
    }

    public void setWaypointCount(int waypointCount) {
        this.waypointCount = waypointCount;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}