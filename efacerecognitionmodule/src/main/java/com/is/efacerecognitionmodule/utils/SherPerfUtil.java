package com.is.efacerecognitionmodule.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.data.model.LocationModel;

import org.jetbrains.annotations.NotNull;

public class SherPerfUtil {


    @NotNull
    public static LocationModel getLocationModel(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final LocationModel locationModel;
        double latitude = Double.parseDouble(sharedPreferences.getString(context.getString(R.string.pref_key_latitude), "0"));
        double longitude = Double.parseDouble(sharedPreferences.getString(context.getString(R.string.pref_key_longitude), "0"));
        double radius = Double.parseDouble(sharedPreferences.getString(context.getString(R.string.pref_key_radius), "0"));
        locationModel = new LocationModel(latitude, longitude, radius);

        return locationModel;
    }

}
