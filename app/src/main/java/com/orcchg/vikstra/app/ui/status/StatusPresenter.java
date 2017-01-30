package com.orcchg.vikstra.app.ui.status;

import com.orcchg.vikstra.app.ui.base.BasePresenter;

import javax.inject.Inject;

public class StatusPresenter extends BasePresenter<StatusContract.View> implements StatusContract.Presenter {

    @Inject
    StatusPresenter() {
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    @Override
    protected void onRestoreState() {
    }
}
