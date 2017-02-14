package com.orcchg.vikstra.app.ui.report.main;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.endpoint.AccessTokenTracker;

public interface ReportContract {
    interface View extends AccessTokenTracker, SubView,
            IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void enableSwipeToRefresh(boolean isEnabled);
        void enableButtonsOnPostingFinished();

        String getDumpFilename();

        void onPostingCancel();
        void onPostingFinished(int posted, int total);
        void onPostRevertingStarted();
        void onPostRevertingEmpty();
        void onPostRevertingError();
        void onPostRevertingFinished();

        void openCloseWhilePostingDialog();
        void openDumpNotReadyDialog();
        void openEditDumpFileNameDialog();
        void openEditDumpEmailDialog();
        void openEmailScreen(EmailContent.Builder builder);
        void openGroupDetailScreen(long groupId);
        void openRevertAllWarningDialog();

        void showDumpError();
        void showDumpSuccess(String path);
        void showEmptyPost();
        void showErrorPost();
        void showPost(PostSingleGridItemVO viewObject);
        void updatePostedCounters(int posted, int total);

        void closeView();
        boolean isForceDisableInteractiveMode();

        void cancelPreviousNotifications();
        void updateGroupReportBundleId(long groupReportBundleId);
    }

    interface SubView extends LceView, MvpListView {
        void enableSwipeToRefresh(boolean isEnabled);
        void showGroupReports(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
        void onCloseView();
        void onDumpPressed();
        void interruptPostingAndClose(boolean shouldClose);
        void performDumping(String path);
        void performDumping(String path, @Nullable String email);
        void performReverting();
        void retry();
        void retryPost();
    }
}
