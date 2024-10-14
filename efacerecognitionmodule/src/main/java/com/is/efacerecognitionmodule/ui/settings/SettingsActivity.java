package com.is.efacerecognitionmodule.ui.settings;

import android.app.Fragment;
import android.os.Bundle;

import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.ui.ToolbarActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;


public class SettingsActivity extends ToolbarActivity {
    //private static final String TAG = "SettingsActivity";
    private static final Class<?>[] CLASSES = new Class<?>[]{HomeSettingFragment.class, LocationPreferenceFragment.class};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        startFragment(CLASSES[0]);

    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar.setTitle(R.string.main_btn_title_settings);
    }

    void startFragment(@NotNull Class<?> aClass) {
        try {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content, (Fragment) aClass.getDeclaredConstructor().newInstance())
                    .commit();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}

