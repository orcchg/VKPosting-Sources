package com.orcchg.vikstra.app.ui.group.list.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.permission.BasePermissionActivity;
import com.orcchg.vikstra.app.ui.common.content.IListReach;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.showcase.SingleShot;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.DaggerGroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.GroupListComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListFragment;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class GroupListActivity extends BasePermissionActivity<GroupListContract.View, GroupListContract.Presenter>
        implements GroupListContract.View, IListReach, IScrollList, ShadowHolder, OnShowcaseEventListener {
    private static final String FRAGMENT_TAG = "group_list_fragment_tag";
    private static final String BUNDLE_KEY_CHOSEN_SETTING_VARIANT = "bundle_key_chosen_setting_variant";
    private static final String BUNDLE_KEY_KEYWORD_BUNDLE_ID = "bundle_key_keyword_bundle_id";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.GROUP_LIST_SCREEN;

    private String ADD_KEYWORD_DIALOG_TITLE, ADD_KEYWORD_DIALOG_HINT,
            DUMP_FILE_DIALOG_TITLE, DUMP_FILE_DIALOG_HINT,
            EMAIL_FILE_DIALOG_TITLE, EMAIL_FILE_DIALOG_HINT, EMAIL_BODY, EMAIL_SUBJECT,
            EDIT_TITLE_DIALOG_TITLE, EDIT_TITLE_DIALOG_HINT, INFO_TITLE, GROUPS_DUMP_FILE_PREFIX,
            SNACKBAR_KEYWORD_ALREADY_ADDED, SNACKBAR_DUMP_SUCCESS, SNACKBAR_KEYWORDS_LIMIT;

    @BindView(R.id.coordinator_root) ViewGroup coordinatorRoot;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.tv_info_title) TextView selectedGroupsCountView;
    @BindView(R.id.btn_add_keyword) Button addKeywordButton;
    @BindView(R.id.btn_change_post) Button changePostButton;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.fl_container) FrameLayout frameLayout;
    @BindView(R.id.fl_fab_container) FrameLayout fabFrameLayout;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.fab_label) TextView fabLabel;
    @OnClick(R.id.fab)
    void onPostFabClick() {
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_HIDE);
        presenter.onFabClick();
    }
    @OnClick(R.id.btn_add_keyword)
    void onAddKeywordClick() {
        openAddKeywordDialog();
    }
    @OnClick(R.id.btn_change_post)
    void onChangePostClick() {
        navigationComponent.navigator().openPostListScreen(this, postId);
    }

    private GroupListComponent groupComponent;
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    private @DebugSake int chosenSettingVariant = 0;  // for DEBUG

    private @Nullable ShowcaseView showcaseView;

    private @Nullable AlertDialog dialog1, dialog2, dialog3, dialog4, dialog5;

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
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_ADD_KEYWORD);
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_KEY_CHOSEN_SETTING_VARIANT, chosenSettingVariant);
        outState.putLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, keywordBundleId);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) dialog1.dismiss();
        if (dialog2 != null) dialog2.dismiss();
        if (dialog3 != null) dialog3.dismiss();
        if (dialog4 != null) dialog4.dismiss();
        if (dialog5 != null) dialog5.dismiss();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("Restore state");
            chosenSettingVariant = savedInstanceState.getInt(BUNDLE_KEY_CHOSEN_SETTING_VARIANT, 0);
            keywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        } else {
            chosenSettingVariant = 0;
            keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
        }
        Timber.d("KeywordBundle id: %s ; Post id: %s", keywordBundleId, postId);
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
        showFab(false);  // hide fab at fresh start before post fetched
        postThumbnail.setOnClickListener((view) -> {
            if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_MAKE_WALL_POSTING);
            presenter.onPostThumbnailClick(postId);
        });
        postThumbnail.setErrorRetryButtonClickListener((view) -> presenter.retryPost());
        updateSelectedGroupsCounter(0, 0);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            GroupListFragment fragment = GroupListFragment.newInstance(keywordBundleId, postId);
            fm.beginTransaction().replace(R.id.fl_container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.group_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> onBackPressed());  // finish with current result
        switch (AppConfig.INSTANCE.sendDumpFilesVia()) {
            case AppConfig.SEND_DUMP_FILE:
                toolbar.inflateMenu(R.menu.edit_dump);
                break;
            case AppConfig.SEND_DUMP_EMAIL:
                toolbar.inflateMenu(R.menu.edit_send);
                break;
            case AppConfig.SEND_DUMP_SHARE:
                toolbar.inflateMenu(R.menu.edit_share);
                break;
        }
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    openEditTitleDialog(toolbar.getTitle().toString());
                    return true;
                case R.id.dump:
                case R.id.send:
                case R.id.share:
                    askForPermission_writeExternalStorage();
                    return true;
                case R.id.settings:
                    CharSequence[] variants = getResources().getStringArray(R.array.group_list_settings_posting_interval_variants);
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.group_list_settings_posting_interval_title)
                            .setSingleChoiceItems(variants, chosenSettingVariant, (dialog, which) -> {
                                dialog.dismiss();
                                chosenSettingVariant = which;
                                int timeout = Integer.parseInt(variants[which].toString());
                                presenter.setPostingTimeout(timeout);
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
    public void enableAddKeywordButton(boolean isEnabled) {
        addKeywordButton.setEnabled(isEnabled);
    }

    // ------------------------------------------
    @Override
    public String getDumpFilename() {
        return FileUtility.makeDumpFileName(this, GROUPS_DUMP_FILE_PREFIX, true /* external */, true /* with timestamp */);
    }

    // ------------------------------------------
    @Override
    public void onAddKeywordError() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_error_add_keyword);
    }

    @Override
    public void onAlreadyAddedKeyword(String keyword) {
        UiUtility.showSnackbar(coordinatorRoot, String.format(Locale.ENGLISH, SNACKBAR_KEYWORD_ALREADY_ADDED, keyword));
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
        dialog1 = DialogProvider.showEditTextDialog(this, ADD_KEYWORD_DIALOG_TITLE, ADD_KEYWORD_DIALOG_HINT, null,
                (dialog, which, text) -> {
                    if (!TextUtils.isEmpty(text)) {
                        dialog.dismiss();
                        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_SELECT_POST);
                        presenter.addKeyword(Keyword.create(text));
                    }
                });
    }

    @Override
    public void openEditDumpFileNameDialog() {
        dialog2 = DialogProvider.showEditTextDialog(this, DUMP_FILE_DIALOG_TITLE, DUMP_FILE_DIALOG_HINT, "",
                (dialog, which, text) -> {
                    if (!TextUtils.isEmpty(text)) {
                        dialog.dismiss();
                        String path = FileUtility.makeDumpFileName(this, text, true /* external */);
                        presenter.performDumping(path);
                    }
                });
    }

    @Override
    public void openEditDumpEmailDialog() {
        dialog5 = DialogProvider.showEditTextDialog(this, EMAIL_FILE_DIALOG_TITLE, EMAIL_FILE_DIALOG_HINT, "",
                (dialog, which, email) -> {
                    if (!TextUtils.isEmpty(email)) {
                        dialog.dismiss();
                        String path = getDumpFilename();
                        presenter.performDumping(path, email);
                    }
                });
    }

    @Override
    public void openDumpNotReadyDialog() {
        dialog3 = DialogProvider.showTextDialog(this, R.string.dialog_warning_title, R.string.group_list_dialog_groups_not_ready_to_dump);
    }

    @Override
    public void openEditTitleDialog(@Nullable String initTitle) {
        dialog4 = DialogProvider.showEditTextDialog(this, EDIT_TITLE_DIALOG_TITLE, EDIT_TITLE_DIALOG_HINT, initTitle,
                (dialog, which, text) -> {
                    if (!TextUtils.isEmpty(text)) {
                        dialog.dismiss();
                        toolbar.setTitle(text);
                        presenter.onTitleChanged(text);
                    }
                });
    }

    @Override
    public void openEmailScreen(EmailContent.Builder builder) {
        builder.setBody(EMAIL_BODY).setSubject(EMAIL_SUBJECT);
        navigationComponent.navigator().openEmailScreen(this, builder.build());
    }

    @Override
    public void openPostCreateScreen(long postId) {
        navigationComponent.navigator().openPostCreateScreen(this, postId);
    }

    @Override
    public void openPostListScreen() {
        navigationComponent.navigator().openPostListScreen(this, postId);
    }

    // ------------------------------------------
    @Override
    public String getInputGroupsTitle() {
        return toolbar.getTitle().toString();
    }

    @Override
    public void setInputGroupsTitle(String title) {
        if (!TextUtils.isEmpty(title)) toolbar.setTitle(title);
    }

    @Override
    public void setCloseViewResult(int result) {
        setResult(result);
    }

    @Override
    public void setNewPostId(long postId) {
        this.postId = postId;
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
        showFab(false);
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
        showFab(isVisible);
    }

    @Override
    public void showPostingFailed() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.group_list_snackbar_posting_failed);
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
    public void hasReachedTop(boolean reached) {
        showFab(true);
    }

    @Override
    public void hasReachedBottom(boolean reached) {
        showFab(!reached);
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
        DUMP_FILE_DIALOG_TITLE = resources.getString(R.string.group_list_dialog_new_dump_file_title);
        DUMP_FILE_DIALOG_HINT = resources.getString(R.string.group_list_dialog_new_dump_file_hint);
        EMAIL_FILE_DIALOG_TITLE = resources.getString(R.string.dialog_send_email_title);
        EMAIL_FILE_DIALOG_HINT = resources.getString(R.string.dialog_send_email_hint);
        EMAIL_BODY = resources.getString(R.string.group_list_dump_file_email_body);
        EMAIL_SUBJECT = resources.getString(R.string.group_list_dump_file_email_subject);
        EDIT_TITLE_DIALOG_TITLE = resources.getString(R.string.dialog_input_edit_title);
        EDIT_TITLE_DIALOG_HINT = resources.getString(R.string.dialog_input_edit_title_hint);
        INFO_TITLE = resources.getString(R.string.group_list_selected_groups_total_count);
        GROUPS_DUMP_FILE_PREFIX = resources.getString(R.string.group_list_dump_file_prefix);
        SNACKBAR_DUMP_SUCCESS = resources.getString(R.string.group_list_snackbar_groups_dump_succeeded);
        SNACKBAR_KEYWORD_ALREADY_ADDED = resources.getString(R.string.group_list_error_already_added_keyword);
        SNACKBAR_KEYWORDS_LIMIT = resources.getString(R.string.group_list_snackbar_keywords_limit_message);
    }

    private void showFab(boolean isVisible) {
        if (isVisible) {
            fab.show();
            fabLabel.setVisibility(View.VISIBLE);
        } else {
            fab.hide();
            fabLabel.setVisibility(View.INVISIBLE);
        }
    }

    /* Showcase */
    // --------------------------------------------------------------------------------------------
    @Nullable
    private ShowcaseView runShowcase(@SingleShot.ShowCase int showcase) {
        // check single shot
        if (showcase != SingleShot.CASE_HIDE &&
            sharedPrefsManagerComponent.sharedPrefsManager().checkShowcaseSingleShot(showcase, SingleShot.GROUP_LIST_SCREEN)) {
            Timber.i("Showcase [%s] has already been fired on Main Screen", showcase);
            return null;
        }
        sharedPrefsManagerComponent.sharedPrefsManager().notifyShowcaseFired(showcase, SingleShot.GROUP_LIST_SCREEN);

        @StringRes int titleId = 0;
        @StringRes int descriptionId = 0;
        View target = null;

        boolean ok = false;
        switch (showcase) {
            case SingleShot.CASE_HIDE:
                if (showcaseView != null && showcaseView.isShowing()) showcaseView.hide();
                return null;
            case SingleShot.CASE_ADD_KEYWORD:
                titleId = R.string.group_list_showcase_add_keyword_title;
                descriptionId = R.string.group_list_showcase_add_keyword_description;
                target = addKeywordButton;
                ok = true;
                break;
            case SingleShot.CASE_SELECT_POST:
                titleId = R.string.group_list_showcase_select_post_title;
                target = postThumbnail;
                ok = true;
                break;
            case SingleShot.CASE_MAKE_WALL_POSTING:
                titleId = R.string.group_list_showcase_make_wall_posting;
                target = fabFrameLayout;
                ok = true;
                break;
        }

        if (ok && target != null) {
            if (showcaseView != null && showcaseView.isShowing()) showcaseView.hide();
            ShowcaseView sv = SingleShot.runShowcase(this, target, titleId, descriptionId, showcase,
                    SingleShot.GROUP_LIST_SCREEN, R.layout.custom_showcase_button, this);
            sv.setButtonPosition(SingleShot.moveButton(getResources()));
            return sv;
        }
        return null;
    }

    // ------------------------------------------
    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        UiUtility.dimViewCancel(fabFrameLayout);
        UiUtility.dimViewCancel(toolbar);
        UiUtility.dimViewCancel(frameLayout);
        UiUtility.dimViewCancel(addKeywordButton);
        UiUtility.dimViewCancel(changePostButton);
        UiUtility.dimViewCancel(postThumbnail);
        UiUtility.dimViewCancel(selectedGroupsCountView);
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
        UiUtility.dimView(toolbar);
        UiUtility.dimView(frameLayout);
        UiUtility.dimView(changePostButton);
        UiUtility.dimView(selectedGroupsCountView);

        SingleShot.ShowcaseTag tag = (SingleShot.ShowcaseTag) showcaseView.getTag();

        switch (tag.showcase()) {
            case SingleShot.CASE_ADD_KEYWORD:
                UiUtility.dimViewCancel(addKeywordButton);
                UiUtility.dimView(fabFrameLayout);
                UiUtility.dimView(postThumbnail);
                break;
            case SingleShot.CASE_SELECT_POST:
                UiUtility.dimView(addKeywordButton);
                UiUtility.dimView(fabFrameLayout);
                UiUtility.dimViewCancel(postThumbnail);
                break;
            case SingleShot.CASE_MAKE_WALL_POSTING:
                UiUtility.dimView(addKeywordButton);
                UiUtility.dimViewCancel(fabFrameLayout);
                UiUtility.dimView(postThumbnail);
                break;
        }
    }

    @Override
    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
    }
}
