package com.orcchg.vikstra.app.ui.group.list;

import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface GroupListContract {
    interface View extends MvpView {
        RecyclerView getListView();
        void showGroups(boolean isEmpty);
        void showError();
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
    }
}
