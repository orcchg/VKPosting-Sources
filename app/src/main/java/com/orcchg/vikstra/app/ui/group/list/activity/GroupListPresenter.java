package com.orcchg.vikstra.app.ui.group.list.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import javax.inject.Inject;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    String title;  // TODO: set initial title

    GroupListMediatorComponent mediatorComponent;

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
    public void onAddKeyword() {
        sendAddKeywordRequest();
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
    public void receivePost(@Nullable PostSingleGridItemVO viewObject) {
        if (isViewAttached()) getView().showPost(viewObject);
    }

    @Override
    public void receiveUpdatedSelectedGroupsCounter(int newCount, int total) {
        if (isViewAttached()) getView().updateSelectedGroupsCounter(newCount, total);
    }

    @Override
    public void sendAddKeywordRequest() {
        mediatorComponent.mediator().sendAddKeywordRequest();
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
