package com.eventwise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the Entrant Events Community Fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-03
 */

public class EntrantEventsCommunityFragment extends Fragment {

    private RecyclerView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;

    public EntrantEventsCommunityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_events_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventListView = view.findViewById(R.id.event_list_view);
        eventListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();

        eventList.add(new Event(
                "SketchyOrganization",
                "Totally Safe Event",
                "A fun workshop that is completely safe",
                10.00,
                "Edmonton",
                "Uhh Candy",
                1772442000L,
                1772445600L,
                1772000000L,
                1772300000L,
                false,
                20,
                10,
                null,
                null
        ));

        eventList.add(new Event(
                "org456",
                "Community Soccer Day",
                "Join us for a beginner friendly soccer event.",
                0.00,
                "St. Albert",
                "Sports",
                1773000000L,
                1773007200L,
                1772500000L,
                1772900000L,
                false,
                30,
                15,
                null,
                null
        ));

        eventAdapter = new EventAdapter(eventList);
        eventListView.setAdapter(eventAdapter);
    }
}