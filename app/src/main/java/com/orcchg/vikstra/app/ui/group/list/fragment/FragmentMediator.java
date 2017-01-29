package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.mediator.MediatorReceiver;
import com.orcchg.vikstra.app.ui.base.mediator.MediatorSender;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.DebugSake;

public interface FragmentMediator {
    interface Receiver extends MediatorReceiver {
        void receiveAddKeywordRequest(Keyword keyword);
        long receiveAskForGroupBundleIdToDump();
        void receiveAskForRetry();
        void receiveAskForRetryPost();
        void receivePostHasChangedRequest();
        void receivePostToGroupsRequest();

        @DebugSake
        void receivePostingTimeout(int timeout);
    }

    interface Sender extends MediatorSender {
        void sendAddKeywordError();
        void sendEnableAddKeywordButtonRequest(boolean isEnabled);
        void sendEmptyPost();
        void sendErrorPost();
        void sendGroupBundleChanged();
        void sendGroupsNotSelected();
        void sendKeywordBundleChanged();
        void sendKeywordsLimitReached(int limit);
        void sendPost(@Nullable PostSingleGridItemVO viewObject);
        void sendPostNotSelected();
        void sendPostingStartedMessage(boolean isStarted);
        void sendShowPostingButtonRequest(boolean isVisible);
        void sendUpdatedSelectedGroupsCounter(int newCount, int total);
    }
}
