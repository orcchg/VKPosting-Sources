package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.mediator.MediatorReceiver;
import com.orcchg.vikstra.app.ui.base.mediator.MediatorSender;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.DebugSake;

public interface ActivityMediator {
    interface Receiver extends MediatorReceiver {
        void receiveAddKeywordError();
        void receiveEmptyPost();
        void receiveErrorPost();
        void receiveGroupBundleChanged();
        void receiveGroupsNotSelected();
        void receiveKeywordBundleChanged();
        void receiveKeywordsLimitReached(int limit);
        void receivePost(@Nullable PostSingleGridItemVO viewObject);
        void receivePostNotSelected();
        void receivePostingStartedMessage(boolean isStarted);
        void receiveShowPostingButtonRequest(boolean isVisible);
        void receiveUpdatedSelectedGroupsCounter(int newCount, int total);
    }

    interface Sender extends MediatorSender {
        void sendAddKeywordRequest(Keyword keyword);
        long sendAskForGroupBundleIdToDump();
        void sendPostHasChangedRequest();
        void sendPostToGroupsRequest();

        @DebugSake
        void sendPostingTimeout(int timeout);
    }
}
