package com.example.trekaltitudelogger.repository;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.trekaltitudelogger.database.AppDatabase;
import com.example.trekaltitudelogger.database.Trek;
import com.example.trekaltitudelogger.database.TrekDao;
import com.example.trekaltitudelogger.database.Waypoint;
import com.example.trekaltitudelogger.database.WaypointDao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrekRepository {

    private final TrekDao trekDao;
    private final WaypointDao waypointDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnTrekCreatedListener {
        void onTrekCreated(Trek trek);
    }

    public interface OnWaypointAddedListener {
        void onWaypointAdded(Waypoint waypoint, Trek updatedTrek);
        void onError(String message);
    }

    public TrekRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.trekDao = db.trekDao();
        this.waypointDao = db.waypointDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<Trek>> getAllTreks() {
        return trekDao.getAllTreks();
    }

    public LiveData<Trek> getTrek(String trekId) {
        return trekDao.getTrekById(trekId);
    }

    public LiveData<List<Waypoint>> getWaypoints(String trekId) {
        return waypointDao.getWaypointsForTrek(trekId);
    }

    public void startNewTrek(String name, OnTrekCreatedListener listener) {
        executorService.execute(() -> {
            String trekId = UUID.randomUUID().toString();
            long startTime = System.currentTimeMillis();
            Trek newTrek = new Trek(trekId, name, startTime);
            trekDao.insertTrek(newTrek);

            mainHandler.post(() -> listener.onTrekCreated(newTrek));
        });
    }

    public void addWaypointToTrek(String trekId, double latitude, double longitude, double altitude, OnWaypointAddedListener listener) {
        executorService.execute(() -> {
            Trek trek = trekDao.getTrekByIdSync(trekId);
            if (trek == null) {
                mainHandler.post(() -> listener.onError("Trek not found."));
                return;
            }

            List<Waypoint> existingWaypoints = waypointDao.getWaypointsForTrekSync(trekId);
            if (existingWaypoints == null) {
                existingWaypoints = new ArrayList<>();
            }

            int nextNumber = existingWaypoints.size() + 1;
            double elevationChange = 0.0;

            if (!existingWaypoints.isEmpty()) {
                Waypoint lastWaypoint = existingWaypoints.get(existingWaypoints.size() - 1);
                elevationChange = altitude - lastWaypoint.getAltitude();
            }

            Waypoint newWaypoint = new Waypoint(
                    trekId,
                    nextNumber,
                    latitude,
                    longitude,
                    altitude,
                    elevationChange,
                    System.currentTimeMillis()
            );

            waypointDao.insertWaypoint(newWaypoint);

            // Fetch all waypoints including the newly inserted one to recalculate exact metrics
            List<Waypoint> updatedWaypoints = waypointDao.getWaypointsForTrekSync(trekId);
            recalculateTrekMetrics(trek, updatedWaypoints);
            trekDao.updateTrek(trek);

            mainHandler.post(() -> listener.onWaypointAdded(newWaypoint, trek));
        });
    }

    public void recalculateTrekMetrics(Trek trek, List<Waypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            trek.setTotalDistance(0.0);
            trek.setTotalClimb(0.0);
            trek.setHighestAltitude(0.0);
            trek.setLowestAltitude(0.0);
            trek.setWaypointCount(0);
            return;
        }

        double totalDistanceKm = 0.0;
        double totalClimbMeters = 0.0;
        double highestAlt = waypoints.get(0).getAltitude();
        double lowestAlt = waypoints.get(0).getAltitude();

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint current = waypoints.get(i);
            double currentAlt = current.getAltitude();

            if (currentAlt > highestAlt) {
                highestAlt = currentAlt;
            }
            if (currentAlt < lowestAlt) {
                lowestAlt = currentAlt;
            }

            if (i > 0) {
                Waypoint prev = waypoints.get(i - 1);

                // Calculate distance in km
                float[] results = new float[1];
                Location.distanceBetween(prev.getLatitude(), prev.getLongitude(),
                        current.getLatitude(), current.getLongitude(), results);
                totalDistanceKm += (results[0] / 1000.0);

                // Calculate positive elevation gain
                double climbDiff = currentAlt - prev.getAltitude();
                if (climbDiff > 0) {
                    totalClimbMeters += climbDiff;
                }
            }
        }

        trek.setTotalDistance(totalDistanceKm);
        trek.setTotalClimb(totalClimbMeters);
        trek.setHighestAltitude(highestAlt);
        trek.setLowestAltitude(lowestAlt);
        trek.setWaypointCount(waypoints.size());
    }

    public void finishTrek(String trekId, Runnable onComplete) {
        executorService.execute(() -> {
            Trek trek = trekDao.getTrekByIdSync(trekId);
            if (trek != null) {
                List<Waypoint> waypoints = waypointDao.getWaypointsForTrekSync(trekId);
                recalculateTrekMetrics(trek, waypoints);
                trek.setFinished(true);
                trekDao.updateTrek(trek);
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deleteTrek(String trekId, Runnable onComplete) {
        executorService.execute(() -> {
            waypointDao.deleteWaypointsForTrek(trekId);
            trekDao.deleteTrekById(trekId);
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }
}