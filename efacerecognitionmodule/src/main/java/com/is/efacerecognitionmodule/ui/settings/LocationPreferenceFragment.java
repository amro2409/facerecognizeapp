package com.is.efacerecognitionmodule.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.is.efacerecognitionmodule.R;

public class LocationPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_location);
    }

}
