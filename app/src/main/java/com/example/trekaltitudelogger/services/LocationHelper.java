package com.example.trekaltitudelogger.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    public interface LocationCallbackListener {
        void onLocationRetrieved(@NonNull Location location);
        void onError(String errorMessage);
    }

    public static void getCurrentLocation(Context context, LocationCallbackListener listener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.onError("Location permissions not granted.");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        listener.onLocationRetrieved(location);
                    } else {
                        // Fallback to getLastLocation if getCurrentLocation returned null
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(lastLocation -> {
                                    if (lastLocation != null) {
                                        listener.onLocationRetrieved(lastLocation);
                                    } else {
                                        listener.onError("Unable to retrieve location. Please check if GPS is enabled.");
                                    }
                                })
                                .addOnFailureListener(e -> listener.onError("Failed to get location: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> listener.onError("Failed to get current location: " + e.getMessage()));
    }
}