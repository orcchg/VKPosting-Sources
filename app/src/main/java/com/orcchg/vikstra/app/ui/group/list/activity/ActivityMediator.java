package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.mediator.MediatorReceiver;
import com.orcchg.vikstra.app.ui.base.mediator.MediatorSender;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;

public interface ActivityMediator {
    interface Receiver extends MediatorReceiver {
        void receiveEmptyPost();
        void receiveKeywordBundleChanged();
        void receivePost(@Nullable PostSingleGridItemVO viewObject);
        void receiveUpdatedSelectedGroupsCounter(int newCount, int total);
    }

    interface Sender extends MediatorSender {
        void sendAddKeywordRequest(Keyword keyword);
        void sendPostToGroupsRequest();
    }
}
