package com.eventwise;

import android.annotation.SuppressLint;
import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * This serves as the basic class for the location object.
 * @author Luke Forster
 * @version 1.0
 * @since 2026-04-01
 */
public class Location {

    private double latitude;
    private double longitude;
    private float accuracy;

    public Location() {}

    public Location(double latitude, double longitude, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getAccuracy() { return accuracy; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context,
                                          LocationCallback callback) {

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(deviceLocation -> {

                    if (deviceLocation != null) {

                        Location location = new Location(
                                deviceLocation.getLatitude(),
                                deviceLocation.getLongitude(),
                                deviceLocation.getAccuracy()
                        );

                        callback.onLocationReceived(location);

                    } else {

                        callback.onLocationReceived(null);
                    }
                });
    }
}
