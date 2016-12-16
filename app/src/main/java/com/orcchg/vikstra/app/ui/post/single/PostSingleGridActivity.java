package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.post.single.injection.DaggerPostSingleGridComponent;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridComponent;

public class PostSingleGridActivity extends BaseActivity<PostSingleGridContract.View, PostSingleGridContract.Presenter>
        implements PostSingleGridContract.View {
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
        PostSingleGridFragment fragment = getFragment();
        if (fragment != null) fragment.showPosts(isEmpty);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private PostSingleGridFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (PostSingleGridFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    }
}
