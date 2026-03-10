package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Event;
import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the admin Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

public class AdminEventsFragment extends Fragment {

    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    public AdminEventsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventListView = view.findViewById(R.id.event_list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        //Get events from Firebase
        EventSearcherDatabaseManager eventSearcherDBMan = new EventSearcherDatabaseManager();

        eventSearcherDBMan.getEvents()
                .addOnSuccessListener(returnedList ->{
                    for (int i = 0; i < returnedList.size(); i++) {
                        Log.d("Event", returnedList.get(i).getName());
                        eventList.add(returnedList.get(i));
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(param-> {
                    Log.d("Event", "Event failed to get");
                });

        eventAdapter = new EventAdapter(eventList);
        eventListView.setAdapter(eventAdapter);
    }
}