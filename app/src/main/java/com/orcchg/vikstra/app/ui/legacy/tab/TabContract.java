package com.orcchg.vikstra.app.ui.legacy.tab;

import com.orcchg.vikstra.domain.model.Genre;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

import java.util.List;

public interface TabContract {
    interface View extends MvpView {
        void showTabs(List<Genre> genres);
        void showError();
        void showLoading();
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
    }
}
