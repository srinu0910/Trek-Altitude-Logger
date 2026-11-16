package com.example.trekaltitudelogger.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trekaltitudelogger.R;
import com.example.trekaltitudelogger.activities.TrekActivity;
import com.example.trekaltitudelogger.database.Trek;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrekAdapter extends RecyclerView.Adapter<TrekAdapter.TrekViewHolder> {

    public interface OnTrekClickListener {
        void onTrekClick(Trek trek);
        void onDeleteClick(Trek trek);
    }

    private List<Trek> treks = new ArrayList<>();
    private final OnTrekClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public TrekAdapter(OnTrekClickListener listener) {
        this.listener = listener;
    }

    public void setTreks(List<Trek> treks) {
        this.treks = treks != null ? treks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trek, parent, false);
        return new TrekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrekViewHolder holder, int position) {
        Trek trek = treks.get(position);
        holder.bind(trek, dateFormat, listener);
    }

    @Override
    public int getItemCount() {
        return treks.size();
    }

    public static class TrekViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTrekName;
        private final TextView tvTrekDate;
        private final TextView tvDistance;
        private final TextView tvClimb;
        private final TextView tvWaypointCount;
        private final ImageButton btnDelete;

        public TrekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrekName = itemView.findViewById(R.id.tvTrekName);
            tvTrekDate = itemView.findViewById(R.id.tvTrekDate);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvClimb = itemView.findViewById(R.id.tvClimb);
            tvWaypointCount = itemView.findViewById(R.id.tvWaypointCount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Trek trek, SimpleDateFormat dateFormat, OnTrekClickListener listener) {
            tvTrekName.setText(trek.getName());
            tvTrekDate.setText(dateFormat.format(new Date(trek.getStartTime())));
            tvDistance.setText(TrekActivity.formatDistance(trek.getTotalDistance()));
            tvClimb.setText(String.format(Locale.US, "%.0f m", trek.getTotalClimb()));
            tvWaypointCount.setText(String.format(Locale.US, "%d waypoints", trek.getWaypointCount()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrekClick(trek);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(trek);
                }
            });
        }
    }
}