package com.eventwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onViewInvitedClick(Event event);
        void onManageClick(Event event);
    }

    public OrganizerEventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventDescription;
        TextView eventLocationText;
        TextView eventStartDateText;
        TextView eventEndDateText;
        TextView eventSpotsCount;
        TextView eventWaitlistedCount;
        TextView eventRegisteredCount;
        Button viewInvitedButton;
        Button manageButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDescription = itemView.findViewById(R.id.event_description);
            eventLocationText = itemView.findViewById(R.id.event_location_text);
            eventStartDateText = itemView.findViewById(R.id.event_start_date_text);
            eventEndDateText = itemView.findViewById(R.id.event_end_date_text);
            eventSpotsCount = itemView.findViewById(R.id.event_spots_count);
            eventWaitlistedCount = itemView.findViewById(R.id.event_waitlisted_count);
            eventRegisteredCount = itemView.findViewById(R.id.event_registered_count);
            viewInvitedButton = itemView.findViewById(R.id.view_invited_button);
            manageButton = itemView.findViewById(R.id.manage_button);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_organizer_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventName.setText(event.getName());
        holder.eventDescription.setText(event.getDescription());
        holder.eventLocationText.setText(event.getLocationName());
        holder.eventStartDateText.setText(formatEpoch(event.getEventStartEpochSec()));
        holder.eventEndDateText.setText(formatEpoch(event.getEventEndEpochSec()));

        int spots = event.getMaxWinnersToSample();
        int waitlisted = event.getWaitingListCount();
        int registered = event.getConfirmedEntrantIds() == null ? 0 : event.getConfirmedEntrantIds().size();

        holder.eventSpotsCount.setText("•  " + spots + " spots");
        holder.eventWaitlistedCount.setText("•  " + waitlisted + " waitlisted");
        holder.eventRegisteredCount.setText("•  " + registered + " registered");

        holder.viewInvitedButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewInvitedClick(event);
            }
        });

        holder.manageButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onManageClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    private String formatEpoch(long epochSeconds) {
        Date date = new Date(epochSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}