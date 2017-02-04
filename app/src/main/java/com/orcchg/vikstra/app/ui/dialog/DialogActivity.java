package com.orcchg.vikstra.app.ui.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;

public class DialogActivity extends SimpleBaseActivity {
    public static final String EXTRA_DIALOG_TITLE = "extra_dialog_title";
    public static final String EXTRA_DIALOG_DESCRIPTION = "extra_dialog_description";
    public static final String EXTRA_DIALOG_YES_BUTTON_LABEL = "extra_dialog_yes_button_label";

    private String DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_YES_BUTTON_LABEL;

    public static Intent getCallingIntent(@NonNull Context context, @StringRes int description) {
        return getCallingIntent(context, 0, description, 0);
    }

    public static Intent getCallingIntent(@NonNull Context context, @StringRes int description, @StringRes int yesLabel) {
        return getCallingIntent(context, 0, description, yesLabel);
    }

    public static Intent getCallingIntent(@NonNull Context context, @StringRes int title,
                                          @StringRes int description, @StringRes int yesLabel) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra(EXTRA_DIALOG_TITLE, title);
        intent.putExtra(EXTRA_DIALOG_DESCRIPTION, description);
        intent.putExtra(EXTRA_DIALOG_YES_BUTTON_LABEL, yesLabel);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initResources();
        setFinishOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AlertDialog dialog = DialogProvider.getTextDialog(this, DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_YES_BUTTON_LABEL,
                (xdialog, which) -> {
                    xdialog.dismiss();
                    navigationComponent.navigator().openStartScreen(this);
                    finish();
                });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Intent intent = getIntent();
        Resources resources = getResources();
        @StringRes int titleId = intent.getIntExtra(EXTRA_DIALOG_TITLE, R.string.dialog_warning_title);
        @StringRes int descriptionId = intent.getIntExtra(EXTRA_DIALOG_DESCRIPTION, R.string.toast_access_token_has_expired);
        @StringRes int yesLabelId = intent.getIntExtra(EXTRA_DIALOG_YES_BUTTON_LABEL, R.string.button_close);
        DIALOG_TITLE = resources.getString(titleId != 0 ? titleId : R.string.dialog_warning_title);
        DIALOG_DESCRIPTION = resources.getString(descriptionId);
        DIALOG_YES_BUTTON_LABEL = resources.getString(yesLabelId != 0 ? yesLabelId : R.string.button_close);
    }
}
