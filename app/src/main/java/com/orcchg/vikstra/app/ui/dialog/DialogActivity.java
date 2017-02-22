package com.orcchg.vikstra.app.ui.dialog;

import android.app.Activity;
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
import com.orcchg.vikstra.domain.util.Constant;

import timber.log.Timber;

public class DialogActivity extends SimpleBaseActivity {
    private static final String BUNDLE_KEY_FINISH_APP = "bundle_key_finish_app";
    public static final String OUT_BUNDLE_KEY_FINISH_APP = "out_bundle_key_finish_app";
    public static final String EXTRA_DIALOG_TITLE = "extra_dialog_title";
    public static final String EXTRA_DIALOG_DESCRIPTION = "extra_dialog_description";
    public static final String EXTRA_DIALOG_YES_BUTTON_LABEL = "extra_dialog_yes_button_label";
    public static final String EXTRA_FINISH_APP = "extra_finish_app";
    public static final int REQUEST_CODE = Constant.RequestCode.DIALOG_SCREEN;

    private String DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_YES_BUTTON_LABEL;

    private boolean finishApp = false;

    private @Nullable AlertDialog dialog1;

    public static Intent getCallingIntent(@NonNull Context context, @StringRes int title,
                                          @StringRes int description, @StringRes int yesLabel,
                                          boolean finishApp) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra(EXTRA_DIALOG_TITLE, title);
        intent.putExtra(EXTRA_DIALOG_DESCRIPTION, description);
        intent.putExtra(EXTRA_DIALOG_YES_BUTTON_LABEL, yesLabel);
        intent.putExtra(EXTRA_FINISH_APP, finishApp);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        initResources();
        setFinishOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog1 = DialogProvider.getTextDialog(this, DIALOG_TITLE, DIALOG_DESCRIPTION, DIALOG_YES_BUTTON_LABEL,
                (xdialog, which) -> {
                    xdialog.dismiss();
                    Intent data = new Intent();
                    if (finishApp) {
                        data.putExtra(OUT_BUNDLE_KEY_FINISH_APP, true);
                    } else {
                        navigationComponent.navigator().openStartScreen(this);
                    }
                    setResult(Activity.RESULT_OK, data);
                    finish();
                });
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) dialog1.dismiss();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            finishApp = savedInstanceState.getBoolean(BUNDLE_KEY_FINISH_APP, false);
        } else {
            finishApp = getIntent().getBooleanExtra(EXTRA_FINISH_APP, false);
        }
        Timber.d("Finish app flag: %s", finishApp);
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
