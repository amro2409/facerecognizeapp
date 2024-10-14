package me.dev.is.mllibrary.feature.detector_face.ui.setting;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import me.dev.is.mllibrary.R;


/** Configures still(fixed) image demo settings. */
public class StillImagePreferenceFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference_still_image);
  }
}
