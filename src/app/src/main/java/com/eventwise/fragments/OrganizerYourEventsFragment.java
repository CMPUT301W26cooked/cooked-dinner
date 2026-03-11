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

import com.eventwise.EventAdapter;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import com.eventwise.Event;

/**
 * This class is responsible for the admin Organizer Your Events Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-09
 */

public class OrganizerYourEventsFragment extends Fragment {
    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventSearcherDatabaseManager eventSearcherDBMan;

    public OrganizerYourEventsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_organizer_your_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        View createEventButton = view.findViewById(R.id.create_new_event_button);

        //New event button
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new CreateEventFragment())
                    .addToBackStack(null)
                    .commit();
        });
        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList = new ArrayList<>();

        //UPDATE: I changed the event adapter to fetch the right widget for its screen.
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_EDIT_CANCEL, this::deleteEvent);
        eventListView.setAdapter(eventAdapter);

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
                    Log.d("Event", "Event failed to get...");
                });
    }

    public void deleteEvent(Event event){
        eventSearcherDBMan.deleteEvent(event)
            .addOnSuccessListener(unused -> {
                eventList.remove(event);
                eventAdapter.notifyDataSetChanged();
                Log.d("Event", "Event deleted successfully...");
            })
            .addOnFailureListener(e -> {
                Log.d("Event", "Event delete failed...");
            });
    }
}