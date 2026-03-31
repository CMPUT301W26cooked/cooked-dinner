package com.eventwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;
import com.eventwise.items.AdminProfileItem;

import java.util.List;

/**
 * Adapter for showing admin profile rows.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminProfilesAdapter extends RecyclerView.Adapter<AdminProfilesAdapter.ViewHolder> {

    /**
     * TODO
     * - Add tests for delete row behavior later.
     */

    /**
     * Callback for when delete is pressed on one profile.
     */
    public interface OnDeleteClickListener {
        void onDeleteClicked(AdminProfileItem item);
    }

    private final List<AdminProfileItem> profiles;
    private final OnDeleteClickListener listener;

    /**
     * Makes the adapter for profile rows.
     *
     * @param profiles profile list
     * @param listener delete callback
     */
    public AdminProfilesAdapter(List<AdminProfileItem> profiles, OnDeleteClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_admin_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProfileItem item = profiles.get(position);
        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.phone.setText(item.getPhone());
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


    /**
     * Holds one profile row view.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        TextView phone;
        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            phone = itemView.findViewById(R.id.profile_phone);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

    }
}
