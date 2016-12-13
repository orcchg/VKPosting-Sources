package com.orcchg.vikstra.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import javax.inject.Inject;

public class PermissionManager {
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 102;

    private final Context context;

    @Inject
    public PermissionManager(Context context) {
        this.context = context;
    }

    /* Read & Write external storage */
    // ------------------------------------------
    public boolean hasReadExternalStoragePermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public boolean hasWriteExternalStoragePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestReadExternalStoragePermission(Activity activity) {
        requestPermissions(activity, READ_EXTERNAL_STORAGE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void requestWriteExternalStoragePermission(Activity activity) {
        requestPermissions(activity, WRITE_EXTERNAL_STORAGE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(Activity activity, int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
