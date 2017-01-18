package com.orcchg.vikstra.app.ui.group.list.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private String title;  // TODO: set initial title

    private GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter() {
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediatorComponent = DaggerGroupListMediatorComponent.builder()
                .groupListMediatorModule(new GroupListMediatorModule())
                .build();
        mediatorComponent.inject(this);
        mediatorComponent.mediator().attachFirst(this);
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PostCreateActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Timber.d("Post has been changed (and should be refreshed) resulting from screen with request code: %s", requestCode);
                    sendPostHasChangedRequest();
                }
                break;
            // TODO: handle result from PostListActivity after new Post selected
        }
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
        Timber.i("addKeyword: %s", keyword.toString());
        sendAddKeywordRequest(keyword);
    }

    @Override
    public void onDumpPressed() {
        Timber.i("onDumpPressed");
        // TODO: dump found groups
    }

    @Override
    public void onFabClick() {
        Timber.i("onFabClick");
        sendPostToGroupsRequest();
    }

    @Override
    public void onTitleChanged(String text) {
        Timber.i("onTitleChanged: %s", text);
        title = text;
    }

    /* Mediator */
    // ------------------------------------------
    @Override
    public void receiveAddKeywordError() {
        if (isViewAttached()) getView().onAddKeywordError();
    }

    @Override
    public void receiveEmptyPost() {
        if (isViewAttached()) getView().showEmptyPost();
    }

    @Override
    public void receiveGroupBundleChanged() {
        if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
    }

    @Override
    public void receiveGroupsNotSelected() {
        if (isViewAttached()) getView().onGroupsNotSelected();
    }

    @Override
    public void receiveKeywordBundleChanged() {
        if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
    }

    @Override
    public void receiveKeywordsLimitReached(int limit) {
        if (isViewAttached()) getView().onKeywordsLimitReached(limit);
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
    public void receivePostingStartedMessage(boolean isStarted) {
        if (isViewAttached()) getView().showPostingStartedMessage(isStarted);
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
    public void sendPostHasChangedRequest() {
        mediatorComponent.mediator().sendPostHasChangedRequest();
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
