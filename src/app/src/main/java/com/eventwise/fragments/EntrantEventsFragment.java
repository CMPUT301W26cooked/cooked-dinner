package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.R;

/**
 * Entrant Events Tab with Your Events and Events Community
 *
 * @author Luke Forster
 * @version 1.0
 * @since 2026-03-13
 */
public class EntrantEventsFragment extends Fragment {
    private TextView myEventsTab;
    private TextView eventsCommunityTab;

    public EntrantEventsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myEventsTab = view.findViewById(R.id.tab_my_events);
        eventsCommunityTab = view.findViewById(R.id.tab_events_community);

        myEventsTab.setOnClickListener(v -> showMyEventsTab());
        eventsCommunityTab.setOnClickListener(v -> showEventsCommunityTab());

        if (savedInstanceState == null) {
            showEventsCommunityTab();
        }
    }

    /**
     * Shows the My Events tab and updates tab colors.
     */
    private void showMyEventsTab() {
        myEventsTab.setTextColor(getResources().getColor(R.color.weird_piss, null));
        eventsCommunityTab.setTextColor(getResources().getColor(R.color.forest_green, null));

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.entrant_events_content_container, new EntrantMyEventsFragment())
                .commit();
    }

    /**
     * Shows the Events Community tab and updates tab colors.
     */
    private void showEventsCommunityTab(){
        myEventsTab.setTextColor(getResources().getColor(R.color.forest_green, null));
        eventsCommunityTab.setTextColor(getResources().getColor(R.color.weird_piss, null));

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.entrant_events_content_container, new EntrantEventsCommunityFragment())
                .commit();
    }
}