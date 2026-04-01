package com.eventwise.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.eventwise.Event;
import com.eventwise.EventDetailFragment;
import com.eventwise.R;
import com.eventwise.database.EventSearcherDatabaseManager;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.camera.CameraSource;
import com.google.mlkit.vision.camera.CameraSourcePreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * QR Scanner Fragment - Scans QR codes to open event details
 * Author: Hao
 * Version: 1.0
 * Date: 2026-04-01
 */
public class QRScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private CameraSourcePreview previewView;
    private CameraSource cameraSource;
    private TextView scannerInstructions;

    private boolean isScanning = true;
    private EventSearcherDatabaseManager eventDbManager;

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

        previewView = view.findViewById(R.id.preview_view);
        scannerInstructions = view.findViewById(R.id.scanner_instructions);

        eventDbManager = new EventSearcherDatabaseManager();

        // Check camera permission
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    private void startCamera() {
        try {
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            cameraSource = new CameraSource.Builder(requireContext(), scanner)
                    .setAutoFocusEnabled(true)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .build();

            cameraSource.start();
            previewView.start(cameraSource);

            // Set up frame processor for real-time scanning
            previewView.setOnFrameProcessor(frame -> {
                if (isScanning) {
                    scanner.process(frame.getImage())
                            .addOnSuccessListener(barcodes -> {
                                if (barcodes != null && !barcodes.isEmpty()) {
                                    handleBarcode(barcodes.get(0));
                                }
                            });
                }
                return null;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to start camera: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleBarcode(Barcode barcode) {
        if (!isScanning) return;

        String rawValue = barcode.getRawValue();
        if (rawValue == null || rawValue.isEmpty()) return;

        // Parse the QR code value - should be event ID
        String eventId = parseEventIdFromQR(rawValue);
        if (eventId == null) {
            Toast.makeText(requireContext(), "Invalid QR code format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop scanning while processing
        isScanning = false;

        // Find event by ID
        eventDbManager.getEvents().addOnSuccessListener(events -> {
            Event foundEvent = null;
            for (Event event : events) {
                if (eventId.equals(event.getEventId())) {
                    foundEvent = event;
                    break;
                }
            }

            if (foundEvent != null) {
                // Navigate to event detail fragment
                EventDetailFragment fragment = EventDetailFragment.newInstance(foundEvent);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.organizer_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                // Resume scanning
                isScanning = true;
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error loading event", Toast.LENGTH_SHORT).show();
            isScanning = true;
        });
    }

    private String parseEventIdFromQR(String qrValue) {
        // Expected format: "event://{eventId}" or just the event ID
        if (qrValue.startsWith("event://")) {
            return qrValue.substring(8);
        }
        return qrValue;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (previewView != null) {
            previewView.stop();
        }
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                cameraSource.start();
                previewView.start(cameraSource);
                isScanning = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (previewView != null) {
            previewView.stop();
        }
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}