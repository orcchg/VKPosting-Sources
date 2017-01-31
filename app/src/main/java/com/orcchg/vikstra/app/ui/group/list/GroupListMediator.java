package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.mediator.BaseMediator;
import com.orcchg.vikstra.app.ui.group.list.activity.ActivityMediator;
import com.orcchg.vikstra.app.ui.group.list.fragment.FragmentMediator;
import com.orcchg.vikstra.app.ui.util.ContextUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

@PerActivity
public class GroupListMediator extends BaseMediator<ActivityMediator.Receiver, FragmentMediator.Receiver>
        implements ActivityMediator.Sender, FragmentMediator.Sender {

    @DebugLog @Inject
    public GroupListMediator() {
    }

    @Override
    public void sendAddKeywordRequest(Keyword keyword) {
        if (clientSecond != null) clientSecond.receiveAddKeywordRequest(keyword);
    }

    @Override
    public long sendAskForGroupBundleIdToDump() {
        if (clientSecond != null) return clientSecond.receiveAskForGroupBundleIdToDump();
        return Constant.BAD_ID;
    }

    @Override
    public void sendAskForRetry() {
        if (clientSecond != null) clientSecond.receiveAskForRetry();
    }

    @Override
    public void sendAskForRetryPost() {
        if (clientSecond != null) clientSecond.receiveAskForRetryPost();
    }

    @Override
    public void sendNewTitle(String newTitle) {
        if (clientSecond != null) clientSecond.receiveNewTitle(newTitle);
    }

    @Override
    public void sendPostHasChangedRequest() {
        if (clientSecond != null) clientSecond.receivePostHasChangedRequest();
    }

    @Override
    public void sendPostToGroupsRequest() {
        if (clientSecond != null) clientSecond.receivePostToGroupsRequest();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void sendPostingTimeout(int timeout) {
        if (clientSecond != null) clientSecond.receivePostingTimeout(timeout);
    }

    // --------------------------------------------------------------------------------------------
    @Override
    public void sendAddKeywordError() {
        if (clientFirst != null) clientFirst.receiveAddKeywordError();
    }

    @Override
    public void sendAlreadyAddedKeyword(String keyword) {
        if (clientFirst != null) clientFirst.receiveAlreadyAddedKeyword(keyword);
    }

    @Override
    public String sendAskForTitle() {
        if (clientFirst != null) return clientFirst.receiveAskForTitle();
        return ContextUtility.defaultTitle();
    }

    @Override
    public void sendEnableAddKeywordButtonRequest(boolean isEnabled) {
        if (clientFirst != null) clientFirst.receiveEnableAddKeywordButtonRequest(isEnabled);
    }

    @Override
    public void sendEmptyPost() {
        if (clientFirst != null) clientFirst.receiveEmptyPost();
    }

    @Override
    public void sendErrorPost() {
        if (clientFirst != null) clientFirst.receiveErrorPost();
    }

    @Override
    public void sendGroupBundleChanged() {
        if (clientFirst != null) clientFirst.receiveGroupBundleChanged();
    }

    @Override
    public void sendGroupsNotSelected() {
        if (clientFirst != null) clientFirst.receiveGroupsNotSelected();
    }

    @Override
    public void sendKeywordBundleChanged() {
        if (clientFirst != null) clientFirst.receiveKeywordBundleChanged();
    }

    @Override
    public void sendKeywordsLimitReached(int limit) {
        if (clientFirst != null) clientFirst.receiveKeywordsLimitReached(limit);
    }

    @Override
    public void sendPost(@Nullable PostSingleGridItemVO viewObject) {
        if (clientFirst != null) clientFirst.receivePost(viewObject);
    }

    @Override
    public void sendPostNotSelected() {
        if (clientFirst != null) clientFirst.receivePostNotSelected();
    }

    @Override
    public void sendPostingStartedMessage(boolean isStarted) {
        if (clientFirst != null) clientFirst.receivePostingStartedMessage(isStarted);
    }

    @Override
    public void sendShowPostingButtonRequest(boolean isVisible) {
        if (clientFirst != null) clientFirst.receiveShowPostingButtonRequest(isVisible);
    }

    @Override
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        if (clientFirst != null) clientFirst.receiveUpdatedSelectedGroupsCounter(newCount, total);
    }
}
