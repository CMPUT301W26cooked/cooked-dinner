package com.eventwise.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;

import java.util.List;

/**
 * Adapter for showing admin organization rows.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminOrganizationsAdapter extends RecyclerView.Adapter<AdminOrganizationsAdapter.ViewHolder> {

    /**
     * TODO
     * - Add tests for delete row behavior later.
     */

    /**
     * delete is pressed on one organization.
     */
    public interface OnDeleteClickListener {
        void onDeleteClicked(AdminOrganizationItem item);
    }

    private final List<AdminOrganizationItem> organizations;
    private final OnDeleteClickListener listener;

    /**
     * Makes the adapter for organization rows.
     *
     * @param organizations organization list
     * @param listener delete callback
     */
    public AdminOrganizationsAdapter(List<AdminOrganizationItem> organizations, OnDeleteClickListener listener) {
        this.organizations = organizations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_admin_organization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminOrganizationItem item = organizations.get(position);

        holder.name.setText(item.getName());
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }

    /**
     * Holds one organization row view.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.organization_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
