package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public interface GroupListContract {
    interface View extends MvpView, ViewMediator {
        void openEditTitleDialog(@Nullable String initTitle);
        void setInputGroupsTitle(String title);
    }

    interface ViewMediator {
        void showEmptyPost();
        void showPost(@Nullable PostSingleGridItemVO viewObject);
        void updateSelectedGroupsCounter(int newCount, int total);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            ActivityMediator.Receiver, ActivityMediator.Sender {
        void onAddKeyword();
        void onDumpPressed();
        void onFabClick();
        void onTitleChanged(String text);
    }
}
