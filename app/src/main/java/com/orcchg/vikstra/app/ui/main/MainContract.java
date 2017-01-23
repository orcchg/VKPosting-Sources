package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;

public interface MainContract {
    interface View extends KeywordListContract.View, PostSingleGridContract.View,
            IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void onLoggedOut();

        void openGroupListScreen(long keywordBundleId, long postId);
        void openReportScreen(long groupReportBundleId, long postId);
        void showFab(boolean isVisible);
        void updateGroupReportBundleId(long groupReportBundleId);
        void updatePostId(long postId);
    }

    interface Presenter extends MvpPresenter<View> {
        void onFabClick();
        void onScrollKeywordsList(int itemsLeftToEnd);
        void onScrollPostsGrid(int itemsLeftToEnd);

        void logout();

        void removeListItem(int position);
        void retryKeywords();
        void retryPosts();
    }
}
