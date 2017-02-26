package com.orcchg.vikstra.app.ui.report.main;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.model.misc.PostingUnit;
import com.orcchg.vikstra.domain.util.endpoint.AccessTokenTracker;

public interface ReportContract {
    interface View extends AccessTokenTracker, SubView {
        void enableSwipeToRefresh(boolean isEnabled);

        String getDumpFilename();

        void onPostingCancel();
        void onPostingFinished(int posted, int total);
        void onPostRevertingStarted();
        void onPostRevertingEmpty();
        void onPostRevertingError();
        void onPostRevertingFinished();
        void onWallPostingInterrupt();
        void onWallPostingSuspend(boolean paused);

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
        void switchToNormalMode(long groupReportBundleId);
    }

    interface SubView extends LceView, MvpListView {
        void enableSwipeToRefresh(boolean isEnabled);
        void showGroupReports(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
        void onCloseView();
        void onDumpPressed();
        void onSuspendClick();

        void onPostingCancel(int apiErrorCode, long groupReportBundleId);
        void onPostingFinish(long groupReportBundleId);
        void onPostingProgress(PostingUnit postingUnit);

        void interruptPostingAndClose(boolean shouldClose);

        void performDumping(String path);
        void performDumping(String path, @Nullable String email);
        void performReverting();

        void retry();
        void retryPost();
    }
}
