package com.orcchg.vikstra.app.ui.group.list;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.domain.model.Keyword;

public interface GroupListContract {
    interface View extends MvpListView {
        void openGroupDetailScreen(long groupId);
        void showGroups(boolean isEmpty);
        void showError();
        void updateSelectedGroupsCounter(int newCount, int total);
    }

    interface Presenter extends MvpPresenter<View> {
        void addKeyword(Keyword keyword);
        void postToGroups();
        void retry();
    }
}
