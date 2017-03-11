package com.orcchg.vikstra.app.ui.group.custom.activity;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpListView;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

public interface GroupCustomListContract {
    interface View extends MvpListView {
        void enableAddKeywordButton(boolean isEnabled);

        void onGroupsNotSelected();
        void onPostNotSelected();

        void openEditTitleDialog(@Nullable String initTitle);
        void openPostCreateScreen(long postId);
        void openPostListScreen();

        String getInputGroupsTitle();
        void setInputGroupsTitle(String title);
        void setCloseViewResult(int result);
        void setNewPostId(long postId);

        void showEmptyPost();
        void showErrorPost();
        void showPost(@Nullable PostSingleGridItemVO viewObject);
        void showPostingButton(boolean isVisible);
        void showPostingFailed();
        void showPostingStartedMessage(boolean isStarted);
        void updateSelectedGroupsCounter(int newCount, int total);
    }

    // ------------------------------------------
    interface Presenter extends MvpPresenter<View> {
        void onBackPressed();
        void onFabClick();
        void onPostThumbnailClick(long postId);
        void onTitleChanged(String text);
        void retry();
        void retryPost();
        void setPostingTimeout(int timeout);
    }
}
