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
import com.orcchg.vikstra.app.ui.util.ContextUtility;

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

    public static AlertDialog getTextDialog(Activity activity, @StringRes int title, @StringRes int description,
                                            @StringRes int yesLabel, DialogInterface.OnClickListener listener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        String xyesLabel = activity.getResources().getString(yesLabel);
        return getTextDialog(activity, xtitle, xdescription, xyesLabel, listener);
    }

    public static AlertDialog getTextDialog(Activity activity, String title, String description) {
        return getTextDialog(activity, title, description, null);
    }

    public static AlertDialog getTextDialog(Activity activity, String title, String description,
                                            DialogInterface.OnClickListener listener) {
        String xyesLabel = activity.getResources().getString(R.string.button_close);
        return getTextDialog(activity, title, description, xyesLabel, listener);
    }

    public static AlertDialog getTextDialog(Activity activity, String title, String description,
                                            String yesLabel, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(yesLabel, listener)
                .create();
    }

    /* Show */
    // ------------------------------------------
    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, @StringRes int description) {
        AlertDialog dialog = getTextDialog(activity, title, description, null);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, String description) {
        String xtitle = activity.getResources().getString(title);
        AlertDialog dialog = getTextDialog(activity, xtitle, description, null);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialog(Activity activity, @StringRes int title, @StringRes int description,
                                             DialogInterface.OnClickListener listener) {
        String xtitle = activity.getResources().getString(title);
        String xdescription = activity.getResources().getString(description);
        AlertDialog dialog = getTextDialog(activity, xtitle, xdescription, listener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialog(Activity activity, String title, String description) {
        AlertDialog dialog = getTextDialog(activity, title, description, null);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialog(Activity activity, String title, String description,
                                             DialogInterface.OnClickListener listener) {
        AlertDialog dialog = getTextDialog(activity, title, description, listener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
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
    public static AlertDialog showTextDialogTwoButtons(Activity activity, @StringRes int title, @StringRes int description,
                                                       @StringRes int yesLabel, @StringRes int noLabel,
                                                       DialogInterface.OnClickListener yesListener,
                                                       DialogInterface.OnClickListener noListener) {
        AlertDialog dialog = getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialogTwoButtons(Activity activity, @StringRes int title, String description,
                                                       @StringRes int yesLabel, @StringRes int noLabel,
                                                       DialogInterface.OnClickListener yesListener,
                                                       DialogInterface.OnClickListener noListener) {
        AlertDialog dialog = getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showTextDialogTwoButtons(Activity activity, String title, String description,
                                                       String yesLabel, String noLabel,
                                                       DialogInterface.OnClickListener yesListener,
                                                       DialogInterface.OnClickListener noListener) {
        AlertDialog dialog = getTextDialogTwoButtons(activity, title, description, yesLabel, noLabel, yesListener, noListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    /* Edit text */
    // --------------------------------------------------------------------------------------------
    public interface OnEditTextDialogOkPressed {
        void onClick(DialogInterface dialog, int which, String text);
    }

    /* Get */
    // ------------------------------------------
    public static AlertDialog getEditTextDialog(Activity activity, @StringRes int title, @StringRes int hint,
                                                String init, @NonNull OnEditTextDialogOkPressed okListener) {
        String xtitle = activity.getResources().getString(title);
        String xhint = activity.getResources().getString(hint);
        return getEditTextDialog(activity, xtitle, xhint, init, okListener);
    }

    public static AlertDialog getEditTextDialog(Activity activity, String title, String hint,
                                                @StringRes int init, @NonNull OnEditTextDialogOkPressed okListener) {
        String xinit = activity.getResources().getString(init);
        return getEditTextDialog(activity, title, hint, xinit, okListener);
    }

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
    public static AlertDialog showEditTextDialog(Activity activity, @StringRes int title, @StringRes int hint,
                                                 String init, @NonNull OnEditTextDialogOkPressed okListener) {
        AlertDialog dialog = getEditTextDialog(activity, title, hint, init, okListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showEditTextDialog(Activity activity, String title, String hint,
                                                 @StringRes int init, @NonNull OnEditTextDialogOkPressed okListener) {
        AlertDialog dialog = getEditTextDialog(activity, title, hint, init, okListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }

    public static AlertDialog showEditTextDialog(Activity activity, String title, String hint,
                                                 @Nullable String init, @NonNull OnEditTextDialogOkPressed okListener) {
        AlertDialog dialog = getEditTextDialog(activity, title, hint, init, okListener);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
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
    public static AlertDialog showUploadPhotoDialog(BaseActivity activity) {
        AlertDialog dialog = getUploadPhotoDialog(activity);
        if (!ContextUtility.isActivityDestroyed(activity)) dialog.show();
        return dialog;
    }
}
