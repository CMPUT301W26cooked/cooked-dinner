package com.eventwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {


    public static final int TYPE_ENTRANT = 0;
    public static final int TYPE_ORGANIZER = 1;
    public static final int TYPE_ADMIN = 2;


    private final List<Notification> notificationList;

    private int mode;

    public interface OnPrimaryButtonClickListener {
        void onPrimaryButtonClick(Notification notification);
    }

    private final OnPrimaryButtonClickListener primaryButtonClickListener;


    public NotificationAdapter(List<Notification> notificationList, int mode, OnPrimaryButtonClickListener primaryButtonClickListener) {
        this.notificationList = notificationList;
        this.mode = mode;
        this.primaryButtonClickListener = primaryButtonClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mode;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notificationTitle;
        TextView notificationDescription;
        TextView notificationDate;
        TextView notificationType;
        TextView notificationMessage;

        Button primaryButton; //Dismiss

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.notification_description);
            notificationDate = itemView.findViewById(R.id.notification_date);
            notificationType = itemView.findViewById(R.id.notification_type);

//            primaryButton = itemView.findViewById(R.id.primary_button);

        }
    }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_ENTRANT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_entrant, parent, false);
        } else if (viewType == TYPE_ORGANIZER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_organizer, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_admin, parent, false);
        }
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.notificationTitle.setText(notification.getMessageTitle());
        holder.notificationMessage.setText(notification.getMessageBody());
        //Converts Epoch to String
        holder.notificationDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date(notification.getTimestamp() * 1000)));


        switch (notification.getType()) {
            case WAITING_LIST:
                holder.notificationType.setText("Waiting List Entry");
                break;
            case INVITED:
                holder.notificationType.setText("Invitation");
                break;
            case CANCELLED:
                holder.notificationType.setText("Cancelled");
                break;
            case OTHER:
                holder.notificationType.setText("UnknownError");
        }
    }

    @Override
    public int getItemCount() {return notificationList.size();}

}
