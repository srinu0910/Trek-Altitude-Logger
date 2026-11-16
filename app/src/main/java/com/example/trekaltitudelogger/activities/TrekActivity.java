package com.example.trekaltitudelogger.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trekaltitudelogger.R;
import com.example.trekaltitudelogger.adapter.WaypointAdapter;
import com.example.trekaltitudelogger.database.Trek;
import com.example.trekaltitudelogger.database.Waypoint;
import com.example.trekaltitudelogger.viewmodel.TrekViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class TrekActivity extends AppCompatActivity {

    public static final String EXTRA_TREK_ID = "extra_trek_id";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TrekViewModel viewModel;

    private LinearLayout layoutStartSection;
    private LinearLayout layoutActiveTrek;
    private EditText etTrekName;
    private TextView tvToolbarTitle;

    private TextView tvLiveDistance;
    private TextView tvLiveClimb;
    private TextView tvLiveHighest;
    private TextView tvLiveLowest;
    private TextView tvLiveAltitude;
    private TextView tvLiveWaypointCount;
    private TextView tvEmptyWaypoints;
    private ProgressBar progressBar;

    private WaypointAdapter adapter;
    private String currentTrekId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trek);

        viewModel = new ViewModelProvider(this).get(TrekViewModel.class);

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);

        layoutStartSection = findViewById(R.id.layoutStartSection);
        layoutActiveTrek = findViewById(R.id.layoutActiveTrek);
        etTrekName = findViewById(R.id.etTrekName);

        MaterialButton btnStartTrekConfirm = findViewById(R.id.btnStartTrekConfirm);
        MaterialButton btnAddWaypoint = findViewById(R.id.btnAddWaypoint);
        MaterialButton btnFinishTrek = findViewById(R.id.btnFinishTrek);

        tvLiveDistance = findViewById(R.id.tvLiveDistance);
        tvLiveClimb = findViewById(R.id.tvLiveClimb);
        tvLiveHighest = findViewById(R.id.tvLiveHighest);
        tvLiveLowest = findViewById(R.id.tvLiveLowest);
        tvLiveAltitude = findViewById(R.id.tvLiveAltitude);
        tvLiveWaypointCount = findViewById(R.id.tvLiveWaypointCount);
        tvEmptyWaypoints = findViewById(R.id.tvEmptyWaypoints);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView rvWaypoints = findViewById(R.id.rvWaypoints);
        rvWaypoints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaypointAdapter();
        rvWaypoints.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnStartTrekConfirm.setOnClickListener(v -> {
            String name = etTrekName.getText().toString().trim();
            if (name.isEmpty()) {
                etTrekName.setError("Please enter trek name");
                return;
            }
            startTrek(name);
        });

        btnAddWaypoint.setOnClickListener(v -> checkPermissionAndAddWaypoint());

        btnFinishTrek.setOnClickListener(v -> finishTrekAndShowSummary());
    }

    private void startTrek(String name) {
        viewModel.startNewTrek(name, trek -> {
            currentTrekId = trek.getTrekId();
            tvToolbarTitle.setText(trek.getName());
            layoutStartSection.setVisibility(View.GONE);
            layoutActiveTrek.setVisibility(View.VISIBLE);

            observeTrekData();
        });
    }

    private void observeTrekData() {
        if (currentTrekId == null) return;

        viewModel.getTrek(currentTrekId).observe(this, trek -> {
            if (trek != null) {
                tvLiveDistance.setText(formatDistance(trek.getTotalDistance()));
                tvLiveClimb.setText(String.format(Locale.US, "%.0f m", trek.getTotalClimb()));
                if (trek.getWaypointCount() > 0) {
                    tvLiveHighest.setText(String.format(Locale.US, "%.1f m", trek.getHighestAltitude()));
                    tvLiveLowest.setText(String.format(Locale.US, "%.1f m", trek.getLowestAltitude()));
                } else {
                    tvLiveHighest.setText("-- m");
                    tvLiveLowest.setText("-- m");
                }
                tvLiveWaypointCount.setText(String.format(Locale.US, "%d", trek.getWaypointCount()));
            }
        });

        viewModel.getWaypoints(currentTrekId).observe(this, waypoints -> {
            adapter.setWaypoints(waypoints);
            if (waypoints != null && !waypoints.isEmpty()) {
                Waypoint last = waypoints.get(waypoints.size() - 1);
                tvLiveAltitude.setText(String.format(Locale.US, "%.1f m", last.getAltitude()));

                if (waypoints.size() == 1) {
                    tvEmptyWaypoints.setVisibility(View.VISIBLE);
                    tvEmptyWaypoints.setText("📍 1 waypoint recorded. Walk to a new location and tap 'Add Waypoint' to calculate distance & elevation gain!");
                } else {
                    tvEmptyWaypoints.setVisibility(View.GONE);
                }
            } else {
                tvEmptyWaypoints.setVisibility(View.VISIBLE);
                tvEmptyWaypoints.setText(R.string.no_waypoints);
                tvLiveAltitude.setText("-- m");
            }
        });
    }

    public static String formatDistance(double distanceKm) {
        if (distanceKm <= 0) {
            return "0.00 km";
        } else if (distanceKm < 1.0) {
            double meters = distanceKm * 1000.0;
            return String.format(Locale.US, "%.2f km (%.0f m)", distanceKm, meters);
        } else {
            return String.format(Locale.US, "%.2f km", distanceKm);
        }
    }

    private void checkPermissionAndAddWaypoint() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            addWaypoint();
        }
    }

    private void addWaypoint() {
        if (currentTrekId == null) {
            Toast.makeText(this, "Please start a trek first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        viewModel.addWaypoint(currentTrekId, this, new TrekViewModel.AddWaypointCallback() {
            @Override
            public void onSuccess(Waypoint waypoint, Trek updatedTrek) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TrekActivity.this, "Waypoint #" + waypoint.getWaypointNumber() + " added!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TrekActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishTrekAndShowSummary() {
        if (currentTrekId == null) return;

        viewModel.finishTrek(currentTrekId, () -> {
            Intent intent = new Intent(TrekActivity.this, SummaryActivity.class);
            intent.putExtra(EXTRA_TREK_ID, currentTrekId);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addWaypoint();
            } else {
                Toast.makeText(this, "Location permission required to record waypoints", Toast.LENGTH_SHORT).show();
            }
        }
    }
}