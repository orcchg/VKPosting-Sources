package com.orcchg.vikstra.app.ui.common.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.PermissionManager;
import com.orcchg.vikstra.app.ui.base.BaseActivity;

public class DialogProvider {

    /* Edit text */
    // --------------------------------------------------------------------------------------------
    public interface OnEditTextDialogOkPressed {
        void onClick(DialogInterface dialog, int which, String text);
    }

    public static void showEditTextDialog(Activity activity, String title, String hint,
                                          @Nullable String init, @NonNull OnEditTextDialogOkPressed okListener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_text, null, false);
        EditText input = (EditText) view.findViewById(R.id.et_input);
        init = TextUtils.isEmpty(init) ? "" : init;
        input.setText(init);
        input.setHint(hint);
        input.setSelection(init.length());

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> okListener.onClick(dialog, which, input.getText().toString()))
                .setNegativeButton(R.string.button_cancel, null)
                .create();

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }

    /* Photo */
    // --------------------------------------------------------------------------------------------
    public static void showUploadPhotoDialog(BaseActivity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_upload_photo_title)
                .setSingleChoiceItems(R.array.dialog_upload_photo_variants, -1, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            PermissionManager pm = activity.getPermissionManagerComponent().permissionManager();
                            if (pm.hasReadExternalStoragePermission()) {
                                activity.getNavigationComponent().navigator().openGallery(activity);
                            } else {
                                pm.requestReadExternalStoragePermission(activity);
                            }
                            break;
                        case 1:
                            activity.getNavigationComponent().navigator().openCamera(activity, false);
                            break;
                        case 2:
                            activity.getNavigationComponent().navigator().openSocialAlbumsScreen(activity);
                            break;
                    }
                    dialog.dismiss();
                })
                .show();
    }
}
