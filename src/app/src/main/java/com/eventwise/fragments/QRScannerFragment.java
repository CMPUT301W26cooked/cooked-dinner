package com.eventwise.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eventwise.fragments.EntrantEventDetailFragment;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.eventwise.database.SessionStore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.io.IOException;

/**
 * QR Scanner Fragment - Scans QR codes to open event details
 * Author: Hao
 * Version: 1.0
 * Date: 2026-04-01
 */
public class QRScannerFragment extends Fragment {

    private boolean hasStartedScanner = false;

    public QRScannerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startScannerButton = view.findViewById(R.id.start_scanner_button);
        startScannerButton.setOnClickListener(v -> launchScanner());
    }

    @Override
    public void onResume() {
        super.onResume();

        // Optional: auto-launch the scanner the first time this tab opens
//        if (!hasStartedScanner) {
//            hasStartedScanner = true;
//            launchScanner();
//        }
    }

    private void launchScanner() {
        if (getContext() == null) return;

        GmsBarcodeScannerOptions options =
                new GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(requireActivity(), options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();

                    if (rawValue == null || rawValue.trim().isEmpty()) {
                        Toast.makeText(requireContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String eventId = parseEventIdFromQR(rawValue);
                    String entrantId = getCurrentEntrantId();

                    if (entrantId == null || entrantId.trim().isEmpty()) {
                        Toast.makeText(requireContext(), "Could not identify user", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EntrantEventDetailFragment fragment =
                            EntrantEventDetailFragment.newInstance(eventId, entrantId);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.entrant_fragment_container, fragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                })
                .addOnCanceledListener(() -> {
                    // user backed out of scanner
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Scanning failed", Toast.LENGTH_SHORT).show()
                );
    }

    private String getCurrentEntrantId() {
        SessionStore sessionStore = new SessionStore(requireContext());
        return sessionStore.getEntrantProfileId();
    }

    private String parseEventIdFromQR(String qrValue) {
        if (qrValue.startsWith("event://")) {
            return qrValue.substring(8);
        }
        return qrValue;
    }
}