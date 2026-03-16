package com.eventwise.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Profile;
import com.eventwise.R;

import java.util.ArrayList;

/**
 * RecyclerView adapter used to display a list of Profile objects.
 * Each item shows name, email, phone, and has a Remove button.
 *
 * The adapter communicates removal events back to the parent Fragment/Activity
 * through the OnRemoveClickListener interface.
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    /**
     * Callback interface used to notify when "Remove" is clicked.
     */
    public interface OnRemoveClickListener {
        void onRemoveClicked(String profileId);
    }
    private ArrayList<Profile> profiles;
    private OnRemoveClickListener listener;

    /**
     * Constructor for ProfileAdapter
     * @param profiles list of Profile objects to display
     * @param listener callback for remove button
     */
    public ProfileAdapter(ArrayList<Profile> profiles, OnRemoveClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate each profile item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the profile for this position
        Profile profile = profiles.get(position);

        // Bind text fields
        holder.name.setText(profile.getName());
        holder.email.setText(profile.getEmail());
        holder.phone.setText(profile.getPhone());

        // Remove button callback
        holder.removeButton.setOnClickListener(v ->
                listener.onRemoveClicked(profile.getProfileId())
        );
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder for each profile row.
     * Holds references to UI elements in item_profile.xml
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, email, phone;
        Button removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match Ids from item_profile.xml layout
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            phone = itemView.findViewById(R.id.profile_phone);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}
