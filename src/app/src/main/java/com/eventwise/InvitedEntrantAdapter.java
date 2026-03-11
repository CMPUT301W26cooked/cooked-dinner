package com.eventwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvitedEntrantAdapter extends RecyclerView.Adapter<InvitedEntrantAdapter.ViewHolder> {

    private List<Entrant> entrants = new ArrayList<>();
    private OnItemClickListener listener;
    private String eventId;

    public interface OnItemClickListener {
        void onItemClick(Entrant entrant);
    }

    public InvitedEntrantAdapter(String eventId) {
        this.eventId = eventId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invited_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.bind(entrant);
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public void setEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView emailText;
        private TextView invitedDateText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.entrant_name);
            emailText = itemView.findViewById(R.id.entrant_email);
            invitedDateText = itemView.findViewById(R.id.invited_date);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(entrants.get(position));
                }
            });
        }

        void bind(Entrant entrant) {
            nameText.setText(entrant.getName() != null ? entrant.getName() : "Unknown");
            emailText.setText(entrant.getEmail() != null ? entrant.getEmail() : "No email");

            long invitedTimestamp = 0;
            if (entrant.getEventHistory() != null) {
                for (Entrant.EventHistoryEntry entry : entrant.getEventHistory()) {
                    if (entry.getOutcome() == Entrant.EventOutcome.INVITED &&
                            eventId.equals(entry.getEventId())) {
                        invitedTimestamp = entry.getTimestampEpochSec();
                        break;
                    }
                }
            }

            if (invitedTimestamp > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String date = sdf.format(new Date(invitedTimestamp * 1000));
                invitedDateText.setText("Invited: " + date);
            } else {
                invitedDateText.setText("Invited recently");
            }
        }
    }
}