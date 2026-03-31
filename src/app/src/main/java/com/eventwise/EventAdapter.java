package com.eventwise;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * This Event Adapter class takes each event item into a visual widget on screen.
 *
 * @author Luke Forster
 * @version 2.0
 * @since 2026-03-03
 * Updated By Becca Irving on 2026-03-09
 * Updated By Becca Irving on 2026-03-16
 * TODO: The primary secondary  logic in onBindViewHolder is wonky please help.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public static final int TYPE_JOIN = 0;
    public static final int TYPE_CANCEL = 1;
    public static final int TYPE_WAITLISTED = 2;
    public static final int TYPE_EDIT_LEAVE = 3;
    public static final int TYPE_EDIT_CANCEL = 4;
    public static final int TYPE_ACCEPT_DECLINE = 5;
    public static final int TYPE_ENROLLED = 6;

    private final List<Event> eventList;
    private final int mode;
    private final String currentEntrantId;

    public interface OnPrimaryButtonClickListener {
        void onPrimaryButtonClick(Event event);
    }

    public interface OnSecondaryButtonClickListener {
        void onSecondaryButtonClick(Event event);
    }

    public interface OnEventCardClickListener {
        void onEventCardClick(Event event);
    }

    private final OnPrimaryButtonClickListener primaryButtonClickListener;

    private final OnSecondaryButtonClickListener secondaryButtonClickListener;
    private final OnEventCardClickListener eventCardClickListener;

    /**
     * Makes an event adapter without a card click callback.
     *
     * @param eventList events to display
     * @param mode widget mode to use
     * @param primaryButtonClickListener primary button callback
     */
    public EventAdapter(
            List<Event> eventList,
            int mode,
            OnPrimaryButtonClickListener primaryButtonClickListener
    ) {
        this(eventList, mode, null, primaryButtonClickListener, null, null);
    }

    public EventAdapter(
            List<Event> eventList,
            int mode,
            String currentEntrantId,
            OnPrimaryButtonClickListener primaryButtonClickListener
    ) {
        this(eventList, mode, currentEntrantId, primaryButtonClickListener, null, null);
    }

    public EventAdapter(
            List<Event> eventList,
            int mode,
            OnPrimaryButtonClickListener primaryButtonClickListener,
            OnEventCardClickListener eventCardClickListener
    ) {
        this(eventList, mode, null, primaryButtonClickListener, null, eventCardClickListener);
    }

    public EventAdapter(
            List<Event> eventList,
            int mode,
            String currentEntrantId,
            OnPrimaryButtonClickListener primaryButtonClickListener,
            OnEventCardClickListener eventCardClickListener
    ) {
        this(eventList, mode, currentEntrantId, primaryButtonClickListener, null, eventCardClickListener);
    }

    public EventAdapter(
            List<Event> eventList,
            int mode,
            OnPrimaryButtonClickListener primaryButtonClickListener,
            OnSecondaryButtonClickListener secondaryButtonClickListener,
            OnEventCardClickListener eventCardClickListener
    ) {
        this(eventList, mode, null, primaryButtonClickListener, secondaryButtonClickListener, eventCardClickListener);
    }

    public EventAdapter(
            List<Event> eventList,
            int mode,
            String currentEntrantId,
            OnPrimaryButtonClickListener primaryButtonClickListener,
            OnSecondaryButtonClickListener secondaryButtonClickListener,
            OnEventCardClickListener eventCardClickListener
    ) {
        this.eventList = eventList;
        this.mode = mode;
        this.currentEntrantId = currentEntrantId;
        this.primaryButtonClickListener = primaryButtonClickListener;
        this.secondaryButtonClickListener = secondaryButtonClickListener;
        this.eventCardClickListener = eventCardClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        Event event = eventList.get(position);

        if (mode == TYPE_EDIT_CANCEL) {
            return TYPE_EDIT_CANCEL;
        }

        if (mode == TYPE_CANCEL) {
            return TYPE_CANCEL;
        }

        if (mode == TYPE_EDIT_LEAVE) {
            if (currentEntrantId != null) {
                if (event.getEntrantIdsByStatus(EventEntrantStatus.INVITED).contains(currentEntrantId)) {
                    return TYPE_ACCEPT_DECLINE;
                }
                if (event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(currentEntrantId)) {
                    return TYPE_WAITLISTED;
                }
                if (event.getEntrantIdsByStatus(EventEntrantStatus.ENROLLED).contains(currentEntrantId)) {
                    return TYPE_ENROLLED;
                }
            }
        }

        if (currentEntrantId != null
                && event.getEntrantIdsByStatus(EventEntrantStatus.WAITLISTED).contains(currentEntrantId)) {
            return TYPE_WAITLISTED;
        }

        return TYPE_JOIN;
    }

    /**
     * Holds one event widget row.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;
        TextView eventDescription;
        TextView eventLocationText;
        TextView eventStartDateText;
        TextView eventEndDateText;
        TextView eventOrganization;

        TextView eventSpotsCount;
        TextView eventWaitlistedCount;
        TextView eventRegisteredCount;
        ImageView eventStatusIcon;
        Button primaryButton;
        Button secondaryButton;

        ImageView posterView;

        /**
         * Finds the event widget views.
         *
         * @param itemView row view
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.event_name);
            eventDescription = itemView.findViewById(R.id.event_description);

            eventLocationText = itemView.findViewById(R.id.event_location_text);

            eventStartDateText = itemView.findViewById(R.id.event_start_date_text);
            eventEndDateText = itemView.findViewById(R.id.event_end_date_text);

            eventOrganization = itemView.findViewById(R.id.event_organization);

            eventSpotsCount = itemView.findViewById(R.id.event_spots_count);
            eventWaitlistedCount = itemView.findViewById(R.id.event_waitlisted_count);
            eventRegisteredCount = itemView.findViewById(R.id.event_event_registered_count);

            eventStatusIcon = itemView.findViewById(R.id.event_status_icon);

            primaryButton = itemView.findViewById(R.id.primary_button);
            secondaryButton = itemView.findViewById(R.id.secondary_button);

            posterView = itemView.findViewById(R.id.event_poster);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
         * Update: I changed the event adapter to support the different types of widgets we will
         * need to inflate in different views.
         */
        View view;
        if(viewType == TYPE_EDIT_LEAVE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_edit_leave_event, parent, false);
        } else if(viewType == TYPE_EDIT_CANCEL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_edit_cancel_event, parent, false);
        } else if(viewType == TYPE_ACCEPT_DECLINE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_accept_decline, parent, false);
        } else if(viewType == TYPE_WAITLISTED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_waitlisted_event, parent, false);
        } else if(viewType == TYPE_CANCEL) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_cancel_event, parent, false);
        } else if(viewType == TYPE_ENROLLED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_enrolled_event, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_join_event, parent, false);
        }
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {

        Event event = eventList.get(position);
        int viewType = getItemViewType(position);

        holder.eventName.setText(event.getName());
        holder.eventDescription.setText(event.getDescription());

        holder.eventLocationText.setText(event.getLocationName());

        holder.eventStartDateText.setText(formatEpoch(event.getEventStartEpochSec()));
        holder.eventEndDateText.setText(formatEpoch(event.getEventEndEpochSec()));

        holder.eventOrganization.setText("Organization Name");

        if (holder.eventStatusIcon != null) {
            holder.eventStatusIcon.setImageResource(getEventStatusDrawable(event));
        }

        int spots = event.getMaxWinnersToSample();
        int waitlisted = event.getWaitingListCount();
        int registered = event.getEnrolledCount();

        holder.eventSpotsCount.setText("•  " + spots + " spots");
        holder.eventWaitlistedCount.setText("•  " + waitlisted + " waitlisted");
        holder.eventRegisteredCount.setText("•  " + registered + " registered");

        if (event.getPosterPath() != null && !event.getPosterPath().isEmpty()) {
            File file = new File(holder.itemView.getContext().getFilesDir(), event.getPosterPath());
            if (file.exists()) {
                holder.posterView.setImageURI(Uri.fromFile(file));
            }
            else{
                holder.posterView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        else{
            holder.posterView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        TextView registeredBadge = holder.itemView.findViewById(R.id.registered);
        TextView waitlistedBadge = holder.itemView.findViewById(R.id.waitlisted_text);

        if (registeredBadge != null) {
            registeredBadge.setVisibility(View.GONE);
        }

        if (waitlistedBadge != null) {
            waitlistedBadge.setVisibility(View.GONE);
        }

        if (viewType == TYPE_ACCEPT_DECLINE && registeredBadge != null) {
            registeredBadge.setText("Action Required");
            registeredBadge.setTextColor(holder.itemView.getResources().getColor(R.color.red, null));
            registeredBadge.setVisibility(View.VISIBLE);
        } else if (viewType == TYPE_WAITLISTED) {
            if (waitlistedBadge != null) {
                waitlistedBadge.setVisibility(View.VISIBLE);
            } else if (registeredBadge != null) {
                registeredBadge.setText("Waitlisted");
                registeredBadge.setTextColor(holder.itemView.getResources().getColor(R.color.moss_green, null));
                registeredBadge.setVisibility(View.VISIBLE);
            }
        } else if (viewType == TYPE_ENROLLED && registeredBadge != null) {
            registeredBadge.setText("Registered");
            registeredBadge.setTextColor(holder.itemView.getResources().getColor(R.color.moss_green, null));
            registeredBadge.setVisibility(View.VISIBLE);
        }

        boolean eventStarted = hasEventStarted(event);
        boolean registrationClosed = isRegistrationClosed(event);

        if (holder.primaryButton != null) {
            boolean enablePrimary = true;

            if (eventStarted) {
                enablePrimary = false;
            } else if (viewType == TYPE_JOIN) {
                enablePrimary = event.isRegistrationOpenNow() && !event.isWaitingListFull();
            } else if (viewType == TYPE_ACCEPT_DECLINE) {
                enablePrimary = !registrationClosed;
            } else {
                enablePrimary = true;
            }

            holder.primaryButton.setEnabled(enablePrimary);
            holder.primaryButton.setAlpha(enablePrimary ? 1.0f : 0.5f);

            holder.primaryButton.setOnClickListener(v -> {
                if (primaryButtonClickListener != null && holder.primaryButton.isEnabled()) {
                    primaryButtonClickListener.onPrimaryButtonClick(event);
                }
            });
        }

        if (holder.secondaryButton != null) {
            boolean enableSecondary = !eventStarted;
            holder.secondaryButton.setEnabled(enableSecondary);
            holder.secondaryButton.setAlpha(enableSecondary ? 1.0f : 0.5f);

            holder.secondaryButton.setOnClickListener(v -> {
                if (secondaryButtonClickListener != null && holder.secondaryButton.isEnabled()) {
                    secondaryButtonClickListener.onSecondaryButtonClick(event);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (eventCardClickListener != null) {
                eventCardClickListener.onEventCardClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


    private boolean hasEventStarted(Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec >= event.getEventStartEpochSec();
    }

    private boolean isRegistrationClosed(Event event) {
        long nowEpochSec = System.currentTimeMillis() / 1000L;
        return nowEpochSec > event.getRegistrationCloseEpochSec();
    }

    private boolean shouldDisablePrimaryButton(Event event) {
        return hasEventStarted(event) || isRegistrationClosed(event);
    }

    private int getEventStatusDrawable(Event event) {
        if (hasEventStarted(event)) {
            return R.drawable.event_over;
        }
        if (isRegistrationClosed(event)) {
            return R.drawable.event_closed;
        }
        return R.drawable.event_open;
    }

    /**
     * Formats an event epoch time for display.
     *
     * @param epochSeconds epoch seconds
     * @return formatted date and time
     */
    private String formatEpoch(long epochSeconds) {
        Date date = new Date(epochSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
