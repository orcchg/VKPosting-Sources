package com.orcchg.vikstra.app.ui.group.detail;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface GroupDetailContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
