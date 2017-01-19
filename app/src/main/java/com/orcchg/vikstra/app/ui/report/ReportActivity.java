package com.orcchg.vikstra.app.ui.report;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.report.injection.DaggerReportComponent;
import com.orcchg.vikstra.app.ui.report.injection.ReportComponent;
import com.orcchg.vikstra.app.ui.report.injection.ReportModule;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportActivity extends BaseActivity<ReportContract.View, ReportContract.Presenter>
        implements ReportContract.View, IScrollList {
    private static final String FRAGMENT_TAG = "report_fragment_tag";
    private static final String EXTRA_GROUP_REPORT_BUNDLE_ID = "extra_group_report_bundle_id";
    private static final String EXTRA_POST_ID = "extra_post_id";

    private String INFO_TITLE;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_info_title) TextView reportTextView;
    @BindView(R.id.report_indicator) ProgressBar reportIndicatorView;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;

    private ReportComponent reportComponent;
    private long groupReportBundleId = Constant.BAD_ID;  // if BAD_ID will not change later, then update reports interactively
    private long postId = Constant.BAD_ID;

    @NonNull @Override
    protected ReportContract.Presenter createPresenter() {
        return reportComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        reportComponent = DaggerReportComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postModule(new PostModule(postId))
                .reportModule(new ReportModule(groupReportBundleId, ContentUtility.getDumpGroupReportsFileName(this)))
                .build();
        reportComponent.inject(this);
    }

    public static Intent getCallingIntent(@NonNull Context context, long groupReportBundleId, long postId) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        initResources();
        initView();
        initToolbar();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        groupReportBundleId = getIntent().getLongExtra(EXTRA_GROUP_REPORT_BUNDLE_ID, Constant.BAD_ID);
        postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        postThumbnail.setOnClickListener((view) -> navigationComponent.navigator().openPostViewScreen(this, postId));
        updatePostedCounters(0, 0);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            ReportFragment fragment = ReportFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.report_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.dump);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.dump:
                    presenter.onDumpPressed();
                    return true;
            }
            return false;
        });
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public RecyclerView getListView(int tag) {
        ReportFragment fragment = getFragment();
        if (fragment != null) return fragment.getListView(tag);
        return null;
    }

    @Override
    public void showGroupReports(boolean isEmpty) {
        ReportFragment fragment = getFragment();
        if (fragment != null) fragment.showGroupReports(isEmpty);
    }

    @Override
    public void showEmptyPost() {
        postThumbnail.setPost(null);
    }

    @Override
    public void showPost(PostSingleGridItemVO viewObject) {
        postThumbnail.setPost(viewObject);
    }

    @Override
    public void updatePostedCounters(int posted, int total) {
        reportTextView.setText(String.format(INFO_TITLE, posted, total));
        reportIndicatorView.setMax(total);
        reportIndicatorView.setProgress(posted);
    }

    // ------------------------------------------
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
        INFO_TITLE = resources.getString(R.string.report_posted_counters);
    }
}
