package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.content.IScrollGrid;
import com.orcchg.vikstra.app.ui.post.single.injection.DaggerPostSingleGridComponent;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridComponent;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;

public class PostSingleGridActivity extends BaseActivity<PostSingleGridContract.View, PostSingleGridContract.Presenter>
        implements PostSingleGridContract.View, IScrollGrid {
    private static final String FRAGMENT_TAG = "post_single_grid_fragment_tag";

    private PostSingleGridComponent postSingleGridComponent;

    @NonNull @Override
    protected PostSingleGridContract.Presenter createPresenter() {
        return postSingleGridComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postSingleGridComponent = DaggerPostSingleGridComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postSingleGridModule(new PostSingleGridModule(BaseSelectAdapter.SELECT_MODE_SINGLE))  // items are selectable
                .build();
        postSingleGridComponent.inject(this);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    // TODO: later migrate this Activity to PostListActivity

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public RecyclerView getListView(int tag) {
        PostSingleGridFragment fragment = getFragment();
        if (fragment != null) return fragment.getListView(tag);
        return null;
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

    // ------------------------------------------
    @Override
    public void showPosts(boolean isEmpty) {
        PostSingleGridFragment fragment = getFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    // ------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        PostSingleGridFragment fragment = getFragment();
        return fragment == null || fragment.isContentViewVisible(tag);
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        showPosts(isEmpty);
    }

    @Override
    public void showEmptyList(int tag) {
        PostSingleGridFragment fragment = getFragment();
        if (fragment != null) fragment.showEmptyList(tag);
    }

    @Override
    public void showError(int tag) {
        PostSingleGridFragment fragment = getFragment();
        if (fragment != null) fragment.showError(tag);
    }

    @Override
    public void showLoading(int tag) {
        PostSingleGridFragment fragment = getFragment();
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
    private PostSingleGridFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (PostSingleGridFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }
}
