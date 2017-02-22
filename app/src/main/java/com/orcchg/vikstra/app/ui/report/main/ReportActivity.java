package com.orcchg.vikstra.app.ui.report.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.orcchg.vikstra.app.ui.report.main.injection.DaggerReportComponent;
import com.orcchg.vikstra.app.ui.report.main.injection.ReportComponent;
import com.orcchg.vikstra.app.ui.report.main.injection.ReportModule;
import com.orcchg.vikstra.app.ui.report.service.WallPostingService;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.model.misc.PostingUnit;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import static com.orcchg.vikstra.R.id.fab;

public class ReportActivity extends BasePermissionActivity<ReportContract.View, ReportContract.Presenter>
        implements ReportContract.View, IScrollList, OnShowcaseEventListener {
    private static final String FRAGMENT_TAG = "report_fragment_tag";
    private static final String BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID = "bundle_key_group_report_bundle_id";
    private static final String BUNDLE_KEY_KEYWORD_BUNDLE_ID = "bundle_key_keyword_bundle_id";
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED = "bundle_key_flag_posting_revert_finished";
    private static final String BUNDLE_KEY_FLAG_HAS_FIRST_POSTING_UNIT_ARRIVED = "bundle_key_flag_has_first_posting_unit_arrived";
    private static final String BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE = "bundle_key_flag_is_interactive_mode";
    private static final String EXTRA_GROUP_REPORT_BUNDLE_ID = "extra_group_report_bundle_id";
    private static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    private static final String EXTRA_IS_INTERACTIVE_MODE = "extra_is_interactive_mode";
    public static final int REQUEST_CODE = Constant.RequestCode.REPORT_SCREEN;

    private static boolean isAlive = true;

    private String DUMP_FILE_DIALOG_TITLE, DUMP_FILE_DIALOG_HINT,
            EMAIL_FILE_DIALOG_TITLE, EMAIL_FILE_DIALOG_HINT, EMAIL_BODY, EMAIL_SUBJECT,
            INFO_TITLE, REPORTS_DUMP_FILE_PREFIX,
            SNACKBAR_DUMP_SUCCESS, SNACKBAR_POSTING_FINISHED;

    private @ColorInt int FAB_NORMAL_COLOR, FAB_NORMAL_RIPPLE_COLOR, FAB_PAUSE_COLOR, FAB_PAUSE_RIPPLE_COLOR;

    @BindView(R.id.coordinator_root) ViewGroup coordinatorRoot;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.ll_container) ViewGroup container;
    @BindView(R.id.tv_info_title) TextView reportTextView;
    @BindView(R.id.report_indicator) ProgressBar reportIndicatorView;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.anchor_view) View achorView;
    @BindView(R.id.btn_posting_interrupt) Button interruptButton;
    @BindView(R.id.btn_posting_revert_all) Button revertAllButton;
    @BindView(fab) FloatingActionButton fabSuspend;
    @OnClick(R.id.btn_posting_interrupt)
    void onInterruptPostingClick() {
        if (hasFirstPostingUnitArrived) {
            presenter.interruptPostingAndClose(false);  // don't close on interruption
            UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_posting_interrupted);
        } else {
            UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_posting_interrupt_disallowed);
        }
    }
    @OnClick(R.id.btn_posting_revert_all)
    void onRevertAllPostingClick() {
        openRevertAllWarningDialog();
    }
    @OnClick(fab)
    void onSuspendClick() {
        presenter.onSuspendClick();
    }

    private ReportComponent reportComponent;
    private long groupReportBundleId = Constant.BAD_ID;  // if BAD_ID will not change later, then update reports interactively
    private long keywordBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    private boolean hasFirstPostingUnitArrived = false;
    private boolean isInteractiveMode = true;
    private boolean postingRevertFinished = false;

    private @Nullable ShowcaseView showcaseView;

    private @Nullable AlertDialog dialog1, dialog2, dialog3, dialog4, dialog5, dialog6, dialog7;

    public static Intent getCallingIntent(@NonNull Context context, long groupReportBundleId,
                                          long keywordBundleId, long postId) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBundleId);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    public static Intent getCallingIntentNoInteractive(@NonNull Context context, long groupReportBundleId,
                                                       long keywordBundleId, long postId) {
        Intent intent = getCallingIntent(context, groupReportBundleId, keywordBundleId, postId);
        intent.putExtra(EXTRA_IS_INTERACTIVE_MODE, false);
        return intent;
    }

    public static boolean isAlive() {
        return isAlive;
    }

    @NonNull @Override
    protected ReportContract.Presenter createPresenter() {
        return reportComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        ReportModule reportModule = new ReportModule(groupReportBundleId, keywordBundleId,
                FileUtility.getDumpGroupReportsFileName(this, true /* external */), isInteractiveMode);
        reportComponent = DaggerReportComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postModule(new PostModule(postId))
                .reportModule(reportModule)
                .build();
        reportComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isAlive = true;
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        checkForAccessToken();
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();

        if (isInteractiveMode()) {
            Timber.d("Subscribe on posting progress callback on ReportScreen");
            IntentFilter filterCancel = new IntentFilter(Constant.Broadcast.WALL_POSTING_CANCELLED);
            IntentFilter filterFinish = new IntentFilter(Constant.Broadcast.WALL_POSTING_FINISHED);
            IntentFilter filterResult = new IntentFilter(Constant.Broadcast.WALL_POSTING_PROGRESS_UNIT);
            LocalBroadcastManager.getInstance(this).registerReceiver(receiverCancel, filterCancel);
            LocalBroadcastManager.getInstance(this).registerReceiver(receiverFinish, filterFinish);
            LocalBroadcastManager.getInstance(this).registerReceiver(receiverResult, filterResult);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.i("onNewIntent");
        checkForAccessToken();
    }

    @Override
    public void onBackPressed() {
        if (isInteractiveMode()) {
            presenter.onCloseView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        outState.putLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, keywordBundleId);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
        outState.putBoolean(BUNDLE_KEY_FLAG_HAS_FIRST_POSTING_UNIT_ARRIVED, hasFirstPostingUnitArrived);
        outState.putBoolean(BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE, isInteractiveMode);
        outState.putBoolean(BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED, postingRevertFinished);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isInteractiveMode()) {
            Timber.d("Unsubscribe from posting progress callback on ReportScreen");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCancel);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinish);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);

            Timber.d("notify Activity destroyed to Service");
            Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_SCREEN_DESTROY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        if (dialog1 != null) dialog1.dismiss();
        if (dialog2 != null) dialog2.dismiss();
        if (dialog3 != null) dialog3.dismiss();
        if (dialog4 != null) dialog4.dismiss();
        if (dialog5 != null) dialog5.dismiss();
        if (dialog6 != null) dialog6.dismiss();
        if (dialog7 != null) dialog7.dismiss();

        isAlive = false;
    }

    /* Broadcast receiver */
    // --------------------------------------------------------------------------------------------
    private BroadcastReceiver receiverCancel = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            long groupReportBundleId = intent.getLongExtra(WallPostingService.OUT_EXTRA_WALL_POSTING_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            int apiErrorCode = intent.getIntExtra(WallPostingService.OUT_EXTRA_WALL_POSTING_CANCEL_REASON_CODE, 0);
            presenter.onPostingCancel(apiErrorCode, groupReportBundleId);
        }
    };

    private BroadcastReceiver receiverFinish = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            long groupReportBundleId = intent.getLongExtra(WallPostingService.OUT_EXTRA_WALL_POSTING_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            presenter.onPostingFinish(groupReportBundleId);
        }
    };

    private BroadcastReceiver receiverResult = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            hasFirstPostingUnitArrived = true;  // enable 'interrupt' button as first result arrives.
            PostingUnit postingUnit = intent.getParcelableExtra(WallPostingService.OUT_EXTRA_WALL_POSTING_PROGRESS_UNIT);
            presenter.onPostingProgress(postingUnit);
        }
    };

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Timber.d("Restore state");
            groupReportBundleId = savedInstanceState.getLong(BUNDLE_KEY_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            keywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
            hasFirstPostingUnitArrived = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_HAS_FIRST_POSTING_UNIT_ARRIVED, false);
            isInteractiveMode = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE, true);
            postingRevertFinished = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_POSTING_REVERT_FINISHED, false);
        } else {
            groupReportBundleId = getIntent().getLongExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
            keywordBundleId = getIntent().getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
            hasFirstPostingUnitArrived = false;
            isInteractiveMode = getIntent().getBooleanExtra(EXTRA_IS_INTERACTIVE_MODE, true);
            postingRevertFinished = false;
        }
        Timber.d("GroupReportBundle id: %s ; KeywordBundle id: %s ; Post id: %s ; isInteractiveMode: %s",
                groupReportBundleId, keywordBundleId, postId, isInteractiveMode);
    }

    private boolean isInteractiveMode() {
        return isInteractiveMode;
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

        if (isInteractiveMode()) {
            styleFabSuspend(false);  // pause icon
            showFab(true);
            interruptButton.setVisibility(View.VISIBLE);
            revertAllButton.setEnabled(false);
        } else {
            showFab(false);
            interruptButton.setVisibility(View.GONE);
            revertAllButton.setEnabled(true);
        }

        if (isStateRestored()) {
            showFab(false);  // don't show fab when state restore regardless whether in interactive mode or not
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
        switch (AppConfig.INSTANCE.sendDumpFilesVia()) {
            case AppConfig.SEND_DUMP_FILE:
                toolbar.inflateMenu(R.menu.dump);
                break;
            case AppConfig.SEND_DUMP_EMAIL:
                toolbar.inflateMenu(R.menu.send);
                break;
            case AppConfig.SEND_DUMP_SHARE:
                toolbar.inflateMenu(R.menu.share);
                break;
        }
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.dump:
                case R.id.send:
                case R.id.share:
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

    // ------------------------------------------
    @Override
    public String getDumpFilename() {
        return FileUtility.makeDumpFileName(this, REPORTS_DUMP_FILE_PREFIX, true /* external */, true /* with timestamp */);
    }

    // ------------------------------------------
    @Override
    public void onPostingCancel() {
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_DUMP_REPORT);
        dialog1 = DialogProvider.showTextDialog(this, R.string.dialog_warning_title, R.string.report_dialog_posting_was_cancelled_daily_limit_reached);
        enableButtonsOnPostingFinished();
        showFab(false);
    }

    @Override
    public void onPostingFinished(int posted, int total) {
        if (AppConfig.INSTANCE.useTutorialShowcases()) showcaseView = runShowcase(SingleShot.CASE_DUMP_REPORT);
        String text = String.format(Locale.ENGLISH, SNACKBAR_POSTING_FINISHED, posted, total);
        dialog2 = DialogProvider.showTextDialog(this, R.string.report_dialog_posting_finished, text);
        enableButtonsOnPostingFinished();
        showFab(false);
    }

    @Override
    public void onPostRevertingStarted() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_revert_all_wall_posting_started);
    }

    @Override
    public void onPostRevertingEmpty() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_revert_all_wall_posting_empty);
    }

    @Override
    public void onPostRevertingError() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.snackbar_error_message);
    }

    @Override
    public void onPostRevertingFinished() {
        postingRevertFinished = true;
        revertAllButton.setEnabled(false);
        UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_revert_all_wall_posting_finished);
    }

    @DebugLog @Override
    public void onWallPostingInterrupt() {
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_INTERRUPT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @DebugLog @Override
    public void onWallPostingSuspend(boolean paused) {
        if (isInteractiveMode()) {
            if (paused) {
                UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_posting_paused);
            } else {
                UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_posting_resumed);
            }
            styleFabSuspend(paused);
            Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_SUSPEND);
            intent.putExtra(Constant.Broadcast.WALL_POSTING_SUSPEND, paused);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    // ------------------------------------------
    @Override
    public void openCloseWhilePostingDialog() {
        dialog3 = DialogProvider.showTextDialogTwoButtons(this, R.string.report_dialog_interrupt_posting_and_close_title,
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
        dialog4 = DialogProvider.showTextDialog(this, R.string.dialog_warning_title,
                R.string.report_dialog_group_reports_not_ready_to_dump);
    }

    @Override
    public void openEditDumpFileNameDialog() {
        dialog5 = DialogProvider.showEditTextDialog(this, DUMP_FILE_DIALOG_TITLE, DUMP_FILE_DIALOG_HINT, "",
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
        dialog7 = DialogProvider.showEditTextDialog(this, EMAIL_FILE_DIALOG_TITLE, EMAIL_FILE_DIALOG_HINT, "",
                (dialog, which, email) -> {
                    if (!TextUtils.isEmpty(email)) {
                        dialog.dismiss();
                        String path = getDumpFilename();
                        presenter.performDumping(path, email);
                    }
                });
    }

    @Override
    public void openEmailScreen(EmailContent.Builder builder) {
        builder.setBody(EMAIL_BODY).setSubject(EMAIL_SUBJECT);
        navigationComponent.navigator().openEmailScreen(this, builder.build());
    }

    @Override
    public void openGroupDetailScreen(long groupId) {
        navigationComponent.navigator().openGroupDetailScreen(this, groupId);
    }

    @Override
    public void openRevertAllWarningDialog() {
        dialog6 = DialogProvider.showTextDialogTwoButtons(this, R.string.dialog_warning_title,
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
        UiUtility.showSnackbar(coordinatorRoot, R.string.report_snackbar_group_reports_dump_failed, Snackbar.LENGTH_LONG);
    }

    @Override
    public void showDumpSuccess(String path) {
        UiUtility.showSnackbar(coordinatorRoot, String.format(Locale.ENGLISH, SNACKBAR_DUMP_SUCCESS,
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
    private void checkForAccessToken() {
        if (EndpointUtility.hasAccessTokenExhausted()) {
            Timber.w("Access Token has exhausted !");
            onAccessTokenExhausted();
        }
    }

    @Nullable
    private ReportFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (ReportFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }

    private void enableButtonsOnPostingFinished() {
        interruptButton.setEnabled(false);
        revertAllButton.setEnabled(!postingRevertFinished);
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        Resources resources = getResources();
        DUMP_FILE_DIALOG_TITLE = resources.getString(R.string.report_dialog_new_dump_file_title);
        DUMP_FILE_DIALOG_HINT = resources.getString(R.string.report_dialog_new_dump_file_hint);
        EMAIL_FILE_DIALOG_TITLE = resources.getString(R.string.dialog_send_email_title);
        EMAIL_FILE_DIALOG_HINT = resources.getString(R.string.dialog_send_email_hint);
        EMAIL_BODY = resources.getString(R.string.report_dump_file_email_body);
        EMAIL_SUBJECT = resources.getString(R.string.report_dump_file_email_subject);
        INFO_TITLE = resources.getString(R.string.report_posted_counters);
        REPORTS_DUMP_FILE_PREFIX = resources.getString(R.string.report_dump_file_prefix);
        SNACKBAR_DUMP_SUCCESS = resources.getString(R.string.report_snackbar_group_reports_dump_succeeded);
        SNACKBAR_POSTING_FINISHED = resources.getString(R.string.report_snackbar_posting_finished);

        FAB_NORMAL_COLOR = resources.getColor(R.color.report_fab_normal_color);
        FAB_NORMAL_RIPPLE_COLOR = resources.getColor(R.color.report_fab_normal_ripple_color);
        FAB_PAUSE_COLOR = resources.getColor(R.color.report_fab_pause_color);
        FAB_PAUSE_RIPPLE_COLOR = resources.getColor(R.color.report_fab_pause_ripple_color);
    }

    private void styleFabSuspend(boolean paused) {
        if (paused) {
            fabSuspend.setBackgroundTintList(ColorStateList.valueOf(FAB_NORMAL_COLOR));
            fabSuspend.setRippleColor(FAB_NORMAL_RIPPLE_COLOR);
            fabSuspend.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        } else {
            fabSuspend.setBackgroundTintList(ColorStateList.valueOf(FAB_PAUSE_COLOR));
            fabSuspend.setRippleColor(FAB_PAUSE_RIPPLE_COLOR);
            fabSuspend.setImageResource(R.drawable.ic_pause_white_24dp);
        }
    }

    private void showFab(boolean isVisible) {
        if (isVisible) {
            fabSuspend.show();
        } else {
            fabSuspend.hide();
        }
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
