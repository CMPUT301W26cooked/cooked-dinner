package com.eventwise;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.internal.InternalTokenProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


//Todo
//Documentation and Tests

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private static final long END_OF_DAY_OFFSET = 86399000L; // (24*60*60 - 1) * 1000

    public interface FilterListener {
        void onFiltersApplied(Long startDateTimestamp, Long endDateTimestamp, Integer minSpots);
    }

    private FilterListener listener;
    private Date startDate = null;
    private Date endDate = null;

    private TextView startDateLabel;
    private TextView endDateLabel;
    private TextView spotsValueLabel;

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        startDateLabel = view.findViewById(R.id.start_date_label);
        endDateLabel = view.findViewById(R.id.end_date_label);
        spotsValueLabel = view.findViewById(R.id.spots_value_label);

        // Start date picker
        view.findViewById(R.id.start_date_picker).setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select start date")
                    .setTheme(R.style.CustomDatePicker)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                //Adjust for Timezone difference
                TimeZone utc = TimeZone.getTimeZone("UTC");
                long offset = utc.getOffset(selection);
                startDate = new Date(selection + offset);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                startDateLabel.setText(sdf.format(startDate));
            });
            picker.show(getParentFragmentManager(), "start_date");

            view.post(() -> {
                if (picker.getDialog() != null && picker.getDialog().getWindow() != null) {
                    picker.getDialog().getWindow().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.lighter_green))
                    );
                }
            });
        });

        // End date picker
        view.findViewById(R.id.end_date_picker).setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select end date")
                    .setTheme(R.style.CustomDatePicker)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                //Adjust for Timezone difference
                TimeZone utc = TimeZone.getTimeZone("UTC");
                long offset = utc.getOffset(selection);
                //Add End of day offset to get last possible second of tbe ending day
                endDate = new Date(selection + offset + END_OF_DAY_OFFSET);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                endDateLabel.setText(sdf.format(endDate));
            });
            picker.show(getParentFragmentManager(), "end_date");
            view.post(() -> {
                if (picker.getDialog() != null && picker.getDialog().getWindow() != null) {
                    picker.getDialog().getWindow().setBackgroundDrawable(
                            new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.lighter_green))
                    );
                }
            });
        });

        // Spots slider
        SeekBar seekBar = view.findViewById(R.id.spots_slider);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                spotsValueLabel.setText(progress == 0 ? "Any" : String.valueOf(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Apply
        view.findViewById(R.id.apply_button).setOnClickListener(v -> {
            if (listener != null) {

                //Pass null if no selection (the filter class can handle this)
                Long startTimeStamp = null;
                Long endTimeStamp = null;
                Integer numFreeSpots = seekBar.getProgress();
                if (startDate != null) {
                    startTimeStamp = startDate.getTime()/1000;
                }
                if (endDate != null) {
                    endTimeStamp = endDate.getTime()/1000;
                }
                if (numFreeSpots == 0) {
                    numFreeSpots = null;
                }

                listener.onFiltersApplied(startTimeStamp, endTimeStamp, numFreeSpots);
            }
            dismiss();
        });

        return view;
    }
}