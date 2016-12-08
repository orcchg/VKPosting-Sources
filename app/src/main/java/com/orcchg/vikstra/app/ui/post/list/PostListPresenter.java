package com.orcchg.vikstra.app.ui.post.list;

import com.orcchg.vikstra.app.ui.base.BasePresenter;

import javax.inject.Inject;

public class PostListPresenter extends BasePresenter<PostListContract.View> implements PostListContract.Presenter {

    @Inject
    PostListPresenter() {
    }

    @Override
    protected void freshStart() {
    }
}
