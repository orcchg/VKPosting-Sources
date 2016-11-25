package com.orcchg.vikstra.app.ui.post.list;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.app.ui.base.BaseActivity;

public class PostListActivity extends BaseActivity<PostListContract.View, PostListContract.Presenter>
        implements PostListContract.View {

    @NonNull
    @Override
    protected PostListContract.Presenter createPresenter() {
        return null;
    }

    @Override
    protected void injectDependencies() {
        //
    }
}
