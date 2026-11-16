package com.example.trekaltitudelogger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trekaltitudelogger.R;
import com.example.trekaltitudelogger.database.Waypoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaypointAdapter extends RecyclerView.Adapter<WaypointAdapter.WaypointViewHolder> {

    private List<Waypoint> waypoints = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WaypointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waypoint, parent, false);
        return new WaypointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaypointViewHolder holder, int position) {
        Waypoint waypoint = waypoints.get(position);
        holder.bind(waypoint, dateFormat);
    }

    @Override
    public int getItemCount() {
        return waypoints.size();
    }

    public static class WaypointViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvWaypointNumber;
        private final TextView tvAltitude;
        private final TextView tvCoordinates;
        private final TextView tvTimestamp;
        private final TextView tvElevationChange;

        public WaypointViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWaypointNumber = itemView.findViewById(R.id.tvWaypointNumber);
            tvAltitude = itemView.findViewById(R.id.tvAltitude);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvElevationChange = itemView.findViewById(R.id.tvElevationChange);
        }

        public void bind(Waypoint waypoint, SimpleDateFormat dateFormat) {
            Context context = itemView.getContext();

            tvWaypointNumber.setText(String.format(Locale.US, "%d", waypoint.getWaypointNumber()));
            tvAltitude.setText(String.format(Locale.US, "%.1f m", waypoint.getAltitude()));
            tvCoordinates.setText(String.format(Locale.US, "Lat: %.5f, Lon: %.5f", waypoint.getLatitude(), waypoint.getLongitude()));
            tvTimestamp.setText(dateFormat.format(new Date(waypoint.getTimestamp())));

            double change = waypoint.getElevationChange();
            if (change > 0) {
                tvElevationChange.setText(String.format(Locale.US, "▲ +%.1f m", change));
                tvElevationChange.setTextColor(ContextCompat.getColor(context, R.color.climb_up));
                tvElevationChange.setBackgroundResource(R.drawable.bg_badge_green);
            } else if (change < 0) {
                tvElevationChange.setText(String.format(Locale.US, "▼ %.1f m", change));
                tvElevationChange.setTextColor(ContextCompat.getColor(context, R.color.climb_down));
                tvElevationChange.setBackgroundResource(R.drawable.bg_badge_red);
            } else {
                tvElevationChange.setText("▲ 0.0 m");
                tvElevationChange.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                tvElevationChange.setBackgroundResource(R.drawable.bg_badge_neutral);
            }
        }
    }
}