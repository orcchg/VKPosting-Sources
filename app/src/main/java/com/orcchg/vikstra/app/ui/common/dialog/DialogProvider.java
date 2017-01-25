package com.orcchg.vikstra.app.ui.common.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

    /* Text */
    // --------------------------------------------------------------------------------------------
    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, @StringRes int description) {
        return showTextDialog(activity, title, description, null);
    }

    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, String description) {
        String xtitle = activity.getResources().getString(title);
        return showTextDialog(activity, xtitle, description, null);
    }

    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, @StringRes int description,
                                             DialogInterface.OnClickListener listener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        return showTextDialog(activity, xtitle, xdescription, listener);
    }

    public static AlertDialog showTextDialog(Activity activity, String title, String description) {
        return showTextDialog(activity, title, description, null);
    }

    public static AlertDialog showTextDialog(Activity activity, String title, String description,
                                             DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(R.string.button_close, listener)
                .create();
    }

    // ------------------------------------------
    public static AlertDialog showTextDialogTwoButtons(Activity activity, @StringRes int title, @StringRes int description,
                                                       @StringRes int yesLabel, @StringRes int noLabel,
                                                       DialogInterface.OnClickListener yesListener,
                                                       DialogInterface.OnClickListener noListener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        String xyesLabel = activity.getResources().getString(yesLabel);
        String xnoLabel = activity.getResources().getString(noLabel);
        return showTextDialogTwoButtons(activity, xtitle, xdescription, xyesLabel, xnoLabel, yesListener, noListener);
    }

    public static AlertDialog showTextDialogTwoButtons(Activity activity, String title, String description,
                                                       String yesLabel, String noLabel,
                                                       DialogInterface.OnClickListener yesListener,
                                                       DialogInterface.OnClickListener noListener) {
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(yesLabel, yesListener)
                .setNegativeButton(noLabel, noListener)
                .create();
    }

    /* Edit text */
    // --------------------------------------------------------------------------------------------
    public interface OnEditTextDialogOkPressed {
        void onClick(DialogInterface dialog, int which, String text);
    }

    public static AlertDialog showEditTextDialog(Activity activity, String title, String hint,
                                                 @Nullable String init, @NonNull OnEditTextDialogOkPressed okListener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_text, null, false);
        EditText input = (EditText) view.findViewById(R.id.et_input);
        init = TextUtils.isEmpty(init) ? "" : init;
        input.setText(init);
        input.setHint(hint);
        input.setSelection(init.length());
        String errorMessage = activity.getResources().getString(R.string.error_empty_input_text);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    String text = input.getText().toString();
                    if (TextUtils.isEmpty(text)) {
                        input.setError(errorMessage);  // TODO: not working properly
                    } else {
                        okListener.onClick(dialog, which, text);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .create();

        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return alertDialog;
    }

    /* Photo */
    // --------------------------------------------------------------------------------------------
    public static AlertDialog showUploadPhotoDialog(BaseActivity activity) {
        return new AlertDialog.Builder(activity)
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
                            activity.getNavigationComponent().navigator().openCamera(activity, true);
                            break;
                        case 2:
                            activity.getNavigationComponent().navigator().openSocialAlbumsScreen(activity);
                            break;
                    }
                    dialog.dismiss();
                }).create();
    }
}
