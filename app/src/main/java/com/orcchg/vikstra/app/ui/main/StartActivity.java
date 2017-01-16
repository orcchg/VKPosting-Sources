package com.orcchg.vikstra.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.util.vkontakte.VkUtility;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import timber.log.Timber;

public class StartActivity extends SimpleBaseActivity {

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
            @Override
            public void onResult(VKAccessToken accessToken) {
                Timber.i("User has just passed authorization");
                goToMainScreen();
            }

            @Override
            public void onError(VKError error) {
                Timber.w("Authorization has failed: %s", error.toString());
                AlertDialog dialog = DialogProvider.showTextDialog(StartActivity.this, R.string.dialog_error_title,
                        R.string.main_dialog_authorization_failed, (xdialog, which) -> finish());
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initVkLogin() {
        long vkUsedId = VkUtility.getCurrentUserId();
        if (vkUsedId == VkUtility.BAD_VK_USER_ID) {
            Timber.i("User has not authorized in Vkontakte yet. Starting authorization process...");
            VKSdk.login(this, VkontakteEndpoint.Scope.PHOTOS, VkontakteEndpoint.Scope.WALL);
        } else {
            Timber.i("User has already authorized in Vkontakte, id %s", vkUsedId);
            goToMainScreen();
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    void goToMainScreen() {
        navigationComponent.navigator().openMainScreen(this);
        finish();
    }
}