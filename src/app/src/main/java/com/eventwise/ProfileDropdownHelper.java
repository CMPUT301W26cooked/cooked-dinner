package com.eventwise;

import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.eventwise.activities.AdminMainActivity;
import com.eventwise.activities.EntrantMainActivity;
import com.eventwise.activities.OrganizerMainActivity;
import com.eventwise.R;

public class ProfileDropdownHelper {

    public static void setupDropdown(Fragment fragment,
                                     LinearLayout profileSwitcher,
                                     TextView titleView,
                                     String currentRole) {

        titleView.setText(currentRole);

        profileSwitcher.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(
                    fragment.requireContext(),
                    v,
                    0,
                    0,
                    R.style.ProfilePopupMenuStyle
            );

            menu.getMenu().add("Entrant");
            menu.getMenu().add("Organizer");
            menu.getMenu().add("Admin");

            menu.setOnMenuItemClickListener(item -> {

                String selected = item.getTitle().toString();
                Class<?> targetActivity;

                switch (selected) {

                    case "Organizer":
                        targetActivity = OrganizerMainActivity.class;
                        break;

                    case "Admin":
                        targetActivity = AdminMainActivity.class;
                        break;

                    case "Entrant":
                    default:
                        targetActivity = EntrantMainActivity.class;
                        break;
                }

                if (fragment.requireActivity().getClass().equals(targetActivity)) {
                    return true;
                }

                Intent intent = new Intent(fragment.requireContext(), targetActivity);
                fragment.startActivity(intent);
                fragment.requireActivity().finish();

                return true;
            });

            menu.show();
        });
    }
}