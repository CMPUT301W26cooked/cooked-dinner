package com.eventwise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.Notification;
import com.eventwise.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public static final int TYPE_ENTRANT = 0;
    public static final int TYPE_ORGANIZER = 1;
    public static final int TYPE_ADMIN = 2;

    private static final int TYPE_ENTRANT_NOTICE = 3;
    private static final int TYPE_ENTRANT_ACTION_REQUIRED = 4;

    private final List<Notification> notificationList;

    private int mode;

    public interface OnPrimaryButtonClickListener {
        void onPrimaryButtonClick(Notification notification);
    }

    private final OnPrimaryButtonClickListener primaryButtonClickListener;
    private final Set<String> actionableEventIds;

    public NotificationAdapter(List<Notification> notificationList, int mode, OnPrimaryButtonClickListener primaryButtonClickListener) {
        this(notificationList, mode, primaryButtonClickListener, null);
    }

    public NotificationAdapter(List<Notification> notificationList, int mode,
                               OnPrimaryButtonClickListener primaryButtonClickListener,
                               Set<String> actionableEventIds) {
        this.notificationList = notificationList;
        this.mode = mode;
        this.primaryButtonClickListener = primaryButtonClickListener;
        this.actionableEventIds = actionableEventIds != null ? actionableEventIds : new HashSet<>();
    }

    @Override
    public int getItemViewType(int position) {
        Notification notification = notificationList.get(position);

        if (mode == TYPE_ENTRANT) {
            if (notification != null && isActionRequiredNotification(notification)) {
                return TYPE_ENTRANT_ACTION_REQUIRED;
            }
            return TYPE_ENTRANT_NOTICE;
        }

        if (mode == TYPE_ORGANIZER) {
            return TYPE_ORGANIZER;
        }

        if (mode == TYPE_ADMIN) {
            return TYPE_ADMIN;
        }

        return TYPE_ENTRANT_NOTICE;
    }

    private boolean isActionRequiredNotification(@NonNull Notification notification) {
        switch (notification.getType()) {
            case INVITED:
            case CHOSEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isLatestActionRequiredNotificationForEvent(@NonNull Notification notification) {
        String eventId = notification.getEventId();

        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }

        long timestamp = notification.getTimestamp() != null ? notification.getTimestamp() : Long.MIN_VALUE;
        String notificationId = notification.getNotificationId();

        for (Notification other : notificationList) {
            if (other == null || other == notification) {
                continue;
            }

            if (!isActionRequiredNotification(other)) {
                continue;
            }

            if (!eventId.equals(other.getEventId())) {
                continue;
            }

            long otherTimestamp = other.getTimestamp() != null ? other.getTimestamp() : Long.MIN_VALUE;

            if (otherTimestamp > timestamp) {
                return false;
            }

            if (otherTimestamp == timestamp) {
                String otherNotificationId = other.getNotificationId();

                if (notificationId != null
                        && otherNotificationId != null
                        && otherNotificationId.compareTo(notificationId) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView notificationTitle;
        TextView notificationDescription;
        TextView notificationDate;
        TextView notificationType;
        TextView notificationMessage;
        TextView eventName;
        Button primaryButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.notification_description);
            notificationMessage = itemView.findViewById(R.id.notification_message);
            notificationDate = itemView.findViewById(R.id.notification_date);
            notificationType = itemView.findViewById(R.id.notification_type);
            eventName = itemView.findViewById(R.id.event_name);
            primaryButton = itemView.findViewById(R.id.primary_button);
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_ENTRANT_ACTION_REQUIRED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_action_required, parent, false);
        } else if (viewType == TYPE_ENTRANT_NOTICE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_notice, parent, false);
        } else if (viewType == TYPE_ORGANIZER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_organizer, parent, false);
        } else if (viewType == TYPE_ADMIN) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_admin, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_notification_notice, parent, false);
        }

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        if (holder.primaryButton != null) {
            holder.primaryButton.setVisibility(View.GONE);
            holder.primaryButton.setEnabled(false);
            holder.primaryButton.setAlpha(0.5f);
            holder.primaryButton.setOnClickListener(null);
        }

        if (notification == null) {
            if (holder.notificationTitle != null) {
                holder.notificationTitle.setText("Unknown Notification");
            }
            if (holder.notificationMessage != null) {
                holder.notificationMessage.setText("");
            }
            if (holder.notificationDate != null) {
                holder.notificationDate.setText("");
            }
            if (holder.notificationType != null) {
                holder.notificationType.setText("Unknown Error");
            }
            return;
        }

        if (holder.notificationTitle != null) {
            holder.notificationTitle.setText(notification.getMessageTitle());
        }

        if (holder.notificationMessage != null) {
            holder.notificationMessage.setText(notification.getMessageBody());
        }

        if (holder.notificationDate != null) {
            holder.notificationDate.setText(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date(notification.getTimestamp() * 1000L))
            );
        }

        if (holder.eventName != null) {
            holder.eventName.setVisibility(View.GONE);
        }

        switch (notification.getType()) {
            case WAITING_LIST:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Waiting List Entry");
                }
                break;
            case INVITED:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Action Required");
                }
                break;
            case CANCELLED:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Cancelled");
                }
                break;
            case CHOSEN:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Action Required");
                }
                break;
            case NOT_CHOSEN:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Not Selected");
                }
                break;
            case OTHER:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Notification");
                }
                break;
            default:
                if (holder.notificationType != null) {
                    holder.notificationType.setText("Notification");
                }
                break;
        }

        int viewType = getItemViewType(position);

        if (viewType == TYPE_ENTRANT_ACTION_REQUIRED && holder.primaryButton != null) {
            holder.primaryButton.setVisibility(View.VISIBLE);

            boolean enablePrimary = notification.getEventId() != null
                    && actionableEventIds.contains(notification.getEventId())
                    && isLatestActionRequiredNotificationForEvent(notification);
            holder.primaryButton.setEnabled(enablePrimary);
            holder.primaryButton.setAlpha(enablePrimary ? 1.0f : 0.5f);

            holder.primaryButton.setOnClickListener(v -> {
                if (primaryButtonClickListener != null && holder.primaryButton.isEnabled()) {
                    primaryButtonClickListener.onPrimaryButtonClick(notification);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
