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
    /* Get */
    // ------------------------------------------
    public static AlertDialog getTextDialog(Activity activity, @StringRes int title, @StringRes int description) {
        return getTextDialog(activity, title, description, null);
    }

    public static AlertDialog getTextDialog(Activity activity, @StringRes int title, String description) {
        String xtitle = activity.getResources().getString(title);
        return getTextDialog(activity, xtitle, description, null);
    }

    public static AlertDialog getTextDialog(Activity activity, @StringRes int title, @StringRes int description,
                                            DialogInterface.OnClickListener listener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        return getTextDialog(activity, xtitle, xdescription, listener);
    }

    public static AlertDialog getTextDialog(Activity activity, String title, String description) {
        return getTextDialog(activity, title, description, null);
    }

    public static AlertDialog getTextDialog(Activity activity, String title, String description,
                                            DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(R.string.button_close, listener)
                .create();
    }

    /* Show */
    // ------------------------------------------
    public static void showTextDialog(Activity activity, @StringRes int title, @StringRes int description) {
        if (!activity.isFinishing()) getTextDialog(activity, title, description, null).show();
    }

    public static void showTextDialog(Activity activity, @StringRes int title, String description) {
        String xtitle = activity.getResources().getString(title);
        if (!activity.isFinishing()) getTextDialog(activity, xtitle, description, null).show();
    }

    public static void showTextDialog(Activity activity, @StringRes int title, @StringRes int description,
                                      DialogInterface.OnClickListener listener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        if (!activity.isFinishing()) getTextDialog(activity, xtitle, xdescription, listener).show();
    }

    public static void showTextDialog(Activity activity, String title, String description) {
        if (!activity.isFinishing()) getTextDialog(activity, title, description, null).show();
    }

    public static void showTextDialog(Activity activity, String title, String description,
                                      DialogInterface.OnClickListener listener) {
        if (!activity.isFinishing()) getTextDialog(activity, title, description, listener).show();
    }

    // --------------------------------------------------------------------------------------------
    /* Get */
    // ------------------------------------------
    public static AlertDialog getTextDialogTwoButtons(Activity activity, @StringRes int title, @StringRes int description,
                                                      @StringRes int yesLabel, @StringRes int noLabel,
                                                      DialogInterface.OnClickListener yesListener,
                                                      DialogInterface.OnClickListener noListener) {
        String xdescription = activity.getResources().getString(description);
        return getTextDialogTwoButtons(activity, title, xdescription, yesLabel, noLabel, yesListener, noListener);
    }

    public static AlertDialog getTextDialogTwoButtons(Activity activity, @StringRes int title, String description,
                                                      @StringRes int yesLabel, @StringRes int noLabel,
                                                      DialogInterface.OnClickListener yesListener,
                                                      DialogInterface.OnClickListener noListener) {
        String xtitle = activity.getResources().getString(title);
        String xyesLabel = activity.getResources().getString(yesLabel);
        String xnoLabel = activity.getResources().getString(noLabel);
        return getTextDialogTwoButtons(activity, xtitle, description, xyesLabel, xnoLabel, yesListener, noListener);
    }

    public static AlertDialog getTextDialogTwoButtons(Activity activity, String title, String description,
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

    /* Show */
    // ------------------------------------------
    public static void showTextDialogTwoButtons(Activity activity, @StringRes int title, @StringRes int description,
                                                @StringRes int yesLabel, @StringRes int noLabel,
                                                DialogInterface.OnClickListener yesListener,
                                                DialogInterface.OnClickListener noListener) {
        if (!activity.isFinishing()) getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener).show();
    }

    public static void showTextDialogTwoButtons(Activity activity, @StringRes int title, String description,
                                                @StringRes int yesLabel, @StringRes int noLabel,
                                                DialogInterface.OnClickListener yesListener,
                                                DialogInterface.OnClickListener noListener) {
        if (!activity.isFinishing()) getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener).show();
    }

    public static void showTextDialogTwoButtons(Activity activity, String title, String description,
                                                String yesLabel, String noLabel,
                                                DialogInterface.OnClickListener yesListener,
                                                DialogInterface.OnClickListener noListener) {
        if (!activity.isFinishing()) getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener).show();
    }

    /* Edit text */
    // --------------------------------------------------------------------------------------------
    public interface OnEditTextDialogOkPressed {
        void onClick(DialogInterface dialog, int which, String text);
    }

    /* Get */
    // ------------------------------------------
    public static AlertDialog getEditTextDialog(Activity activity, String title, String hint,
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

    /* Show */
    // ------------------------------------------
    public static void showEditTextDialog(Activity activity, String title, String hint,
                                          @Nullable String init, @NonNull OnEditTextDialogOkPressed okListener) {
        if (!activity.isFinishing()) getEditTextDialog(activity, title, hint, init, okListener).show();
    }

    /* Photo */
    // --------------------------------------------------------------------------------------------
    /* Get */
    // ------------------------------------------
    public static AlertDialog getUploadPhotoDialog(BaseActivity activity) {
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

    /* Show */
    // ------------------------------------------
    public static void showUploadPhotoDialog(BaseActivity activity) {
        if (!activity.isFinishing()) getUploadPhotoDialog(activity).show();
    }
}
