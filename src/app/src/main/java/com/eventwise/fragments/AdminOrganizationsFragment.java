package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Organizer;
import com.eventwise.R;
import com.eventwise.database.AdminDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin organizations tab page.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminOrganizationsFragment extends Fragment {

    /**
     * TODO
     * - Revisit empty state text later if needed.
     * - Add tests for loading and deleting organizer profiles.
     */


    private RecyclerView recyclerView;
    private TextView emptyText;
    private final List<AdminOrganizationItem> organizations = new ArrayList<>();
    private AdminOrganizationsAdapter adapter;
    private AdminDatabaseManager adminDatabaseManager;

    public AdminOrganizationsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_organizations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        adminDatabaseManager = new AdminDatabaseManager();

        recyclerView = view.findViewById(R.id.organizations_recycler_view);
        emptyText = view.findViewById(R.id.empty_organizations_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminOrganizationsAdapter(organizations, item -> {
            adminDatabaseManager.removeProfileById(item.getProfileId())
                    .addOnSuccessListener(unused -> {
                        organizations.remove(item);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    })
                    .addOnFailureListener(e ->
                            Log.e("AdminOrganizations", "Failed to delete organizer profile", e));
        });

        recyclerView.setAdapter(adapter);
        loadOrganizations();
    }

    /**
     * Loads organizer profiles from Firestore.
     */
    private void loadOrganizations() {
        adminDatabaseManager.getAllOrganizers()
                .addOnSuccessListener(returnedOrganizers -> {
                    organizations.clear();

                    for (Organizer organizer : returnedOrganizers) {
                        if (organizer == null) {
                            continue;
                        }

                        organizations.add(new AdminOrganizationItem(safeText(organizer.getProfileId()), safeText(organizer.getName())));
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizations", "Failed to load organizer profiles", e);
                    updateEmptyState();
                });
    }

    /**
     * Swaps between the list and empty state.
     */
    private void updateEmptyState() {
        if (organizations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    /**
     * Returns a not null display string.
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
