package com.orcchg.vikstra.app.ui.group.list.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.DumpGroups;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private final DumpGroups dumpGroupsUseCase;

    private String title;  // TODO: set initial title

    private GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter(DumpGroups dumpGroupsUseCase) {
        this.dumpGroupsUseCase = dumpGroupsUseCase;
        this.dumpGroupsUseCase.setPostExecuteCallback(createDumpGroupsCallback());
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
        long groupBundleId = sendAskForGroupBundleIdToDump();
        if (isViewAttached()) {
            if (groupBundleId != Constant.BAD_ID) {
                Timber.d("GroupBundle id [%s] is valid, ready to dump", groupBundleId);
                dumpGroupsUseCase.setParameters(new DumpGroups.Parameters(groupBundleId));
                getView().openEditDumpFileNameDialog();
            } else {
                Timber.d("GroupBundle is not available to dump");
                getView().openDumpNotReadyDialog();
            }
        }
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

    @Override
    public void performDumping(String path) {
        Timber.i("performDumping: %s", path);
        dumpGroupsUseCase.setPath(path);
        dumpGroupsUseCase.execute();
    }

    @Override
    public void retry() {
        Timber.i("retry");
        sendAskForRetry();
    }

    @Override
    public void retryPost() {
        Timber.i("retryPost");
        sendAskForRetryPost();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void setPostingTimeout(int timeout) {
        sendPostingTimeout(timeout);
    }

    /* Mediator */
    // --------------------------------------------------------------------------------------------
    @Override
    public void receiveAddKeywordError() {
        if (isViewAttached()) getView().onAddKeywordError();
    }

    @Override
    public void receiveAlreadyAddedKeyword(String keyword) {
        if (isViewAttached()) getView().onAlreadyAddedKeyword(keyword);
    }

    @Override
    public void receiveEnableAddKeywordButtonRequest(boolean isEnabled) {
        if (isViewAttached()) getView().enableAddKeywordButton(isEnabled);
    }

    @Override
    public void receiveEmptyPost() {
        if (isViewAttached()) getView().showEmptyPost();
    }

    @Override
    public void receiveErrorPost() {
        if (isViewAttached()) getView().showErrorPost();
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
    public void receiveShowPostingButtonRequest(boolean isVisible) {
        if (isViewAttached()) getView().showPostingButton(isVisible);
    }

    @Override
    public void receiveUpdatedSelectedGroupsCounter(int newCount, int total) {
        if (isViewAttached()) getView().updateSelectedGroupsCounter(newCount, total);
    }

    // ------------------------------------------
    @Override
    public void sendAddKeywordRequest(Keyword keyword) {
        mediatorComponent.mediator().sendAddKeywordRequest(keyword);
    }

    @Override
    public long sendAskForGroupBundleIdToDump() {
        return mediatorComponent.mediator().sendAskForGroupBundleIdToDump();
    }

    @Override
    public void sendAskForRetry() {
        mediatorComponent.mediator().sendAskForRetry();
    }

    @Override
    public void sendAskForRetryPost() {
        mediatorComponent.mediator().sendAskForRetryPost();
    }

    @Override
    public void sendPostHasChangedRequest() {
        mediatorComponent.mediator().sendPostHasChangedRequest();
    }

    @Override
    public void sendPostToGroupsRequest() {
        mediatorComponent.mediator().sendPostToGroupsRequest();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void sendPostingTimeout(int timeout) {
        mediatorComponent.mediator().sendPostingTimeout(timeout);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    // TODO: assign title
    // TODO: call: getView().setInputGroupsTitle(title);

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<String> createDumpGroupsCallback() {
        return new UseCase.OnPostExecuteCallback<String>() {
            @Override
            public void onFinish(@Nullable String path) {
                if (!TextUtils.isEmpty(path)) {
                    Timber.i("Use-Case: succeeded to dump Group-s");
                    if (isViewAttached()) getView().showDumpSuccess(path);
                } else {
                    Timber.e("Use-Case: failed to dump Group-s");
                    if (isViewAttached()) getView().showDumpError();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to dump Group-s");
                if (isViewAttached()) getView().showDumpError();
            }
        };
    }
}
