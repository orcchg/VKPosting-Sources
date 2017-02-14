package com.orcchg.vikstra.app.ui.main;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.app.ui.viewobject.UserVO;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.endpoint.AccessTokenTracker;

public interface MainContract {
    interface View extends AccessTokenTracker,
            KeywordListContract.View, PostSingleGridContract.View,
            IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void notifyBothListsHaveItems();
        void onKeywordBundleAndPostNotSelected();
        void onLoggedOut();

        void openGroupListScreen(long keywordBundleId, long postId);
        void openReportScreen(long groupReportBundleId, long keywordBundleId, long postId);
        void showCurrentUser(@Nullable UserVO viewObject);
        void showFab(boolean isVisible);
        void updateGroupReportBundleId(long groupReportBundleId);
        void updateKeywordBundleId(long keywordBundleId);
        void updatePostId(long postId);
    }

    interface Presenter extends MvpPresenter<View> {
        void onFabClick();
        void onScrollKeywordsList(int itemsLeftToEnd);
        void onScrollPostsGrid(int itemsLeftToEnd);

        void logout();

        void removeKeywordListItem(int position);
        void removePostGridItem(int position);
        void retryKeywords();
        void retryPosts();
    }
}
