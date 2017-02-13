package com.orcchg.vikstra.app.ui.report.history;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;

public interface ReportHistoryContract {
    interface View extends LceView, MvpListView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
