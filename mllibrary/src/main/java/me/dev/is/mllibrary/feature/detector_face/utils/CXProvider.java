package me.dev.is.mllibrary.feature.detector_face.utils;

import android.content.Context;

public class CXProvider {
    private Context context;
/*    @SuppressLint("StaticFieldLeak")
    private static CXProvider instance;

    public static CXProvider getInstance() {
        if (instance == null) {
            instance = new CXProvider();
        }
        return instance;
    }*/

    public Context getContext() {
        return context;
    }

    public CXProvider setContext(Context context) {
        this.context = context;
        return this;
    }
}
