package com.orcchg.vikstra.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.common.content.IScrollList;
import com.orcchg.vikstra.app.ui.common.content.ISwipeToDismiss;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.common.notification.PhotoUploadNotification;
import com.orcchg.vikstra.app.ui.common.notification.PostingNotification;
import com.orcchg.vikstra.app.ui.common.view.AvatarMenuItem;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListFragment;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListModule;
import com.orcchg.vikstra.app.ui.main.injection.DaggerMainComponent;
import com.orcchg.vikstra.app.ui.main.injection.MainComponent;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridFragment;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.app.ui.viewobject.UserVO;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<MainContract.View, MainContract.Presenter>
        implements MainContract.View, IScrollGrid, IScrollList, ISwipeToDismiss, ShadowHolder {
    private static final String POST_GRID_FRAGMENT_TAG = "post_grid_fragment_tag";
    private static final String KEYW_LIST_FRAGMENT_TAG = "keyw_list_fragment_tag";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
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
        navigationComponent.navigator().openGroupListScreen(this);
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
        initToolbar();
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

    private void initToolbar() {
        toolbar.setTitle(R.string.main_screen_title);
        toolbar.setNavigationIcon(null);  // no navigation back from MainScreen
        toolbar.inflateMenu(R.menu.avatar);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
//                case R.id.about:
//                    openAboutDialog();
//                    return true;
                case R.id.logout:
                    openLogoutDialog();
                    return true;
            }
            return false;
        });

        AvatarMenuItem view = (AvatarMenuItem) toolbar.getMenu().findItem(R.id.avatar).getActionView();
        if (view != null) view.setOnClickListener((xview) -> openLogoutDialog());
    }

    @Override
    public void showShadow(boolean show) {
        dropshadowView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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

    // ------------------------------------------
    @Override
    public void onLoggedOut() {
        navigationComponent.navigator().openStartScreen(this);
        finish();
    }

    // ------------------------------------------
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
    public void showCreatePostFailure() {
        UiUtility.showSnackbar(this, R.string.post_single_grid_snackbar_failed_to_create_post);
    }

    @Override
    public void showCurrentUser(@Nullable UserVO viewObject) {
        AvatarMenuItem view = (AvatarMenuItem) toolbar.getMenu().findItem(R.id.avatar).getActionView();
        view.setImage(viewObject != null ? viewObject.photoUrl() : "");
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
    public boolean isContentViewVisible(int tag) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                KeywordListFragment fragment = getKeywordListFragment();
                if (fragment != null) return fragment.isContentViewVisible(tag);
            case PostSingleGridFragment.RV_TAG:
                PostSingleGridFragment fragment1 = getPostGridFragment();
                if (fragment1 != null) return fragment1.isContentViewVisible(tag);
        }
        return true;  // must be unreachable state
    }

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

    // ------------------------------------------
    @Override
    public void onSwipeToDismiss(int position, int tag) {
        switch (tag) {
            case KeywordListFragment.RV_TAG:
                presenter.removeKeywordListItem(position);;
                break;
            case PostSingleGridFragment.RV_TAG:
                presenter.removePostGridItem(position);;
                break;
        }
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

    private void openAboutDialog() {
        DialogProvider.showTextDialog(this, R.string.main_dialog_about_title, R.string.main_dialog_about_description);
    }

    private void openLogoutDialog() {
        DialogProvider.showTextDialogTwoButtons(this, R.string.main_dialog_logout_title,
                R.string.main_dialog_logout_description, R.string.button_logout, R.string.button_cancel,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.logout();
                },
                (dialog, which) -> dialog.dismiss());
    }
}
