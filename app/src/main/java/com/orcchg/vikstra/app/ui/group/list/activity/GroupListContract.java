package com.orcchg.vikstra.app.ui.group.list.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.MvpView;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.misc.EmailContent;

interface GroupListContract {
    interface View extends MvpView {
        void enableAddKeywordButton(boolean isEnabled);

        String getDumpFilename();

        void onAddKeywordError();
        void onAlreadyAddedKeyword(String keyword);
        void onGroupsNotSelected();
        void onKeywordsLimitReached(int limit);
        void onPostNotSelected();

        void openAddKeywordDialog();
        void openEditDumpFileNameDialog();
        void openEditDumpEmailDialog();
        void openDumpNotReadyDialog();
        void openEditTitleDialog(@Nullable String initTitle);
        void openEmailScreen(EmailContent.Builder builder);
        void openPostCreateScreen(long postId);
        void openPostListScreen();

        String getInputGroupsTitle();
        void setInputGroupsTitle(String title);
        void setCloseViewResult(int result);
        void setNewPostId(long postId);

        void showDumpError();
        void showDumpSuccess(String path);
        void showEmptyPost();
        void showErrorPost();
        void showPost(@Nullable PostSingleGridItemVO viewObject);
        void showPostingButton(boolean isVisible);
        void showPostingFailed();
        void showPostingStartedMessage(boolean isStarted);
        void updateSelectedGroupsCounter(int newCount, int total);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View>,
            ActivityMediator.Receiver, ActivityMediator.Sender {
        void addKeyword(Keyword keyword);
        void onBackPressed();
        void onDumpPressed();
        void onFabClick();
        void onPostThumbnailClick(long postId);
        void onTitleChanged(String text);
        void performDumping(String path);
        void performDumping(String path, @Nullable String email);
        void retry();
        void retryPost();
        void setPostingTimeout(int timeout);
    }
}
