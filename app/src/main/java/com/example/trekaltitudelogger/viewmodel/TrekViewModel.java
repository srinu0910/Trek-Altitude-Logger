package com.example.trekaltitudelogger.viewmodel;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.trekaltitudelogger.database.Trek;
import com.example.trekaltitudelogger.database.Waypoint;
import com.example.trekaltitudelogger.repository.TrekRepository;
import com.example.trekaltitudelogger.services.ElevationService;
import com.example.trekaltitudelogger.services.LocationHelper;

import java.util.List;

public class TrekViewModel extends AndroidViewModel {

    private final TrekRepository repository;
    private final ElevationService elevationService;

    public interface AddWaypointCallback {
        void onSuccess(Waypoint waypoint, Trek updatedTrek);
        void onError(String errorMessage);
    }

    public TrekViewModel(@NonNull Application application) {
        super(application);
        this.repository = new TrekRepository(application);
        this.elevationService = new ElevationService();
    }

    public LiveData<List<Trek>> getAllTreks() {
        return repository.getAllTreks();
    }

    public LiveData<Trek> getTrek(String trekId) {
        return repository.getTrek(trekId);
    }

    public LiveData<List<Waypoint>> getWaypoints(String trekId) {
        return repository.getWaypoints(trekId);
    }

    public void startNewTrek(String name, TrekRepository.OnTrekCreatedListener listener) {
        repository.startNewTrek(name, listener);
    }

    public void addWaypoint(String trekId, Context context, AddWaypointCallback callback) {
        LocationHelper.getCurrentLocation(context, new LocationHelper.LocationCallbackListener() {
            @Override
            public void onLocationRetrieved(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Fetch altitude from Open-Meteo API using ElevationService
                elevationService.fetchElevation(latitude, longitude, new ElevationService.ElevationCallback() {
                    @Override
                    public void onElevationFetched(double elevation) {
                        saveWaypoint(trekId, latitude, longitude, elevation, callback);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Fallback to GPS altitude if API fails/offline
                        double altitude = location.hasAltitude() ? location.getAltitude() : 0.0;
                        saveWaypoint(trekId, latitude, longitude, altitude, callback);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private void saveWaypoint(String trekId, double latitude, double longitude, double altitude, AddWaypointCallback callback) {
        repository.addWaypointToTrek(trekId, latitude, longitude, altitude, new TrekRepository.OnWaypointAddedListener() {
            @Override
            public void onWaypointAdded(Waypoint waypoint, Trek updatedTrek) {
                callback.onSuccess(waypoint, updatedTrek);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void finishTrek(String trekId, Runnable onComplete) {
        repository.finishTrek(trekId, onComplete);
    }

    public void deleteTrek(String trekId, Runnable onComplete) {
        repository.deleteTrek(trekId, onComplete);
    }
}