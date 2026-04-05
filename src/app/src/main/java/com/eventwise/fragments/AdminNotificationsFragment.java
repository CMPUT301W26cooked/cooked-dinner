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

import com.eventwise.Notification;
import com.eventwise.adapters.NotificationAdapter;
import com.eventwise.R;
import com.eventwise.database.NotificationSearcherDataBaseManager;
import com.eventwise.database.SessionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple screen for admin notifications.
 *
 * @author Becca Irving
 * @version 1.0
 * @since 2026-03-09
 */
public class AdminNotificationsFragment extends Fragment {

    private RecyclerView notificationListView;
    private TextView emptyListView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    public AdminNotificationsFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationListView = view.findViewById(R.id.notification_list_view);
        emptyListView = view.findViewById(R.id.empty_list);

        notificationListView.setLayoutManager(new LinearLayoutManager(requireContext()));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, NotificationAdapter.TYPE_ADMIN, this::primaryButton);
        notificationListView.setAdapter(notificationAdapter);
        //Get events from Firebase
        refreshNotifications();
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshNotifications();
    }

    private void primaryButton(Notification notification) {
        Log.d("Notification", "Primary button clicked: " + notification.getMessageTitle());
    }

    private void refreshNotifications() {
        NotificationSearcherDataBaseManager notificationSearcherDBMan = new NotificationSearcherDataBaseManager();

        notificationSearcherDBMan.getNotifications()
                .addOnSuccessListener(returnedList -> {
                    notificationList.clear();
                    if (returnedList != null) {
                        notificationList.addAll(returnedList);
                    }
                    notificationAdapter.notifyDataSetChanged();
                    if (notificationList.isEmpty()) {
                        notificationListView.setVisibility(View.GONE);
                        emptyListView.setVisibility(View.VISIBLE);
                    } else {
                        notificationListView.setVisibility(View.VISIBLE);
                        emptyListView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to refresh Notification", e);
                });
    }
}

