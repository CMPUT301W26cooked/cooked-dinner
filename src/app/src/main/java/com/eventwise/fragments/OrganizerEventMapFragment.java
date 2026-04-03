package com.eventwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.Event;
import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class is responsible for the organizer map fragment.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-01
 */
public class OrganizerEventMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "OrganizerEventMap";

    private GoogleMap googleMap;
    private Event event;

    public OrganizerEventMapFragment() {
    }

    public static OrganizerEventMapFragment newInstance(Event event) {
        OrganizerEventMapFragment fragment = new OrganizerEventMapFragment();
        fragment.event = event;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated called");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            Log.d(TAG, "SupportMapFragment found");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "SupportMapFragment is null");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        Log.d(TAG, "onMapReady called");
        googleMap = map;

        LatLng firstLatLng = null;
        int markerCount = 0;

        if (event == null) {
            Log.e(TAG, "event is null");
            addFallbackMarker();
            return;
        }

        if (event.getEntrantStatuses() == null) {
            Log.e(TAG, "entrantStatuses is null");
            addFallbackMarker();
            return;
        }

        Log.d(TAG, "entrantStatuses size = " + event.getEntrantStatuses().size());


        for (Event.EntrantStatusEntry entry : event.getEntrantStatuses()) {
            EntrantDatabaseManager entrantDB = new EntrantDatabaseManager();

            entrantDB.getEntrantFromId(entry.getEntrantProfileId())
                    .addOnSuccessListener(entrant -> {
                        String name = entrant.getName();
                    });
            if (entry == null) {
                continue;
            }

            if (entry.getJoinLocation() == null) {
                Log.d(TAG, "No joinLocation for entrant: " + entry.getEntrantProfileId());
                continue;
            }

            double lat = entry.getJoinLocation().getLatitude();
            double lng = entry.getJoinLocation().getLongitude();

            LatLng latLng = new LatLng(lat, lng);

            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(entry.getEntrantProfileId()));

            EntrantDatabaseManager entrantDatabaseManager = new EntrantDatabaseManager();
            String entrantId = entry.getEntrantProfileId();

            entrantDatabaseManager.getEntrantFromId(entrantId)
                    .addOnSuccessListener(entrant -> {
                        String markerTitle = entrant.getName();

                        if (markerTitle == null || markerTitle.trim().isEmpty()) {
                            markerTitle = entrantId;
                        }

                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(markerTitle));

                        Log.d(TAG, "Added marker for entrant: " + markerTitle
                                + " lat=" + lat + " lng=" + lng);
                    })
                    .addOnFailureListener(e -> {
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(entrantId));

                        Log.d(TAG, "Added marker with fallback ID: " + entrantId
                                + " lat=" + lat + " lng=" + lng);
                    });

            if (firstLatLng == null) {
                firstLatLng = latLng;
            }

            markerCount++;
        }

        if (firstLatLng != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 10f));
            Log.d(TAG, "Moved camera to first marker");
        } else {
            Log.d(TAG, "No entrant markers found, adding fallback marker");
            addFallbackMarker();
        }

        Log.d(TAG, "Total markers added: " + markerCount);
    }

    private void addFallbackMarker() {
        if (googleMap == null) {
            return;
        }

        LatLng fallback = new LatLng(53.5461, -113.4938);

        googleMap.addMarker(new MarkerOptions()
                .position(fallback)
                .title("Test marker"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fallback, 10f));
    }
}