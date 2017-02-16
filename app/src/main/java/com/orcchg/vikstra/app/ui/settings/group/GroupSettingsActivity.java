package com.orcchg.vikstra.app.ui.settings.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.settings.group.injection.DaggerGroupSettingsComponent;
import com.orcchg.vikstra.app.ui.settings.group.injection.GroupSettingsComponent;
import com.orcchg.vikstra.app.ui.settings.group.injection.GroupSettingsModule;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupSettingsActivity extends BaseActivity<GroupSettingsContract.View, GroupSettingsContract.Presenter>
        implements GroupSettingsContract.View, ShadowHolder {
    public static final int REQUEST_CODE = Constant.RequestCode.SETTINGS_GROUP_SCREEN;
    public static final int RV_TAG = Constant.ListTag.SETTINGS_GROUP_SCREEN;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.ll_container) ViewGroup settingsContainer;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.error_view) View errorView;
    @BindView(R.id.loading_view) View loadingView;
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();
    }

    private GroupSettingsComponent groupSettingsComponent;

    private @Nullable AlertDialog dialog1;

    public static Intent getCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, GroupSettingsActivity.class);
        return intent;
    }

    @NonNull @Override
    protected GroupSettingsContract.Presenter createPresenter() {
        return groupSettingsComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupSettingsComponent = DaggerGroupSettingsComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupSettingsModule(new GroupSettingsModule())
                .build();
        groupSettingsComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) dialog1.dismiss();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        // TODO:
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.settings_group_screen_name);
        toolbar.setNavigationOnClickListener((view) -> presenter.onBackPressed());
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.save:
                    presenter.onSavePressed();
                    return true;
            }
            return false;
        });
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        return UiUtility.isVisible(settingsContainer);
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        if (isEmpty) {
            settingsContainer.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            settingsContainer.setVisibility(View.VISIBLE);
        }

        showShadow(true);
    }

    @Override
    public void showEmptyList(int tag) {
        showContent(tag, true);
    }

    @Override
    public void showError(int tag) {
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        settingsContainer.setVisibility(View.GONE);
        showShadow(true);
    }

    @Override
    public void showLoading(int tag) {
        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        settingsContainer.setVisibility(View.GONE);
        showShadow(false);  // don't overlap with progress bar
    }

    // ------------------------------------------
    @Override
    public void openSaveChangesDialog() {
        dialog1 = DialogProvider.showTextDialogTwoButtons(this, R.string.settings_group_dialog_save_changes_title,
                R.string.dialog_ask_to_save_changes, R.string.button_save, R.string.button_close,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.onSavePressed();
                },
                (dialog, which) -> {
                    dialog.dismiss();
                    closeView(Activity.RESULT_CANCELED);
                });
    }

    @Override
    public void closeView() {
        finish();  // with currently set result
    }

    @Override
    public void closeView(int resultCode) {
        setResult(resultCode);
        finish();
    }
}
