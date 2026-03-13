package com.eventwise.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
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
    private EventSearcherDatabaseManager eventSearcherDBMan;
    private Entrant currentEntrant;


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
        eventSearcherDBMan = new EventSearcherDatabaseManager();
        eventListView = view.findViewById(R.id.list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //This line links device ID to the database stuff... IDRKWID though...
        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        //Theres gotta be a better way to be doing this shit...
        currentEntrant = new Entrant(
                deviceId,
                "Test User",
                "test@email.com",
                true,
                requireContext()
        );

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, EventAdapter.TYPE_CANCEL, this.currentEntrant, this::deleteEvent);
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
                    Log.d("Event", "Event failed to get");
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