package com.orcchg.vikstra.app.ui.post.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.post.list.injection.DaggerPostListComponent;
import com.orcchg.vikstra.app.ui.post.list.injection.PostListComponent;
import com.orcchg.vikstra.app.ui.post.list.injection.PostListModule;
import com.orcchg.vikstra.app.ui.util.ShadowHolder;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostListActivity extends BaseActivity<PostListContract.View, PostListContract.Presenter>
        implements PostListContract.View, IScrollGrid, ShadowHolder {
    private static final String FRAGMENT_TAG = "post_list_fragment_tag";
    public static final int REQUEST_CODE = Constant.RequestCode.POST_LIST_SCREEN;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rl_toolbar_dropshadow) View dropshadowView;
    @OnClick(R.id.fab)
    void onFabClick() {
        openPostCreateScreen();
    }

    private PostListComponent postListComponent;

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, PostListActivity.class);
    }

    @NonNull @Override
    protected PostListContract.Presenter createPresenter() {
        return postListComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postListComponent = DaggerPostListComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postListModule(new PostListModule(BaseSelectAdapter.SELECT_MODE_NONE))  // items aren't selectable
                .build();
        postListComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_list);
        ButterKnife.bind(this);
        initView();
        initToolbar();
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
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.post_list_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());  // close screen with current result
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

    @Override
    public void openPostCreateScreen() {
        navigationComponent.navigator().openPostCreateScreen(this);
    }

    @Override
    public void openPostViewScreen(long postId) {
        navigationComponent.navigator().openPostViewScreen(this, postId);
    }

    @Override
    public void showPosts(boolean isEmpty) {
        PostListFragment fragment = getFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    // ------------------------------------------
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
