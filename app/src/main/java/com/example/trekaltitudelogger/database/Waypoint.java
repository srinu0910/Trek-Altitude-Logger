package com.example.trekaltitudelogger.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "waypoints")
public class Waypoint {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String trekId;
    private int waypointNumber;
    private double latitude;
    private double longitude;
    private double altitude; // in meters
    private double elevationChange; // difference in meters from previous waypoint
    private long timestamp;

    public Waypoint(String trekId, int waypointNumber, double latitude, double longitude, double altitude, double elevationChange, long timestamp) {
        this.trekId = trekId;
        this.waypointNumber = waypointNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.elevationChange = elevationChange;
        this.timestamp = timestamp;
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

    public int getWaypointNumber() {
        return waypointNumber;
    }

    public void setWaypointNumber(int waypointNumber) {
        this.waypointNumber = waypointNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getElevationChange() {
        return elevationChange;
    }

    public void setElevationChange(double elevationChange) {
        this.elevationChange = elevationChange;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}