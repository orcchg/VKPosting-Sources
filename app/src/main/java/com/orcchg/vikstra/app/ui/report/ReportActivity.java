package com.orcchg.vikstra.app.ui.report;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.permission.BasePermissionActivity;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.common.showcase.SingleShot;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.report.injection.DaggerReportComponent;
import com.orcchg.vikstra.app.ui.report.injection.ReportComponent;
import com.orcchg.vikstra.app.ui.report.injection.ReportModule;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ReportActivity extends BasePermissionActivity<ReportContract.View, ReportContract.Presenter>
        implements ReportContract.View, IScrollList, OnShowcaseEventListener {
    private static final String FRAGMENT_TAG = "report_fragment_tag";
    private static final String BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID = "bundle_key_group_report_bundle_id";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED = "bundle_key_flag_posting_revert_finished";
    private static final String EXTRA_GROUP_REPORT_BUNDLE_ID = "extra_group_report_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";

    private String DIALOG_TITLE, DIALOG_HINT, INFO_TITLE,
            SNACKBAR_DUMP_SUCCESS, SNACKBAR_POSTING_FINISHED;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.ll_container) ViewGroup container;
    @BindView(R.id.tv_info_title) TextView reportTextView;
    @BindView(R.id.report_indicator) ProgressBar reportIndicatorView;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.anchor_view) View achorView;
    @BindView(R.id.btn_posting_interrupt) Button interruptButton;
    @BindView(R.id.btn_posting_revert_all) Button revertAllButton;
    @OnClick(R.id.btn_posting_interrupt)
    void onInterruptPostingClick() {
        presenter.interruptPostingAndClose(false);  // don't close on interruption
        UiUtility.showSnackbar(this, R.string.report_snackbar_posting_interrupted);
    }
    @OnClick(R.id.btn_posting_revert_all)
    void onRevertAllPostingClick() {
        openRevertAllWarningDialog();
    }

    private ReportComponent reportComponent;
    private long groupReportBundleId = Constant.BAD_ID;  // if BAD_ID will not change later, then update reports interactively
    private long postId = Constant.BAD_ID;

    private boolean postingRevertFinished = false;

    private @Nullable ShowcaseView showcaseView;

    public static Intent getCallingIntent(@NonNull Context context, long groupReportBundleId, long postId) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    @NonNull @Override
    protected ReportContract.Presenter createPresenter() {
        return reportComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        reportComponent = DaggerReportComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postModule(new PostModule(postId))
                .reportModule(new ReportModule(groupReportBundleId, FileUtility.getDumpGroupReportsFileName(this, true /* external */)))
                .build();
        reportComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    @Override
    public void onBackPressed() {
        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            presenter.onCloseView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
        outState.putBoolean(BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED, postingRevertFinished);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            groupReportBundleId = savedInstanceState.getLong(BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
            postingRevertFinished = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED, false);
        } else {
            groupReportBundleId = getIntent().getLongExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
            postingRevertFinished = false;
        }
        Timber.d("GroupReportBundle id: %s ; Post id: %s", groupReportBundleId, postId);
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
        postThumbnail.setOnClickListener((view) -> navigationComponent.navigator().openPostViewScreen(this, postId, false));  // not editable PostViewScreen
        postThumbnail.setErrorRetryButtonClickListener((view) -> presenter.retryPost());
        updatePostedCounters(0, 0);

        if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
            interruptButton.setVisibility(View.VISIBLE);
            revertAllButton.setEnabled(false);
        } else {
            interruptButton.setVisibility(View.GONE);
            revertAllButton.setEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            ReportFragment fragment = ReportFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.report_screen_title);
        toolbar.setNavigationOnClickListener((view) -> onBackPressed());
        toolbar.inflateMenu(R.menu.dump);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.dump:
                    if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_HIDE);
                    askForPermission_writeExternalStorage();
                    return true;
            }
            return false;
        });
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAccessTokenExhausted() {
        navigationComponent.navigator().openAccessTokenExhaustedDialog(this);
    }

    // ------------------------------------------
    @Override
    public RecyclerView getListView(int tag) {
        ReportFragment fragment = getFragment();
        if (fragment != null) return fragment.getListView(tag);
        return null;
    }

    // ------------------------------------------
    @Override
    public void enableSwipeToRefresh(boolean isEnabled) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.enableSwipeToRefresh(isEnabled);
    }

    @Override
    public void enableButtonsOnPostingFinished() {
        interruptButton.setEnabled(false);
        revertAllButton.setEnabled(!postingRevertFinished);
    }

    // ------------------------------------------
    @Override
    public void onPostingCancel() {
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_DUMP_REPORT);
        DialogProvider.showTextDialog(this, R.string.dialog_warning_title,
                R.string.report_dialog_posting_was_cancelled_daily_limit_reached);
        enableButtonsOnPostingFinished();
    }

    @Override
    public void onPostingFinished(int posted, int total) {
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_DUMP_REPORT);
        String text = String.format(Locale.ENGLISH, SNACKBAR_POSTING_FINISHED, posted, total);
        DialogProvider.showTextDialog(this, R.string.report_dialog_posting_finished, text);
        enableButtonsOnPostingFinished();
    }

    @Override
    public void onPostRevertingStarted() {
        UiUtility.showSnackbar(this, R.string.report_snackbar_revert_all_wall_posting_started);
    }

    @Override
    public void onPostRevertingEmpty() {
        UiUtility.showSnackbar(this, R.string.report_snackbar_revert_all_wall_posting_empty);
    }

    @Override
    public void onPostRevertingError() {
        UiUtility.showSnackbar(this, R.string.snackbar_error_message);
    }

    @Override
    public void onPostRevertingFinished() {
        postingRevertFinished = true;
        revertAllButton.setEnabled(false);
        UiUtility.showSnackbar(this, R.string.report_snackbar_revert_all_wall_posting_finished);
    }

    // ------------------------------------------
    @Override
    public void openCloseWhilePostingDialog() {
        DialogProvider.showTextDialogTwoButtons(this, R.string.report_dialog_interrupt_posting_and_close_title,
                R.string.report_dialog_interrupt_posting_and_close_description,
                R.string.button_interrupt,R.string.button_continue,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.interruptPostingAndClose(false);  // don't close on interruption
                },
                (dialog, which) -> dialog.dismiss());
    }

    @Override
    public void openDumpNotReadyDialog() {
        DialogProvider.showTextDialog(this, R.string.dialog_warning_title,
                R.string.report_dialog_group_reports_not_ready_to_dump);
    }

    @Override
    public void openEditDumpFileNameDialog() {
        DialogProvider.showEditTextDialog(this, DIALOG_TITLE, DIALOG_HINT, "",
                (dialog, which, text) -> {
                    dialog.dismiss();
                    String path = FileUtility.makeDumpFileName(this, text, true /* external */);
                    presenter.performDumping(path);
                });
    }

    @Override
    public void openGroupDetailScreen(long groupId) {
        navigationComponent.navigator().openGroupDetailScreen(this, groupId);
    }

    @Override
    public void openRevertAllWarningDialog() {
        DialogProvider.showTextDialogTwoButtons(this, R.string.dialog_warning_title,
                R.string.report_dialog_revert_all_wall_posting_description,
                R.string.button_revert, R.string.button_cancel,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.performReverting();
                },
                (dialog, which) -> dialog.dismiss());
    }

    // ------------------------------------------
    @Override
    public void showDumpError() {
        UiUtility.showSnackbar(this, R.string.report_snackbar_group_reports_dump_failed, Snackbar.LENGTH_LONG);
    }

    @Override
    public void showDumpSuccess(String path) {
        UiUtility.showSnackbar(this, String.format(Locale.ENGLISH, SNACKBAR_DUMP_SUCCESS,
                FileUtility.refineExternalPath(path)), Snackbar.LENGTH_LONG);
    }

    @Override
    public void showEmptyPost() {
        postThumbnail.setPost(null);
    }

    @Override
    public void showErrorPost() {
        postThumbnail.showError(true);
    }

    @Override
    public void showPost(PostSingleGridItemVO viewObject) {
        postThumbnail.setPost(viewObject);
    }

    @Override
    public void updatePostedCounters(int posted, int total) {
        reportTextView.setText(String.format(Locale.ENGLISH, INFO_TITLE, posted, total));
        reportIndicatorView.setMax(total);
        reportIndicatorView.setProgress(posted);
    }

    // ------------------------------------------
    @Override
    public void closeView() {
        finish();
    }

    // ------------------------------------------
    @Override
    public void showGroupReports(boolean isEmpty) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.showGroupReports(isEmpty);
    }

    // ------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        ReportFragment fragment = getFragment();
        return fragment == null || fragment.isContentViewVisible(tag);
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        showGroupReports(isEmpty);
    }

    @Override
    public void showEmptyList(int tag) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.showEmptyList(tag);
    }

    @Override
    public void showError(int tag) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.showError(tag);
    }

    @Override
    public void showLoading(int tag) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.showLoading(tag);
    }

    // ------------------------------------------
    @Override
    public void retryList() {
        presenter.retry();
    }

    @Override
    public void onEmptyList() {
        // TODO: empty reports
    }

    @Override
    public void onScrollList(int itemsLeftToEnd) {
        presenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Nullable
    private ReportFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (ReportFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        DIALOG_TITLE = resources.getString(R.string.report_dialog_new_dump_file_title);
        DIALOG_HINT = resources.getString(R.string.report_dialog_new_dump_file_hint);
        INFO_TITLE = resources.getString(R.string.report_posted_counters);
        SNACKBAR_DUMP_SUCCESS = resources.getString(R.string.report_snackbar_group_reports_dump_succeeded);
        SNACKBAR_POSTING_FINISHED = resources.getString(R.string.report_snackbar_posting_finished);
    }

    /* Showcase */
    // --------------------------------------------------------------------------------------------
    @Nullable
    private ShowcaseView runShowcase(@SingleShot.ShowCase int showcase) {
        // check single shot
        if (showcase != SingleShot.CASE_HIDE &&
            sharedPrefsManagerComponent.sharedPrefsManager().checkShowcaseSingleShot(showcase, SingleShot.REPORT_SCREEN)) {
            Timber.i("Showcase [%s] has already been fired on Main Screen", showcase);
            return null;
        }
        sharedPrefsManagerComponent.sharedPrefsManager().notifyShowcaseFired(showcase, SingleShot.REPORT_SCREEN);

        @StringRes int titleId = 0;
        @StringRes int descriptionId = 0;
        View target = null;

        boolean ok = false;
        switch (showcase) {
            case SingleShot.CASE_HIDE:
                if (showcaseView != null && showcaseView.isShowing()) showcaseView.hide();
                return null;
            case SingleShot.CASE_DUMP_REPORT:
                titleId = R.string.report_showcase_report_dump_title;
                target = achorView;
                ok = true;
                break;
        }

        if (ok && target != null) {
            if (showcaseView != null && showcaseView.isShowing()) showcaseView.hide();
            return SingleShot.runShowcase(this, target, titleId, descriptionId, showcase,
                    SingleShot.REPORT_SCREEN, R.layout.custom_showcase_button, this);
        }
        return null;
    }

    // ------------------------------------------
    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        UiUtility.dimViewCancel(container);
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
        UiUtility.dimView(container);
    }

    @Override
    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
    }
}
