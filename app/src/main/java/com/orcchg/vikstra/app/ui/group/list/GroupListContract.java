package com.orcchg.vikstra.app.ui.group.list;

import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;

public interface GroupListContract {
    interface View extends MvpListView {
        void showGroups(boolean isEmpty);
        void showError();
    }

    interface Presenter extends MvpPresenter<View> {
        void retry();
    }
}
