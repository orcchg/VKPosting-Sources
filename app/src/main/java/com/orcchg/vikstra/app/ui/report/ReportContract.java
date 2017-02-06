package com.orcchg.vikstra.app.ui.report;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.util.endpoint.AccessTokenTracker;

public interface ReportContract {
    interface View extends AccessTokenTracker, SubView {
        void enableSwipeToRefresh(boolean isEnabled);
        void enableButtonsOnPostingFinished();

        void onPostingCancel();
        void onPostingFinished(int posted, int total);
        void onPostRevertingStarted();
        void onPostRevertingError();
        void onPostRevertingFinished();

        void openCloseWhilePostingDialog();
        void openDumpNotReadyDialog();
        void openEditDumpFileNameDialog();
        void openGroupDetailScreen(long groupId);
        void openRevertAllWarningDialog();

        void showDumpError();
        void showDumpSuccess(String path);
        void showEmptyPost();
        void showErrorPost();
        void showPost(PostSingleGridItemVO viewObject);
        void updatePostedCounters(int posted, int total);

        void closeView();
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
        void performReverting();
        void retry();
        void retryPost();
    }
}
