package com.orcchg.vikstra.app.ui.group.list.fragment;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;

interface GroupListContract {
    interface View extends MvpListView, IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void onAddKeywordError();
        void onKeywordsLimitReached(int limit);
        void openGroupDetailScreen(long groupId);
        void openReportScreen(long postId);
        void showError();
        void showGroups(boolean isEmpty);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            FragmentMediator.Receiver, FragmentMediator.Sender {
        void removeListItem(int position);
        void retry();
    }
}
