package com.orcchg.vikstra.app.ui.group.list.fragment;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.endpoint.AccessTokenTracker;

interface GroupListContract {
    interface View extends AccessTokenTracker, LceView, MvpListView,
            IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void enableSwipeToRefresh(boolean isEnabled);

        void onReportReady(long groupReportBundleId, long keywordBundleId, long postId);
        void onSearchingGroupsCancel();
        void openInteractiveReportScreen(long keywordBundleId, long postId);
        void openGroupDetailScreen(long groupId);
        void openStatusScreen();

        void showGroups(boolean isEmpty);
        void updateGroupReportBundleId(long groupReportBundleId);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            FragmentMediator.Receiver, FragmentMediator.Sender {
        void removeChildListItem(int position, int parentPosition);
        void removeParentListItem(int position);
        void refresh();
        void retry();
    }
}
