package com.orcchg.vikstra.app.ui.report.history;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;

public interface ReportHistoryContract {
    interface View extends LceView, MvpListView {
        void openPostViewScreen(long postId);
        void openReportScreen(long groupReportBundleId, long keywordBundleId, long postId);

        void showReports(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View> {
        void refresh();
        void retry();
    }
}
