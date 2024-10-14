package com.is.efacerecognitionmodule.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

public class BaseActivity extends ToolbarActivity {
    private final String[] PERMISSIONS=
            {       Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
    private static final int PERMISSIONS_REQUEST = 10;

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NotNull final String[] permissions, @NotNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                Toast.makeText(this, "Yes Permissions Granted.", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
    }

    private  boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    protected boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String p:PERMISSIONS)
            return checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
        return true;
    }

    protected void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSIONS[0])) {
                Toast.makeText(this, "Camera permission is required for this app", Toast.LENGTH_LONG).show();
            }
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST);
        }
    }

}
