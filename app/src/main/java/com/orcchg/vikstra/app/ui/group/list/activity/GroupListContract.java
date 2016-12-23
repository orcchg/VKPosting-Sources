package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;

public interface GroupListContract {
    interface View extends MvpView {
        void openEditTitleDialog(@Nullable String initTitle);
        void setInputGroupsTitle(String title);
    }

    interface Presenter extends MvpPresenter<View> {
        void onDumpPressed();
        void onTitleChanged(String text);
    }
}
