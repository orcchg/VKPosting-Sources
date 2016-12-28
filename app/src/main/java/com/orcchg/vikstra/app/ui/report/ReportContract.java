package com.orcchg.vikstra.app.ui.report;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.common.screen.LceView;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public interface ReportContract {
    interface View extends SubView {
        void showEmptyPost();
        void showPost(PostSingleGridItemVO viewObject);
    }

    interface SubView extends LceView, MvpListView {
        void showGroupReports(boolean isEmpty);
    }

    interface Presenter extends MvpPresenter<View>, ListPresenter {
    }
}
