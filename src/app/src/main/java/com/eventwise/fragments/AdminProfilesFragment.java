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

import com.eventwise.Entrant;
import com.eventwise.R;
import com.eventwise.adapters.AdminProfilesAdapter;
import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.items.AdminProfileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin profiles tab page.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-13
 */
public class AdminProfilesFragment extends Fragment {

    /**
     * TODO
     * - Revisit empty state text later if needed.
     * - Add tests for loading and deleting entrant profiles.
     */

    private RecyclerView recyclerView;
    private TextView emptyText;
    private final List<AdminProfileItem> profiles = new ArrayList<>();
    private AdminProfilesAdapter adapter;
    private AdminDatabaseManager adminDatabaseManager;

    public AdminProfilesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminDatabaseManager = new AdminDatabaseManager();

        recyclerView = view.findViewById(R.id.profiles_recycler_view);
        emptyText = view.findViewById(R.id.empty_profiles_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminProfilesAdapter(profiles, item -> {
            adminDatabaseManager.removeProfileById(item.getProfileId())
                    .addOnSuccessListener(unused -> {
                        profiles.remove(item);
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    })
                    .addOnFailureListener(e ->
                            Log.e("AdminProfiles", "Failed to delete entrant profile", e));
        });

        recyclerView.setAdapter(adapter);
        loadProfiles();
    }

    /**
     * Loads entrant profiles from Firestore.
     */
    private void loadProfiles() {
        adminDatabaseManager.getAllEntrants()
                .addOnSuccessListener(returnedEntrants -> {
                    profiles.clear();

                    for (Entrant entrant : returnedEntrants) {
                        if (entrant == null) {
                            continue;
                        }

                        profiles.add(new AdminProfileItem(
                                safeText(entrant.getProfileId()),
                                safeText(entrant.getName()),
                                safeText(entrant.getEmail()),
                                safeText(entrant.getPhone())
                        ));
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfiles", "Failed to load entrant profiles", e);
                    updateEmptyState();
                });
    }

    /**
     * Swaps between the list and empty state.
     */
    private void updateEmptyState() {
        if (profiles.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    /**
     * Returns a non-null display string.
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
