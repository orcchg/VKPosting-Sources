package com.orcchg.vikstra.app.ui.post.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.post.OutConstants;
import com.orcchg.vikstra.app.ui.post.list.injection.DaggerPostListComponent;
import com.orcchg.vikstra.app.ui.post.list.injection.PostListComponent;
import com.orcchg.vikstra.app.ui.post.list.injection.PostListModule;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostListActivity extends BaseActivity<PostSingleGridContract.View, PostListContract.Presenter>
        implements PostListContract.View, IScrollGrid, ShadowHolder {
    private static final String FRAGMENT_TAG = "post_list_fragment_tag";
    private static final String BUNDLE_KEY_SELECTED_POST_ID = "bundle_key_selected_post_id";
    private static final String EXTRA_SELECTED_POST_ID = "extra_selected_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.POST_LIST_SCREEN;

    @BindView(R.id.coordinator_root) ViewGroup coordinatorRoot;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @BindView(R.id.container) ViewGroup container;
    @OnClick(R.id.fab)
    void onFabClick() {
        presenter.onSelectPressed();
    }

    private PostListComponent postListComponent;
    private long selectedPostId = Constant.BAD_ID;

    public static Intent getCallingIntent(@NonNull Context context) {
        return getCallingIntent(context, Constant.BAD_ID);
    }

    public static Intent getCallingIntent(@NonNull Context context, long selectedPostId) {
        Intent intent = new Intent(context, PostListActivity.class);
        intent.putExtra(EXTRA_SELECTED_POST_ID, selectedPostId);
        return intent;
    }

    @NonNull @Override
    protected PostListContract.Presenter createPresenter() {
        return postListComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postListComponent = DaggerPostListComponent.builder()
                .applicationComponent(getApplicationComponent())
                // items are selectable
                .postListModule(new PostListModule(BaseSelectAdapter.SELECT_MODE_SINGLE, selectedPostId))
                .build();
        postListComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_SELECTED_POST_ID, selectedPostId);
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedPostId = savedInstanceState.getLong(BUNDLE_KEY_SELECTED_POST_ID, Constant.BAD_ID);
        } else {
            selectedPostId = getIntent().getLongExtra(EXTRA_SELECTED_POST_ID, Constant.BAD_ID);
        }
        Timber.d("Post id: %s", selectedPostId);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_TAG) == null) {
            PostListFragment fragment = PostListFragment.newInstance();
            fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
            fm.executePendingTransactions();
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) container.getLayoutParams();
        int side = getResources().getDimensionPixelSize(R.dimen.post_list_item_spacing);
        params.setMargins(side, 0, side, 0);
        container.setLayoutParams(params);
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.post_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> closeView(Activity.RESULT_CANCELED, Constant.BAD_ID));
        toolbar.inflateMenu(R.menu.add_new);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.add_new:
                    openPostCreateScreen();
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
    public RecyclerView getListView(int tag) {
        PostListFragment fragment = getFragment();
        if (fragment != null) return fragment.getListView(tag);
        return null;
    }

    // ------------------------------------------
    @Override
    public void onPostNotSelected() {
        UiUtility.showSnackbar(coordinatorRoot, R.string.post_list_snackbar_post_is_empty_message);
    }

    // ------------------------------------------
    @Override
    public void closeView() {
        finish();  // with currently set result
    }

    @Override
    public void closeView(int resultCode, long postId) {
        Intent data = new Intent();
        data.putExtra(OutConstants.OUT_EXTRA_POST_ID, postId);
        setResult(resultCode, data);
        finish();
    }

    // ------------------------------------------
    @Override
    public void openPostCreateScreen() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void openPostViewScreen(long postId) {
        navigationComponent.navigator().openPostViewScreen(this, postId);
    }

    @Override
    public void showCreatePostFailure() {
        UiUtility.showSnackbar(this, R.string.post_single_grid_snackbar_failed_to_create_post);
    }

    // ------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        PostListFragment fragment = getFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    // ------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        PostListFragment fragment = getFragment();
        return fragment == null || fragment.isContentViewVisible(tag);
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        showPosts(isEmpty);
    }

    @Override
    public void showEmptyList(int tag) {
        PostListFragment fragment = getFragment();
        if (fragment != null) fragment.showEmptyList(tag);
    }

    @Override
    public void showError(int tag) {
        PostListFragment fragment = getFragment();
        if (fragment != null) fragment.showError(tag);
    }

    @Override
    public void showLoading(int tag) {
        PostListFragment fragment = getFragment();
        if (fragment != null) fragment.showLoading(tag);
    }

    // ------------------------------------------
    @Override
    public void retryGrid() {
        presenter.retry();
    }

    @Override
    public void onEmptyGrid() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void onScrollGrid(int itemsLeftToEnd) {
        presenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private PostListFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (PostListFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }
}
