package com.orcchg.vikstra.app.ui.group;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface GroupContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
