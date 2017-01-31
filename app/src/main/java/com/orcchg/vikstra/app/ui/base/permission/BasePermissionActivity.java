package com.orcchg.vikstra.app.ui.base.permission;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.PermissionManager;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;

import java.util.Arrays;
import java.util.Locale;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BasePermissionActivity<V extends MvpView, P extends MvpPresenter<V>> extends BaseActivity<V, P> {

    @DebugLog @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.i("onRequestPermissionsResult, requestCode = %s", requestCode);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            switch (requestCode) {
                case PermissionManager.READ_EXTERNAL_STORAGE_REQUEST_CODE:
                    onPermissionGranted_readExternalStorage();
                    break;
                case PermissionManager.WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                    onPermissionGranted_writeExternalStorage();
                    break;
            }
        } else {
            Timber.w("Permissions [%s] were not granted", Arrays.toString(permissions));
            if (!shouldShowRequestPermissionRationale(permissions[0])) onPermissionDenied(requestCode);
        }
    }

    /* Permissions handling */
    // --------------------------------------------------------------------------------------------
    /* Ask for permission */
    // ------------------------------------------
    protected void askForPermission_readExternalStorage() {
        Timber.i("askForPermission_readExternalStorage");
        PermissionManager pm = getPermissionManagerComponent().permissionManager();
        if (pm.hasReadExternalStoragePermission()) {
            onPermissionGranted_readExternalStorage();
        } else {
            pm.requestReadExternalStoragePermission(this);
        }
    }

    protected void askForPermission_writeExternalStorage() {
        Timber.i("askForPermission_writeExternalStorage");
        PermissionManager pm = getPermissionManagerComponent().permissionManager();
        if (pm.hasWriteExternalStoragePermission()) {
            onPermissionGranted_writeExternalStorage();
        } else {
            pm.requestWriteExternalStoragePermission(this);
        }
    }

    /* Permission granted */
    // ------------------------------------------
    protected void onPermissionGranted_readExternalStorage() {
        // override in subclasses
    }

    protected void onPermissionGranted_writeExternalStorage() {
        // override in subclasses
    }

    /* Permission denied */
    // ------------------------------------------
    protected void onPermissionDenied(int requestCode) {
        Timber.i("onPermissionDenied: %s", requestCode);
        // open warning dialog by default
        int index = requestCode - PermissionManager.REQUEST_CODE_BASE;
        String[] permissions = getResources().getStringArray(R.array.permission_variants);
        String permission = "Unknown code=" + requestCode;
        if (index >= 0 && index < permissions.length) permission = permissions[index];  // guard from index out of bounds
        String description = String.format(Locale.ENGLISH, getResources().getString(R.string.permission_not_granted_message), permission);
        DialogProvider.showTextDialogTwoButtons(this, R.string.dialog_warning_title, description,
                R.string.button_settings, R.string.button_close,
                (dialog, which) -> {
                    dialog.dismiss();
                    navigationComponent.navigator().openSettings(this);
                },
                (dialog, which) -> dialog.dismiss());
    }
}
