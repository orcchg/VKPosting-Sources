package com.orcchg.vikstra.app.ui.group.custom.fragment;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListContract;

public interface GroupCustomListContract {
    interface View extends GroupListContract.View {
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View> {
        void refresh();
        void retry();
    }
}
