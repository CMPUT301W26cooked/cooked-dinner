package com.eventwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * adapter for selecting entrants for ADA use mostly.
 *
 * @author Becca Irving
 * @since Mar 29 2026
 */
public class EntrantSelectionAdapter extends RecyclerView.Adapter<EntrantSelectionAdapter.EntrantSelectionViewHolder> {

    private final List<String> entrantIds;
    private final Set<String> selectedEntrantIds = new HashSet<>();
    private OnSelectionChangedListener onSelectionChangedListener;

    public EntrantSelectionAdapter(List<String> entrantIds) {
        this.entrantIds = entrantIds;
    }

    public static class EntrantSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView entrantIdText;

        public EntrantSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_select_entrant);
            entrantIdText = itemView.findViewById(R.id.text_entrant_id);
        }
    }

    @NonNull
    @Override
    public EntrantSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_select_entrant, parent, false);
        return new EntrantSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantSelectionViewHolder holder, int position) {
        String entrantId = entrantIds.get(position);

        holder.entrantIdText.setText(entrantId);

        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(selectedEntrantIds.contains(entrantId));

        holder.itemView.setOnClickListener(v -> {
            boolean shouldSelect = !holder.checkbox.isChecked();
            holder.checkbox.setChecked(shouldSelect);
        });

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedEntrantIds.add(entrantId);
            } else {
                selectedEntrantIds.remove(entrantId);
            }

            if (onSelectionChangedListener != null) {
                onSelectionChangedListener.onSelectionChanged(selectedEntrantIds.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrantIds.size();
    }

    /**
     * Returns the currently selected entrant ids.
     *
     * @return selected ids
     */
    public ArrayList<String> getSelectedEntrantIds() {
        return new ArrayList<>(selectedEntrantIds);
    }

    /**
     * Clears current checkbox selections.
     */
    public void clearSelection() {
        selectedEntrantIds.clear();

        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(0);
        }
    }

    /**
     * Sets current selected entrant ids.
     *
     * @param entrantIds ids to preselect
     */
    public void setSelectedEntrantIds(@NonNull List<String> entrantIds) {
        selectedEntrantIds.clear();
        selectedEntrantIds.addAll(entrantIds);

        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(selectedEntrantIds.size());
        }

        notifyDataSetChanged();
    }

    /**
     * Sets a listener for selection count changes.
     *
     * @param listener listener
     */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChangedListener = listener;
    }

    /**
     * Simple selection callback.
     */
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }
}
