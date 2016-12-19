package com.orcchg.vikstra.app.ui.report;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public interface ReportContract {
    interface View extends MvpView {
        void showPost(PostSingleGridItemVO viewObject);
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
