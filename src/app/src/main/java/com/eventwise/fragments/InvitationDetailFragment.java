package com.eventwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.google.android.material.snackbar.Snackbar;

/**
 * Minimal UI for declining an invitation.
 * Pass in eventId & entrantId via newInstance() or Navigation args.
 */
public class InvitationDetailFragment extends Fragment {

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_ENTRANT_ID = "arg_entrant_id";

    private String eventId;
    private String entrantId;

    private Button btnDecline;
    private ProgressBar progress;
    private boolean busy = false;

    public static InvitationDetailFragment newInstance(String eventId, String entrantId) {
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        b.putString(ARG_ENTRANT_ID, entrantId);
        InvitationDetailFragment f = new InvitationDetailFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID);
            entrantId = args.getString(ARG_ENTRANT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_invitation_detail, container, false);
        btnDecline = root.findViewById(R.id.btn_decline);
        progress   = root.findViewById(R.id.progress);

        btnDecline.setOnClickListener(v -> {
            if (busy) return;
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.decline_confirm_title)
                    .setMessage(R.string.decline_confirm_msg)
                    .setPositiveButton(R.string.action_yes, (d, w) -> doDecline())
                    .setNegativeButton(R.string.action_no, null)
                    .show();
        });
        return root;
    }

    private void setBusy(boolean b) {
        busy = b;
        if (btnDecline != null) btnDecline.setEnabled(!b);
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void doDecline() {
        if (eventId == null || entrantId == null) {
            Snackbar.make(requireView(), "Missing args", Snackbar.LENGTH_LONG).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        setBusy(true);
        EntrantDatabaseManager mgr = new EntrantDatabaseManager();
        mgr.declineInvitation(entrantId, eventId, timestamp)
                .addOnSuccessListener(unused -> {
                    setBusy(false);
                    Snackbar.make(requireView(), R.string.msg_declined_done, Snackbar.LENGTH_LONG).show();
                    requireActivity().onBackPressed(); // or NavController.navigateUp()
                })
                .addOnFailureListener(e -> {
                    setBusy(false);
                    String msg = e.getMessage() != null ? e.getMessage() : getString(R.string.msg_unable_decline);
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_retry, v -> doDecline())
                            .show();
                });
    }
}