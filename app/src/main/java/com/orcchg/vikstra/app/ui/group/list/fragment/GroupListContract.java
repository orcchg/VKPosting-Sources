package com.orcchg.vikstra.app.ui.group.list.fragment;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;

interface GroupListContract {
    interface View extends LceView, MvpListView, IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void onReportReady(long groupReportBundleId, long postId);
        void openInteractiveReportScreen(long postId);
        void openGroupDetailScreen(long groupId);
        void openStatusScreen();

        void showGroups(boolean isEmpty);
        void updateGroupReportBundleId(long groupReportBundleId);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            FragmentMediator.Receiver, FragmentMediator.Sender {
        void removeListItem(int position);
        void retry();
    }
}
