package com.orcchg.vikstra.app.ui.report;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public interface ReportContract {
    interface View extends SubView {
        void enableSwipeToRefresh(boolean isEnabled);

        void onPostingCancel();
        void openDumpNotReadyDialog();
        void openEditDumpFileNameDialog();

        void showDumpError();
        void showDumpSuccess(String path);
        void showEmptyPost();
        void showErrorPost();
        void showPost(PostSingleGridItemVO viewObject);
        void updatePostedCounters(int posted, int total);
    }

    interface SubView extends LceView, MvpListView {
        void enableSwipeToRefresh(boolean isEnabled);
        void showGroupReports(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
        void onDumpPressed();
        void performDumping(String path);
        void retry();
    }
}
