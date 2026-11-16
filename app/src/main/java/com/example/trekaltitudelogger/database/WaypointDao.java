package com.example.trekaltitudelogger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WaypointDao {

    @Insert
    void insertWaypoint(Waypoint waypoint);

    @Query("SELECT * FROM waypoints WHERE trekId = :trekId ORDER BY waypointNumber ASC")
    LiveData<List<Waypoint>> getWaypointsForTrek(String trekId);

    @Query("SELECT * FROM waypoints WHERE trekId = :trekId ORDER BY waypointNumber ASC")
    List<Waypoint> getWaypointsForTrekSync(String trekId);

    @Query("DELETE FROM waypoints WHERE trekId = :trekId")
    void deleteWaypointsForTrek(String trekId);
}