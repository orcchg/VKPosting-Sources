package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.mediator.BaseMediator;
import com.orcchg.vikstra.app.ui.group.list.activity.ActivityMediator;
import com.orcchg.vikstra.app.ui.group.list.fragment.FragmentMediator;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;

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
    public void sendPostToGroupsRequest() {
        clientSecond.receivePostToGroupsRequest();
    }

    // ------------------------------------------
    @Override
    public void sendEmptyPost() {
        clientFirst.receiveEmptyPost();
    }

    @Override
    public void sendKeywordBundleChanged() {
        clientFirst.receiveKeywordBundleChanged();
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
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        clientFirst.receiveUpdatedSelectedGroupsCounter(newCount, total);
    }
}
