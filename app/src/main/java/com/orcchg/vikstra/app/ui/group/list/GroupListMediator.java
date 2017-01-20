package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.mediator.BaseMediator;
import com.orcchg.vikstra.app.ui.group.list.activity.ActivityMediator;
import com.orcchg.vikstra.app.ui.group.list.fragment.FragmentMediator;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.DebugSake;

import hugo.weaving.DebugLog;

@PerActivity
public class GroupListMediator extends BaseMediator<ActivityMediator.Receiver, FragmentMediator.Receiver>
        implements ActivityMediator.Sender, FragmentMediator.Sender {

    @DebugLog
    public GroupListMediator() {
    }

    @Override
    public void sendAddKeywordRequest(Keyword keyword) {
        clientSecond.receiveAddKeywordRequest(keyword);
    }

    @Override
    public long sendAskForGroupBundleIdToDump() {
        return clientSecond.receiveAskForGroupBundleIdToDump();
    }

    @Override
    public void sendPostHasChangedRequest() {
        clientSecond.receivePostHasChangedRequest();
    }

    @Override
    public void sendPostToGroupsRequest() {
        clientSecond.receivePostToGroupsRequest();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void sendPostingTimeout(int timeout) {
        clientSecond.receivePostingTimeout(timeout);
    }

    // --------------------------------------------------------------------------------------------
    @Override
    public void sendAddKeywordError() {
        clientFirst.receiveAddKeywordError();
    }

    @Override
    public void sendEmptyPost() {
        clientFirst.receiveEmptyPost();
    }

    @Override
    public void sendGroupBundleChanged() {
        clientFirst.receiveGroupBundleChanged();
    }

    @Override
    public void sendGroupsNotSelected() {
        clientFirst.receiveGroupsNotSelected();
    }

    @Override
    public void sendKeywordBundleChanged() {
        clientFirst.receiveKeywordBundleChanged();
    }

    @Override
    public void sendKeywordsLimitReached(int limit) {
        clientFirst.receiveKeywordsLimitReached(limit);
    }

    @Override
    public void sendPost(@Nullable PostSingleGridItemVO viewObject) {
        clientFirst.receivePost(viewObject);
    }

    @Override
    public void sendPostNotSelected() {
        clientFirst.receivePostNotSelected();
    }

    @Override
    public void sendPostingStartedMessage(boolean isStarted) {
        clientFirst.receivePostingStartedMessage(isStarted);
    }

    @Override
    public void sendShowPostingButtonRequest(boolean isVisible) {
        clientFirst.receiveShowPostingButtonRequest(isVisible);
    }

    @Override
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        clientFirst.receiveUpdatedSelectedGroupsCounter(newCount, total);
    }
}
