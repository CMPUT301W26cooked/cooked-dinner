package com.eventwise.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventwise.R;
import com.eventwise.database.EntrantDatabaseManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Profile screen where user can delete own profile.
 * - Shows a confirm dialog.
 * - Calls EntrantDatabaseManager.deleteEntrant(profileId).
 * - On success: clears UI and navigates back to MainActivity (or LoginActivity) using Intent.
 */
public class ProfileFragment extends Fragment {

    private Button btnDelete;
    private ProgressBar progress;

    // TODO: 这里填入“当前登录用户/当前要删除的 Entrant 的 Id”
    private String currentEntrantId = "REPLACE_WITH_REAL_ID";

    private EntrantDatabaseManager entrantDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 使用你的布局文件
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnDelete = view.findViewById(R.id.btnDeleteProfile);   // 确保和你的布局 id 一致
        progress = view.findViewById(R.id.progressDelete);      // 确保和你的布局 id 一致

        entrantDb = new EntrantDatabaseManager(); // ✅ 无参构造

        btnDelete.setOnClickListener(v -> showConfirmDialog());
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dialog.dismiss();
                    performDelete(currentEntrantId);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void performDelete(String entrantId) {
        if (entrantId == null || entrantId.trim().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.session_expired), Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        setLoading(true);

        entrantDb.deleteEntrant(entrantId)
                .addOnSuccessListener(unused -> {
                    if (!isAdded()) return;
                    setLoading(false);
                    Toast.makeText(requireContext(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                    // TODO: 如果你有 SessionStore，这里可以 session.clear()
                    navigateToWelcome(); // 或 navigateToLogin()
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    setLoading(false);
                    String msg = (e != null && e.getMessage() != null) ? e.getMessage()
                            : getString(R.string.delete_failed);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDelete.setEnabled(!loading);
    }

    private void navigateToWelcome() {
        // 如果你有 LoginActivity，改成 LoginActivity.class 即可
        Intent intent = new Intent(requireContext(), com.eventwise.activities.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), com.eventwise.activities.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
