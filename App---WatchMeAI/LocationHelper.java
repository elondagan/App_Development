package com.example.WatchMeAI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationHelper implements LocationListener {

    private LocationManager locationManager;
    private boolean hasLocationPermission = false;
    private Context context;
    public String locationResult = "";

    public LocationHelper(Context context) {
        this.context = context;
    }

    public String startLocationUpdates() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        getAddressFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        stopLocationUpdates();
        return locationResult;
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void checkLocationPermission() {
        try{
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            } else {
                // Permission already granted
                hasLocationPermission = true;
            }
        }
        catch (Exception ignored){ }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationHelper", "onLocationChanged");
        getAddressFromLocation(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0); // Full address
                locationResult = addressLine + "@" + String.valueOf(latitude) + "@" + String.valueOf(longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

