package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.DebugSake;

interface GroupListContract {
    interface View extends MvpView {
        void onAddKeywordError();
        void onGroupsNotSelected();
        void onKeywordsLimitReached(int limit);
        void onPostNotSelected();

        void openAddKeywordDialog();
        void openEditDumpFileNameDialog();
        void openDumpNotReadyDialog();
        void openEditTitleDialog(@Nullable String initTitle);

        void setInputGroupsTitle(String title);
        void setCloseViewResult(int result);

        void showDumpError();
        void showDumpSuccess(String path);
        void showEmptyPost();
        void showErrorPost();
        void showPost(@Nullable PostSingleGridItemVO viewObject);
        void showPostingButton(boolean isVisible);
        void showPostingStartedMessage(boolean isStarted);
        void updateSelectedGroupsCounter(int newCount, int total);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            ActivityMediator.Receiver, ActivityMediator.Sender {
        void addKeyword(Keyword keyword);
        void onDumpPressed();
        void onFabClick();
        void onTitleChanged(String text);
        void performDumping(String path);
        void retry();

        @DebugSake
        void setPostingTimeout(int timeout);
    }
}
