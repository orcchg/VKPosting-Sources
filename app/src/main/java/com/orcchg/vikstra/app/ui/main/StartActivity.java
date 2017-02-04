package com.orcchg.vikstra.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class StartActivity extends SimpleBaseActivity {

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
                Timber.e("Authorization has failed: %s", error.toString());
                if (!isFinishing()) {
                    AlertDialog dialog = DialogProvider.getTextDialog(StartActivity.this, R.string.dialog_error_title,
                            R.string.main_dialog_authorization_failed, (xdialog, which) -> finish());
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
