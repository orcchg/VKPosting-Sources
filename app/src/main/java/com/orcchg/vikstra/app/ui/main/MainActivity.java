package com.orcchg.vikstra.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.common.notification.PhotoUploadNotification;
import com.orcchg.vikstra.app.ui.common.notification.PostingNotification;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListFragment;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListModule;
import com.orcchg.vikstra.app.ui.main.injection.DaggerMainComponent;
import com.orcchg.vikstra.app.ui.main.injection.MainComponent;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridFragment;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainContract.View, MainContract.Presenter>
        implements MainContract.View, IScrollGrid, IScrollList {
    private static final String POST_GRID_FRAGMENT_TAG = "post_grid_fragment_tag";
    private static final String KEYW_LIST_FRAGMENT_TAG = "keyw_list_fragment_tag";

    @BindView(R.id.tv_groups_selection_counter) TextView selectedGroupsTextView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @OnClick(R.id.fab)
    void onFabClick() {
        presenter.onFabClick();
    }
    @OnClick(R.id.ibtn_see_all_keywords)
    void onAddNewGroupsClick() {
        navigationComponent.navigator().openKeywordListScreen(this);
    }
    @OnClick(R.id.btn_new_keywords)
    public void onNewKeywordsClick() {
        navigationComponent.navigator().openKeywordCreateScreen(this);
    }

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;

    private MainComponent mainComponent;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @NonNull @Override
    protected MainContract.Presenter createPresenter() {
        return mainComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        mainComponent = DaggerMainComponent.builder()
                .applicationComponent(getApplicationComponent())
                .groupListModule(new GroupListModule(Constant.BAD_ID))  // proper id will be set later
                .keywordListModule(new KeywordListModule(BaseSelectAdapter.SELECT_MODE_SINGLE))  // items are selectable
                .postModule(new PostModule(Constant.BAD_ID))  // proper id will be set later
                .postSingleGridModule(new PostSingleGridModule(BaseSelectAdapter.SELECT_MODE_SINGLE))  // items are selectable
                .build();
        mainComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initNotifications();
// TODO: page-proofs for loader dialog
//        navigationComponent.navigator().openStatusDialog(getSupportFragmentManager(), "tag");
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        fab.hide();  // hide fab at fresh start because nothing has been selected yet

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(POST_GRID_FRAGMENT_TAG) == null) {
            PostSingleGridFragment fragment = PostSingleGridFragment.newInstance();
            fm.beginTransaction().replace(R.id.fl_top, fragment, POST_GRID_FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
        if (fm.findFragmentByTag(KEYW_LIST_FRAGMENT_TAG) == null) {
            KeywordListFragment fragment = KeywordListFragment.newInstance();
            fm.beginTransaction().replace(R.id.fl_bottom, fragment, KEYW_LIST_FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public RecyclerView getListView(int tag) {
        MvpListView fragment = null;
        switch (tag) {
            case KeywordListFragment.RV_TAG:     fragment = getKeywordListFragment();  break;
            case PostSingleGridFragment.RV_TAG:  fragment = getPostGridFragment();     break;
        }
        if (fragment != null) return fragment.getListView(tag);
        return null;
    }

    @Override
    public void openGroupListScreen(long keywordBundleId, long postId) {
        navigationComponent.navigator().openGroupListScreen(this, keywordBundleId, postId);
    }

    @Override
    public void openKeywordCreateScreen(long keywordBundleId) {
        navigationComponent.navigator().openKeywordCreateScreen(this, keywordBundleId);
    }

    @Override
    public void openPostCreateScreen() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void openPostViewScreen(long postId) {
        navigationComponent.navigator().openPostViewScreen(this, postId);
    }

    @Override
    public void openReportScreen(long groupReportBundleId, long postId) {
        navigationComponent.navigator().openReportScreen(this, groupReportBundleId, postId);
    }

    @Override
    public void setCloseViewResult(int result) {
        setResult(result);
    }

    @Override
    public void showFab(boolean isVisible) {
        if (isVisible) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @Override
    public void showKeywords(boolean isEmpty) {
        KeywordListFragment fragment = getKeywordListFragment();
        if (fragment != null) fragment.showKeywords(isEmpty);
    }

    @Override
    public void showPosts(boolean isEmpty) {
        PostSingleGridFragment fragment = getPostGridFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    // ------------------------------------------
    @Override
    public void showContent(int tag, boolean isEmpty) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                KeywordListFragment fragment = getKeywordListFragment();
                if (fragment != null) fragment.showContent(tag, isEmpty);
                break;
            case PostSingleGridFragment.RV_TAG:
                PostSingleGridFragment fragment1 = getPostGridFragment();
                if (fragment1 != null) fragment1.showContent(tag, isEmpty);
                break;
        }
    }

    @Override
    public void showEmptyList(int tag) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                KeywordListFragment fragment = getKeywordListFragment();
                if (fragment != null) fragment.showEmptyList(tag);
                break;
            case PostSingleGridFragment.RV_TAG:
                PostSingleGridFragment fragment1 = getPostGridFragment();
                if (fragment1 != null) fragment1.showEmptyList(tag);
                break;
        }
    }

    @Override
    public void showError(int tag) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                KeywordListFragment fragment = getKeywordListFragment();
                if (fragment != null) fragment.showError(tag);
                break;
            case PostSingleGridFragment.RV_TAG:
                PostSingleGridFragment fragment1 = getPostGridFragment();
                if (fragment1 != null) fragment1.showError(tag);
                break;
        }
    }

    @Override
    public void showLoading(int tag) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                KeywordListFragment fragment = getKeywordListFragment();
                if (fragment != null) fragment.showLoading(tag);
                break;
            case PostSingleGridFragment.RV_TAG:
                PostSingleGridFragment fragment1 = getPostGridFragment();
                if (fragment1 != null) fragment1.showLoading(tag);
                break;
        }
    }

    // ------------------------------------------
    @Override
    public void retryList() {
        presenter.retryKeywords();
    }

    @Override
    public void onEmptyList() {
        navigationComponent.navigator().openKeywordCreateScreen(this);
    }

    @Override
    public void onScrollList(int itemsLeftToEnd) {
        presenter.onScrollKeywordsList(itemsLeftToEnd);
    }

    // ------------------------------------------
    @Override
    public void retryGrid() {
        presenter.retryPosts();
    }

    @Override
    public void onEmptyGrid() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void onScrollGrid(int itemsLeftToEnd) {
        presenter.onScrollPostsGrid(itemsLeftToEnd);
    }

    /* Notification delegate */
    // --------------------------------------------------------------------------------------------
    private void initNotifications() {
        postingNotification = new PostingNotification(this);
        photoUploadNotification = new PhotoUploadNotification(this);
    }

    @Override
    public void updateGroupReportBundleId(long groupReportBundleId) {
        postingNotification.updateGroupReportBundleId(this, groupReportBundleId);
    }

    @Override
    public void updatePostId(long postId) {
        postingNotification.updatePostId(this, postId);
    }

    // ------------------------------------------
    @Override
    public void onPostingProgress(int progress, int total) {
        postingNotification.onPostingProgress(progress, total);
    }

    @Override
    public void onPostingProgressInfinite() {
        postingNotification.onPostingProgressInfinite();
    }

    @Override
    public void onPostingComplete() {
        postingNotification.onPostingComplete();
    }

    // ------------------------------------------
    @Override
    public void onPhotoUploadProgress(int progress, int total) {
        photoUploadNotification.onPhotoUploadProgress(progress, total);
    }

    @Override
    public void onPhotoUploadProgressInfinite() {
        photoUploadNotification.onPhotoUploadProgressInfinite();
    }

    @Override
    public void onPhotoUploadComplete() {
        photoUploadNotification.onPhotoUploadComplete();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Nullable
    private KeywordListFragment getKeywordListFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (KeywordListFragment) fm.findFragmentByTag(KEYW_LIST_FRAGMENT_TAG);
    }

    @Nullable
    private PostSingleGridFragment getPostGridFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (PostSingleGridFragment) fm.findFragmentByTag(POST_GRID_FRAGMENT_TAG);
    }
}
