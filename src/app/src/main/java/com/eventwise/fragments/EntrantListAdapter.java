package com.eventwise.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;

import java.util.List;

/**
 * Adapter for showing entrant rows in the view entrants page.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class EntrantListAdapter extends RecyclerView.Adapter<EntrantListAdapter.ViewHolder> {

    /**
     * TODO
     * - Replace dummy data with real entrant data later.
     * - Add tests for rows with partial profile info later.
     */

    private final List<EntrantListItem> entrants;

    /**
     * Makes the adapter for entrant list rows.
     *
     * @param entrants entrant rows to display
     */
    public EntrantListAdapter(List<EntrantListItem> entrants) {
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_entrant_list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntrantListItem item = entrants.get(position);

        holder.deviceId.setText(item.getDeviceId());

        if (item.hasName()) {
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(item.getName());
        } else {
            holder.name.setVisibility(View.GONE);
        }

        if (item.hasEmail()) {
            holder.email.setVisibility(View.VISIBLE);
            holder.email.setText(item.getEmail());
        } else {
            holder.email.setVisibility(View.GONE);
        }

        if (item.hasPhone()) {
            holder.phone.setVisibility(View.VISIBLE);
            holder.phone.setText(item.getPhone());
        } else {
            holder.phone.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * Holds one entrant row view.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceId;
        TextView name;
        TextView email;
        TextView phone;

        /**
         * Finds the entrant row views.
         *
         * @param itemView row view
         */
        ViewHolder(@NonNull View itemView){
            super(itemView);
            deviceId = itemView.findViewById(R.id.text_device_id);
            name = itemView.findViewById(R.id.text_name);
            email = itemView.findViewById(R.id.text_email);
            phone = itemView.findViewById(R.id.text_phone);
        }
    }
}
