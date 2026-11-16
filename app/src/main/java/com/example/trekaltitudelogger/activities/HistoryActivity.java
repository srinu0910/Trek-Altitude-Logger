package com.example.trekaltitudelogger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trekaltitudelogger.R;
import com.example.trekaltitudelogger.adapter.TrekAdapter;
import com.example.trekaltitudelogger.database.Trek;
import com.example.trekaltitudelogger.viewmodel.TrekViewModel;

public class HistoryActivity extends AppCompatActivity implements TrekAdapter.OnTrekClickListener {

    private TrekViewModel viewModel;
    private TrekAdapter adapter;
    private TextView tvEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);

        btnBack.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrekAdapter(this);
        rvHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TrekViewModel.class);
        viewModel.getAllTreks().observe(this, treks -> {
            adapter.setTreks(treks);
            if (treks != null && !treks.isEmpty()) {
                tvEmptyHistory.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
            } else {
                tvEmptyHistory.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTrekClick(Trek trek) {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra(TrekActivity.EXTRA_TREK_ID, trek.getTrekId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Trek trek) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_trek)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteTrek(trek.getTrekId(), null))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}