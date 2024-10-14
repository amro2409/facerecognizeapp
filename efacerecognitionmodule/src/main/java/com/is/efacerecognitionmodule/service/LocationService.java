package com.is.efacerecognitionmodule.service;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationService {
    double allowedLatitude = 37.7749; //
    double allowedLongitude = -122.4194;
    double allowedRadius = 100; // نصف القطر بالأمتار

    FusedLocationProviderClient fusedLocationClient;

    public LocationService(Context context) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    private void getLastLocation(Activity context){
        fusedLocationClient.getLastLocation().addOnSuccessListener(context, location -> {
                    // حصلنا على الموقع الحالي
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        // استخدم الاحداثيات للتحقق من الموقع
                        if (isWithinAllowedLocation(latitude, longitude)) {
                            // السماح بالانتقال إلى شاشة تسجيل الوجه
                            //Intent intent = new Intent(this, FaceRegisterActivity.class);
                           //context.startActivity(intent);
                        } else {
                            // عرض رسالة تحذير
                            Toast.makeText(context, "لا يمكنك تسجيل الوجه إلا في الموقع المسموح به.", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    public boolean isWithinAllowedLocation(double currentLatitude, double currentLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(currentLatitude, currentLongitude, allowedLatitude, allowedLongitude, results);
        float distanceInMeters = results[0];

        return distanceInMeters <= allowedRadius;
    }

}

