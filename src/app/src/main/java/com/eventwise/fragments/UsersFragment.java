package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;
import com.eventwise.Profile;
import com.eventwise.database.AdminDatabaseManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private ArrayList<Profile> profileList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfileAdapter(profileList, profileId -> {
            showRemoveDialog(profileId);
        });

        recyclerView.setAdapter(adapter);

        loadProfiles();

        return view;
    }

    private void loadProfiles() {
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .get()
                .addOnSuccessListener(query -> {
                    profileList.clear();
                    for (DocumentSnapshot doc : query) {
                        Profile p = doc.toObject(Profile.class);
                        profileList.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showRemoveDialog(String profileId) {
        new AdminDatabaseManager().removeProfile(profileId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Profile removed", Toast.LENGTH_SHORT).show();
                loadProfiles();
            } else {
                Toast.makeText(getContext(), "Failed to remove profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}