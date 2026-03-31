/**
 * Admin Images tab page.
 *
 * @author Pablo Osorio
 * @version 1.0
 * @since 2026-03-16
 */

//TODO
// Actually delete photos from device/Firebase!
// Javadoc

package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.R;
import com.eventwise.adapters.AdminImagesAdapter;
import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.items.AdminImageItem;

import java.io.File;
import java.util.ArrayList;

public class AdminImagesFragment extends Fragment {

    private RecyclerView recyclerView;

    private TextView emptyText;

    private AdminDatabaseManager adminDatabaseManager;

    private AdminImagesAdapter adapter;

    private ArrayList<AdminImageItem> imageItems = new ArrayList<AdminImageItem>();



    public AdminImagesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyText = view.findViewById(R.id.empty_list);

        adminDatabaseManager = new AdminDatabaseManager();

        recyclerView = view.findViewById(R.id.events_community_list_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminImagesAdapter(imageItems,item ->
        {
            adminDatabaseManager.deletePoster(item.getFilepath(), requireContext())
                    .addOnSuccessListener( notUsed-> {
                        imageItems.remove(item);
                        adapter.notifyDataSetChanged();

                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminImages", "Failed to delete image", e);
                    });


        });
//                adminDatabaseManager.removeEventById(item.getEventId())
//                        .addOnSuccessListener(unused -> {
//                            imageItems.remove(item);
//                            adapter.notifyDataSetChanged();
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e("AdminImages", "Failed to delete image", e);
//                        }));

        recyclerView.setAdapter(adapter);
        loadImages();



    }


    private void loadImages() {
        adminDatabaseManager.getAllEvents()
            .addOnSuccessListener(returnedList -> {
                if (returnedList == null) {
                    return;
                }
                for (Event event : returnedList) {
                    if (event == null) {
                        continue;
                    }

                    if (event.getPosterPath() != null && !event.getPosterPath().isEmpty()) {
                        File file = new File(getContext().getFilesDir(), event.getPosterPath());
                        if (file.exists()) {
                            //Keep track of image file, event ID and the poster filepath
                            imageItems.add(new AdminImageItem(event.getEventId(), event.getPosterPath(), file, event.getName(), "NEED TO ADD ORG NAME",  "NEED TO ADD TIMESTAMP?"));
                        }
                    }

                }
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }).addOnFailureListener(e -> {
                Log.e("AdminImages", "Failed to load images", e);
            });

    }

    private void updateEmptyState() {
        if (imageItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }



}
