package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.mediator.MediatorReceiver;
import com.orcchg.vikstra.app.ui.base.mediator.MediatorSender;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;

public interface FragmentMediator {
    interface Receiver extends MediatorReceiver {
        void receiveAddKeywordRequest(Keyword keyword);
        void receivePostToGroupsRequest();
    }

    interface Sender extends MediatorSender {
        void sendEmptyPost();
        void sendPost(@Nullable PostSingleGridItemVO viewObject);
        void sendUpdatedSelectedGroupsCounter(int newCount, int total);
    }
}
