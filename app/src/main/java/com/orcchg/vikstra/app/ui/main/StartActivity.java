package com.orcchg.vikstra.app.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.dialog.DialogActivity;
import com.orcchg.vikstra.app.ui.util.ContextUtility;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class StartActivity extends SimpleBaseActivity {

    private @Nullable AlertDialog dialog1;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, StartActivity.class);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVkLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @DebugLog @Override
            public void onResult(VKAccessToken accessToken) {
                Timber.i("User has just passed authorization");
                goToMainScreen();
            }

            @DebugLog @Override
            public void onError(VKError error) {
                if (error != null) Timber.e("Authorization has failed: %s", error.toString());
                if (!ContextUtility.isActivityDestroyed(StartActivity.this)) {
                    dialog1 = DialogProvider.getTextDialog(StartActivity.this, R.string.dialog_error_title,
                            R.string.main_dialog_authorization_failed, (xdialog, which) -> finish());
                    dialog1.setCancelable(false);
                    dialog1.setCanceledOnTouchOutside(false);
                    dialog1.show();
//                    navigationComponent.navigator().openAuthorizationNotPassedDialog(StartActivity.this);
                }
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case DialogActivity.REQUEST_CODE:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        boolean finishApp = data.getBooleanExtra(DialogActivity.OUT_BUNDLE_KEY_FINISH_APP, false);
                        Timber.v("Requested to finish the whole app: %s", finishApp);
                        if (finishApp) finish();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) dialog1.dismiss();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initVkLogin() {
        long vkUsedId = EndpointUtility.getCurrentUserId();
        if (VKSdk.wakeUpSession(getApplicationContext())) {
            Timber.i("User has already been authorized in Vkontakte, user id: %s", vkUsedId);
            goToMainScreen();
        } else {
            Timber.i("User hasn't been authorized in Vkontakte yet. Starting authorization process...");
            VKSdk.logout();
            VKSdk.login(this, VkontakteEndpoint.Scope.PHOTOS, VkontakteEndpoint.Scope.WALL);
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    void goToMainScreen() {
        navigationComponent.navigator().openMainScreen(this);
        finish();
    }
}
