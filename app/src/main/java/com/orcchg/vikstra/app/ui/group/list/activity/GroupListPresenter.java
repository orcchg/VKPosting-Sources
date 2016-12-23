package com.orcchg.vikstra.app.ui.group.list.activity;

import com.orcchg.vikstra.app.ui.base.BasePresenter;

import javax.inject.Inject;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    String title;  // TODO: set initial title

    @Inject
    GroupListPresenter() {
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onDumpPressed() {
        // TODO: dump found groups
    }

    @Override
    public void onTitleChanged(String text) {
        title = text;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        // TODO:
    }

    // TODO: assign title
    // TODO: call: getView().setInputGroupsTitle(title);
}
