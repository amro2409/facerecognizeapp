package com.is.efacerecognitionmodule;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.is.efacerecognitionmodule.data.model.LocationModel;
import com.is.efacerecognitionmodule.databinding.ActivityMainBinding;
import com.is.efacerecognitionmodule.ui.BaseActivity;
import com.is.efacerecognitionmodule.ui.LiveRecognitionActivity;
import com.is.efacerecognitionmodule.ui.StillRegistrationActivity;
import com.is.efacerecognitionmodule.ui.settings.SettingsActivity;
import com.is.efacerecognitionmodule.utils.Logger;
import com.is.efacerecognitionmodule.utils.SherPerfUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MainActivity extends BaseActivity {
    Logger LOGGER = new Logger();

    ActivityMainBinding mainBinding;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationModel locationModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

       if (!hasPermission())requestPermission();

        //mainBinding.buttonOldReg.setOnClickListener(v -> checkLocation(RegisterFaceImageActivity.class));
        mainBinding.buttonRegister.setOnClickListener(v -> checkLocation(StillRegistrationActivity.class));
        mainBinding.buttonRecognition.setOnClickListener(v -> checkLocation(LiveRecognitionActivity.class));
        mainBinding.buttonSettings.setOnClickListener(v -> startActivity(SettingsActivity.class));


        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar.setNavigationIcon(null);
        locationModel= SherPerfUtil.getLocationModel(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void startActivity(Class<?> aClass) {
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
        startActivity(new Intent(getApplicationContext(), aClass));
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
    }

    private void checkLocation(Class<?> aClass) {

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // حصلنا على الموقع الحالي
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // استخدم الاحداثيات للتحقق من الموقع
                if (isWithinAllowedLocation(latitude, longitude)) {
                    // السماح بالانتقال إلى شاشة  الوجه
                    startActivity(aClass);
                } else {
                    Toast.makeText(getApplicationContext(), "لا يمكنك تسجيل او تحقق من الوجه إلا في الموقع المسموح به.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public boolean isWithinAllowedLocation(double currentLatitude, double currentLongitude) {
        LOGGER.d(format("currentLatitude: %f,currentLongitude: %f", currentLatitude, currentLongitude));

        float[] results = new float[1];
        Location.distanceBetween(currentLatitude, currentLongitude, locationModel.getAllowedLatitude(), locationModel.getAllowedLongitude(), results);
        float distanceInMeters = results[0];
        LOGGER.d(format("distanceInMeters: %f", distanceInMeters));
        return distanceInMeters <= locationModel.getAllowedRadius();
    }

    @NotNull
    protected String format(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }


}