package com.orcchg.vikstra.app.ui.group.list.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;

import javax.inject.Inject;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private String title;  // TODO: set initial title

    private GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter() {
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediatorComponent = DaggerGroupListMediatorComponent.builder()
                .groupListMediatorModule(new GroupListMediatorModule())
                .build();
        mediatorComponent.inject(this);
        mediatorComponent.mediator().attachFirst(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediatorComponent.mediator().detachFirst();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addKeyword(Keyword keyword) {
        sendAddKeywordRequest(keyword);
    }

    @Override
    public void onDumpPressed() {
        // TODO: dump found groups
    }

    @Override
    public void onFabClick() {
        sendPostToGroupsRequest();
    }

    @Override
    public void onTitleChanged(String text) {
        title = text;
    }

    /* Mediator */
    // ------------------------------------------
    @Override
    public void receiveEmptyPost() {
        if (isViewAttached()) getView().showEmptyPost();
    }

    @Override
    public void receiveKeywordBundleChanged() {
        if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
    }

    @Override
    public void receivePost(@Nullable PostSingleGridItemVO viewObject) {
        if (isViewAttached()) getView().showPost(viewObject);
    }

    @Override
    public void receivePostNotSelected() {
        if (isViewAttached()) getView().onPostNotSelected();
    }

    @Override
    public void receiveUpdatedSelectedGroupsCounter(int newCount, int total) {
        if (isViewAttached()) getView().updateSelectedGroupsCounter(newCount, total);
    }

    @Override
    public void sendAddKeywordRequest(Keyword keyword) {
        mediatorComponent.mediator().sendAddKeywordRequest(keyword);
    }

    @Override
    public void sendPostToGroupsRequest() {
        mediatorComponent.mediator().sendPostToGroupsRequest();
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
