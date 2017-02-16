package com.orcchg.vikstra.app.ui.settings.group;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.common.screen.LceView;

public interface GroupSettingsContract {
    interface View extends LceView, MvpView {
        void openSaveChangesDialog();
        void closeView();  // with currently set result
        void closeView(int resultCode);
    }

    interface Presenter extends MvpPresenter<View> {
        void onBackPressed();
        void onSavePressed();
        void retry();
    }
}
