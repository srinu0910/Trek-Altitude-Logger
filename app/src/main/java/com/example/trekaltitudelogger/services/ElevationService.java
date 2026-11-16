package com.example.trekaltitudelogger.services;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ElevationService {

    private final OkHttpClient client;
    private final Handler mainHandler;

    public interface ElevationCallback {
        void onElevationFetched(double elevation);
        void onFailure(String errorMessage);
    }

    public ElevationService() {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void fetchElevation(double latitude, double longitude, ElevationCallback callback) {
        String url = String.format(Locale.US, "https://api.open-meteo.com/v1/elevation?latitude=%.6f&longitude=%.6f", latitude, longitude);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onFailure("Network error fetching elevation: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onFailure("Server returned error: " + response.code()));
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseData);
                    double elevation = 0.0;
                    if (json.has("elevation")) {
                        Object obj = json.get("elevation");
                        if (obj instanceof JSONArray) {
                            JSONArray arr = (JSONArray) obj;
                            if (arr.length() > 0) {
                                elevation = arr.getDouble(0);
                            }
                        } else if (obj instanceof Number) {
                            elevation = ((Number) obj).doubleValue();
                        }
                    }

                    final double finalElevation = elevation;
                    mainHandler.post(() -> callback.onElevationFetched(finalElevation));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onFailure("Error parsing elevation response: " + e.getMessage()));
                }
            }
        });
    }
}