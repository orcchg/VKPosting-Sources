package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListContract;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridContract;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;

public interface MainContract {
    interface View extends KeywordListContract.View, PostSingleGridContract.View,
            IPostingNotificationDelegate, IPhotoUploadNotificationDelegate {
        void openGroupListScreen(long keywordBundleId, long postId);
        void openReportScreen(long postId);
        void showFab(boolean isVisible);
        void updatePostId(long postId);
    }

    interface Presenter extends MvpPresenter<View> {
        void retryKeywords();
        void retryPosts();
        void onFabClick();
        void onScrollKeywordsList(int itemsLeftToEnd);
    }
}
