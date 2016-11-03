package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface MainContract {
    interface View extends MvpView {
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
        void onScroll(int itemsLeftToEnd);
    }
}
