package com.example.trekaltitudelogger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trekaltitudelogger.R;
import com.example.trekaltitudelogger.adapter.WaypointAdapter;
import com.example.trekaltitudelogger.database.Trek;
import com.example.trekaltitudelogger.viewmodel.TrekViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private TextView tvSummaryTrekName;
    private TextView tvSummaryTrekDate;
    private TextView tvSummaryDistance;
    private TextView tvSummaryClimb;
    private TextView tvSummaryHighest;
    private TextView tvSummaryLowest;
    private TextView tvSummaryWaypointsCount;

    private WaypointAdapter adapter;
    private Trek currentTrek;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        String trekId = getIntent().getStringExtra(TrekActivity.EXTRA_TREK_ID);

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvSummaryTrekName = findViewById(R.id.tvSummaryTrekName);
        tvSummaryTrekDate = findViewById(R.id.tvSummaryTrekDate);
        tvSummaryDistance = findViewById(R.id.tvSummaryDistance);
        tvSummaryClimb = findViewById(R.id.tvSummaryClimb);
        tvSummaryHighest = findViewById(R.id.tvSummaryHighest);
        tvSummaryLowest = findViewById(R.id.tvSummaryLowest);
        tvSummaryWaypointsCount = findViewById(R.id.tvSummaryWaypointsCount);

        MaterialButton btnShareSummary = findViewById(R.id.btnShareSummary);
        RecyclerView rvSummaryWaypoints = findViewById(R.id.rvSummaryWaypoints);

        btnBack.setOnClickListener(v -> finish());

        rvSummaryWaypoints.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WaypointAdapter();
        rvSummaryWaypoints.setAdapter(adapter);

        TrekViewModel viewModel = new ViewModelProvider(this).get(TrekViewModel.class);

        if (trekId != null) {
            viewModel.getTrek(trekId).observe(this, trek -> {
                if (trek != null) {
                    currentTrek = trek;
                    tvSummaryTrekName.setText(trek.getName());
                    tvSummaryTrekDate.setText(dateFormat.format(new Date(trek.getStartTime())));
                    tvSummaryDistance.setText(TrekActivity.formatDistance(trek.getTotalDistance()));
                    tvSummaryClimb.setText(String.format(Locale.US, "%.0f m", trek.getTotalClimb()));
                    tvSummaryHighest.setText(String.format(Locale.US, "%.1f m", trek.getHighestAltitude()));
                    tvSummaryLowest.setText(String.format(Locale.US, "%.1f m", trek.getLowestAltitude()));
                    tvSummaryWaypointsCount.setText(String.format(Locale.US, "%d", trek.getWaypointCount()));
                }
            });

            viewModel.getWaypoints(trekId).observe(this, waypoints -> adapter.setWaypoints(waypoints));
        }

        btnShareSummary.setOnClickListener(v -> shareTrekSummary());
    }

    private void shareTrekSummary() {
        if (currentTrek == null) {
            Toast.makeText(this, "Summary data not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder();
        shareText.append("⛰️ Trek Summary: ").append(currentTrek.getName()).append("\n");
        shareText.append("📅 Date: ").append(dateFormat.format(new Date(currentTrek.getStartTime()))).append("\n");
        shareText.append("📏 Total Distance: ").append(TrekActivity.formatDistance(currentTrek.getTotalDistance())).append("\n");
        shareText.append("📈 Total Elevation Gain: ").append(String.format(Locale.US, "%.0f m", currentTrek.getTotalClimb())).append("\n");
        shareText.append("🏔️ Highest Altitude: ").append(String.format(Locale.US, "%.1f m", currentTrek.getHighestAltitude())).append("\n");
        shareText.append("📉 Lowest Altitude: ").append(String.format(Locale.US, "%.1f m", currentTrek.getLowestAltitude())).append("\n");
        shareText.append("📍 Waypoints Recorded: ").append(currentTrek.getWaypointCount()).append("\n\n");
        shareText.append("Shared via Trek Altitude Logger 🥾");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Trek Summary");
        startActivity(shareIntent);
    }
}