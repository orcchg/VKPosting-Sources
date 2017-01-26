package com.orcchg.vikstra.app.ui.group.detail;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface GroupDetailContract {
    interface View extends MvpView {
        void onGroupLoaded(String url);
    }

    interface Presenter extends MvpPresenter<View> {
    }
}
