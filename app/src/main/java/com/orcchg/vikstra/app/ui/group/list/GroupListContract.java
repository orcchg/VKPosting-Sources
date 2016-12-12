package com.orcchg.vikstra.app.ui.group.list;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;

public interface GroupListContract {
    interface View extends MvpListView {
        void onFabClick();
        void openGroupDetailScreen(long groupId);
        void showGroups(boolean isEmpty);
        void showError();
        void updateSelectedGroupsCounter(int newCount);
    }

    interface Presenter extends MvpPresenter<View> {
        void postToGroups();
        void retry();
    }
}
