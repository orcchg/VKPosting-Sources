package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.BasePresenter;

import javax.inject.Inject;

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {

    @Inject
    MainPresenter() {
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void retry() {
        // TODO
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        // TODO
    }
}
