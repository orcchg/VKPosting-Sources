package com.orcchg.vikstra.app.ui.post.create;

import com.orcchg.vikstra.app.ui.base.BasePresenter;

import javax.inject.Inject;

public class PostCreatePresenter extends BasePresenter<PostCreateContract.View> implements PostCreateContract.Presenter {

    @Inject
    PostCreatePresenter() {
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttachPressed() {
        //
    }

    @Override
    public void onLocationPressed() {
        //
    }

    @Override
    public void onMediaPressed() {
        if (isViewAttached()) getView().addMedia();
    }

    @Override
    public void onPollPressed() {
        //
    }

    @Override
    public void onSavePressed() {
        // TODO: impl
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
}
