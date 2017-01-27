package com.orcchg.vikstra.app.ui.group.list.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.permission.BasePermissionActivity;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.DaggerGroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.GroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListFragment;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupListActivity extends BasePermissionActivity<GroupListContract.View, GroupListContract.Presenter>
        implements GroupListContract.View, IScrollList, ShadowHolder {
    private static final String FRAGMENT_TAG = "group_list_fragment_tag";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.GROUP_LIST_SCREEN;

    private String ADD_KEYWORD_DIALOG_TITLE, ADD_KEYWORD_DIALOG_HINT,
            DIALOG_TITLE, DIALOG_HINT, EDIT_TITLE_DIALOG_TITLE, EDIT_TITLE_DIALOG_HINT,
            INFO_TITLE, SNACKBAR_DUMP_SUCCESS, SNACKBAR_KEYWORDS_LIMIT;

    @BindView(R.id.coordinator_root) ViewGroup coordinatorRoot;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.tv_info_title) TextView selectedGroupsCountView;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.fab) FloatingActionButton fab;
    @OnClick(R.id.fab)
    void onPostFabClick() {
        presenter.onFabClick();
    }
    @OnClick(R.id.btn_add_keyword)
    void onAddKeywordClick() {
        openAddKeywordDialog();
    }
    @OnClick(R.id.btn_change_post)
    void onChangePost() {
        navigationComponent.navigator().openPostListScreen(this);
    }

    private GroupListComponent groupComponent;
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    private @DebugSake int chosenSettingVariant = 0;  // for DEBUG

    public static Intent getCallingIntent(@NonNull Context context, long keywordBunldeId, long postId) {
        Intent intent = new Intent(context, GroupListActivity.class);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBunldeId);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    @NonNull @Override
    protected GroupListContract.Presenter createPresenter() {
        return groupComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        groupComponent = DaggerGroupListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupListModule(new GroupListModule(FileUtility.getDumpGroupsFileName(this, true /* external */)))
                .build();
        groupComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
    }

    /* Permissions */
    // ------------------------------------------
    @Override
    protected void onPermissionGranted_writeExternalStorage() {
        presenter.onDumpPressed();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fab.hide();  // hide fab at fresh start before post fetched
        postThumbnail.setOnClickListener((view) -> navigationComponent.navigator().openPostCreateScreen(this, postId));
        updateSelectedGroupsCounter(0, 0);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupListFragment fragment = GroupListFragment.newInstance(keywordBundleId, postId);
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());  // finish with current result
        toolbar.inflateMenu(R.menu.edit_dump);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    openEditTitleDialog(toolbar.getTitle().toString());
                    return true;
                case R.id.dump:
                    askForPermission_writeExternalStorage();
                    return true;
                case R.id.settings:
                    CharSequence[] variants = getResources().getStringArray(R.array.group_list_settings_posting_interval_variants);
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.group_list_settings_posting_interval_title)
                            .setSingleChoiceItems(variants, chosenSettingVariant, (dialog, which) -> {
                                chosenSettingVariant = which;
                                int timeout = Integer.parseInt(variants[which].toString());
                                presenter.setPostingTimeout(timeout);
                                dialog.dismiss();
                            }).show();
                    return true;
            }
            return false;
        });

        if (!AppConfig.INSTANCE.showSettingsMenuOnGroupListScreen()) {
            toolbar.getMenu().removeItem(R.id.settings);
        }
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAddKeywordError() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_error_add_keyword);
    }

    @Override
    public void onGroupsNotSelected() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_groups_not_selected_message);
    }

    @Override
    public void onKeywordsLimitReached(int limit) {
        UiUtility.showSnackbar(coordinatorRoot, String.format(Locale.ENGLISH, SNACKBAR_KEYWORDS_LIMIT, limit));
    }

    @Override
    public void onPostNotSelected() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_post_is_empty_message);
    }

    // ------------------------------------------
    @Override
    public void openAddKeywordDialog() {
        DialogProvider.showEditTextDialog(this, ADD_KEYWORD_DIALOG_TITLE, ADD_KEYWORD_DIALOG_HINT, null,
                (dialog, which, text) -> {
                    if (!TextUtils.isEmpty(text)) presenter.addKeyword(Keyword.create(text));
                });
    }

    @Override
    public void openEditDumpFileNameDialog() {
        DialogProvider.showEditTextDialog(this, DIALOG_TITLE, DIALOG_HINT, "",
                (dialog, which, text) -> {
                    String path = FileUtility.makeDumpFileName(this, text, true /* external */);
                    presenter.performDumping(path);
                    dialog.dismiss();
                });
    }

    @Override
    public void openDumpNotReadyDialog() {
        DialogProvider.showTextDialog(this, R.string.dialog_warning_title, R.string.group_list_dialog_groups_not_ready_to_dump);
    }

    @Override
    public void openEditTitleDialog(@Nullable String initTitle) {
        DialogProvider.showEditTextDialog(this, EDIT_TITLE_DIALOG_TITLE, EDIT_TITLE_DIALOG_HINT, initTitle,
                (dialog, which, text) -> {
                    toolbar.setTitle(text);
                    presenter.onTitleChanged(text);
                });
    }

    // ------------------------------------------
    @Override
    public void setCloseViewResult(int result) {
        setResult(result);
    }

    @Override
    public void setInputGroupsTitle(String title) {
        if (!TextUtils.isEmpty(title)) toolbar.setTitle(title);
    }

    // ------------------------------------------
    @Override
    public void showDumpError() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_groups_dump_failed, Snackbar.LENGTH_LONG);
    }

    @Override
    public void showDumpSuccess(String path) {
        UiUtility.showSnackbar(coordinatorRoot, String.format(Locale.ENGLISH, SNACKBAR_DUMP_SUCCESS, FileUtility.refineExternalPath(path)), Snackbar.LENGTH_LONG);
    }

    @Override
    public void showEmptyPost() {
        fab.hide();
        postThumbnail.setPost(null);
    }

    @Override
    public void showErrorPost() {
        postThumbnail.showError(true);
    }

    @Override
    public void showPost(@Nullable PostSingleGridItemVO viewObject) {
        postThumbnail.setPost(viewObject);
    }

    @Override
    public void showPostingButton(boolean isVisible) {
        if (isVisible) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @Override
    public void showPostingStartedMessage(boolean isStarted) {
        if (isStarted) {
            UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_posting_started);
        } else {
            UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_posting_finished);
        }
    }

    @Override
    public void updateSelectedGroupsCounter(int count, int total) {
        selectedGroupsCountView.setText(String.format(Locale.ENGLISH, INFO_TITLE, count, total));
    }

    // ------------------------------------------
    @Override
    public void retryList() {
        presenter.retry();
    }

    @Override
    public void onEmptyList() {
        openAddKeywordDialog();
    }

    @Override
    public void onScrollList(int itemsLeftToEnd) {
        // TODO: on scroll list
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        ADD_KEYWORD_DIALOG_TITLE = resources.getString(R.string.group_list_dialog_new_keyword_title);
        ADD_KEYWORD_DIALOG_HINT = resources.getString(R.string.group_list_dialog_new_keyword_hint);
        DIALOG_TITLE = resources.getString(R.string.group_list_dialog_new_dump_file_title);
        DIALOG_HINT = resources.getString(R.string.group_list_dialog_new_dump_file_hint);
        EDIT_TITLE_DIALOG_TITLE = resources.getString(R.string.dialog_input_edit_title);
        EDIT_TITLE_DIALOG_HINT = resources.getString(R.string.dialog_input_edit_title_hint);
        INFO_TITLE = resources.getString(R.string.group_list_selected_groups_total_count);
        SNACKBAR_DUMP_SUCCESS = resources.getString(R.string.group_list_snackbar_groups_dump_succeeded);
        SNACKBAR_KEYWORDS_LIMIT = resources.getString(R.string.group_list_snackbar_keywords_limit_message);
    }
}
