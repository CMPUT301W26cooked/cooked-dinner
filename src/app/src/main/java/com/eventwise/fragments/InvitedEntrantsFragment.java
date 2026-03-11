package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Entrant;
import com.eventwise.Event;
import com.eventwise.EventEntrantStatus;
import com.eventwise.InvitedEntrantAdapter;
import com.eventwise.R;
import com.eventwise.database.OrganizerDatabaseManager;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InvitedEntrantsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";

    private String eventId;
    private String eventName;

    private RecyclerView recyclerView;
    private InvitedEntrantAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private LinearLayout runLotteryButton;

    private OrganizerDatabaseManager dbManager;
    private FirebaseFirestore firestore;
    private com.google.firebase.firestore.ListenerRegistration eventListener;

    public static InvitedEntrantsFragment newInstance(String eventId, String eventName) {
        InvitedEntrantsFragment fragment = new InvitedEntrantsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }

        dbManager = new OrganizerDatabaseManager();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invited_entrants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.invited_entrants_recycler);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        runLotteryButton = view.findViewById(R.id.run_lottery_button);

        TextView headerTitle = view.findViewById(R.id.header_title);
        headerTitle.setText(eventName != null ? eventName + " - Invited" : "Invited Entrants");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvitedEntrantAdapter(eventId);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(entrant -> {
            Toast.makeText(getContext(), "Selected: " + entrant.getName(), Toast.LENGTH_SHORT).show();
        });

        runLotteryButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            Toast.makeText(getContext(), "Navigate to lottery selection", Toast.LENGTH_SHORT).show();
        });

        loadInvitedEntrants();
        setupRealTimeListener();
    }

    private void setupRealTimeListener() {
        eventListener = firestore.collection("events").document(eventId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) return;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        loadInvitedEntrants();
                    }
                });
    }

    private void loadInvitedEntrants() {
        showLoading(true);

        dbManager.getEntrantsIDsInChosenList(eventId)
                .addOnSuccessListener(chosenIds -> {
                    if (chosenIds == null || chosenIds.isEmpty()) {
                        showEmptyState(true);
                        showLoading(false);
                        return;
                    }

                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (String entrantId : chosenIds) {
                        tasks.add(firestore.collection("profiles").document(entrantId).get());
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(documentSnapshots -> {
                        List<Entrant> invitedEntrants = new ArrayList<>();

                        for (Object result : documentSnapshots) {
                            if (result instanceof DocumentSnapshot) {
                                DocumentSnapshot doc = (DocumentSnapshot) result;
                                Entrant entrant = doc.toObject(Entrant.class);

                                if (entrant != null && !hasAcceptedOrDeclined(entrant, eventId)) {
                                    invitedEntrants.add(entrant);
                                }
                            }
                        }

                        if (invitedEntrants.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                            adapter.setEntrants(invitedEntrants);
                        }
                        showLoading(false);
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean hasAcceptedOrDeclined(Entrant entrant, String eventId) {
        if (entrant.getEventStates() == null) return false;

        for (Entrant.EventStateEntry entry : entrant.getEventStates()) {
            if (eventId.equals(entry.getEventId())) {
                EventEntrantStatus status = entry.getStatus();
                return status == EventEntrantStatus.ENROLLED ||
                        status == EventEntrantStatus.CANCELLED;
            }
        }
        return false;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventListener != null) {
            eventListener.remove();
        }
    }
}