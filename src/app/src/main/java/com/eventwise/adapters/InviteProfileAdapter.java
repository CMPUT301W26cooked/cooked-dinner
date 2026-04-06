package com.eventwise.adapters;

import android.text.TextUtils;
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
import java.util.Set;

/**
 * Adapter for searchable invite profile rows.
 *
 * @author Becca Irving
 * @since 2026-04-06
 */
public class InviteProfileAdapter extends RecyclerView.Adapter<InviteProfileAdapter.ViewHolder> {

    public interface OnInviteClickListener {
        void onInviteClicked(AdminProfileItem item);
    }

    private final List<AdminProfileItem> profiles;
    private final Set<String> unavailableProfileIds;
    private final OnInviteClickListener listener;

    public InviteProfileAdapter(List<AdminProfileItem> profiles, Set<String> unavailableProfileIds, OnInviteClickListener listener) {
        this.profiles = profiles;
        this.unavailableProfileIds = unavailableProfileIds;
        this.listener = listener;
    }

    /**
     * Updates which profile ids should show as already invited / unavailable.
     *
     * @param disabledProfileIds ids that should disable the invite button
     */
    public void setDisabledProfileIds(@NonNull Set<String> disabledProfileIds) {
        unavailableProfileIds.clear();
        unavailableProfileIds.addAll(disabledProfileIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_invite_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProfileItem item = profiles.get(position);

        holder.name.setText(safeValue(item.getName(), item.getProfileId()));

        if (TextUtils.isEmpty(item.getEmail())) {
            holder.email.setVisibility(View.GONE);
        } else {
            holder.email.setVisibility(View.VISIBLE);
            holder.email.setText(item.getEmail());
        }

        if (TextUtils.isEmpty(item.getPhone())) {
            holder.phone.setVisibility(View.GONE);
        } else {
            holder.phone.setVisibility(View.VISIBLE);
            holder.phone.setText(item.getPhone());
        }

        boolean unavailable = unavailableProfileIds.contains(item.getProfileId());
        holder.inviteButton.setText(unavailable ? "Invited" : "Invite");
        holder.inviteButton.setEnabled(!unavailable);
        holder.inviteButton.setAlpha(unavailable ? 0.5f : 1.0f);

        holder.inviteButton.setOnClickListener(v -> {
            if (!unavailable && listener != null) {
                listener.onInviteClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback == null ? "" : fallback;
        }
        return value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        TextView phone;
        Button inviteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
            phone = itemView.findViewById(R.id.profile_phone);
            inviteButton = itemView.findViewById(R.id.invite_button);
        }
    }
}
